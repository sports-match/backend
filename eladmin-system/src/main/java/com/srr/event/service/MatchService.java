package com.srr.event.service;

import com.srr.enumeration.Format;
import com.srr.event.domain.Event;
import com.srr.event.domain.Match;
import com.srr.event.domain.MatchStatus;
import com.srr.event.dto.MatchDto;
import com.srr.event.dto.MatchScoreUpdateDto;
import com.srr.event.mapper.MatchMapper;
import com.srr.event.repository.EventRepository;
import com.srr.event.repository.MatchRepository;
import com.srr.player.domain.*;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.RatingHistoryRepository;
import com.srr.player.repository.TeamPlayerRepository;
import com.srr.utils.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of match service operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final MatchMapper matchMapper;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final RatingService ratingService;
    private final EventRepository eventRepository;

    /**
     * Updates the score of a match
     *
     * @param scoreUpdateDto the score update DTO
     * @return the updated match
     */
    @Transactional
    public Match updateMatchScore(MatchScoreUpdateDto scoreUpdateDto) {
        Match match = findById(scoreUpdateDto.getMatchId());

        // Validate badminton score
        validateBadmintonScore(scoreUpdateDto.getScoreA(), scoreUpdateDto.getScoreB());

        // Get user details
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Not authenticated");
        }

        // Check authority: Organizer can always update, Player only if in match
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        boolean isOrganizer = false;
        if (userDetails != null && userDetails.getAuthorities() != null) {
            isOrganizer = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("Organizer"));
        }

        boolean isInMatch = false;
        if (!isOrganizer) {
            List<TeamPlayer> teamPlayers = teamPlayerRepository.findByUserId(currentUserId);
            if (teamPlayers.isEmpty()) {
                throw new BadRequestException("No team assignments found for the current player");
            }
            if (match.getTeamA() != null && match.getTeamB() != null) {
                Long teamAId = match.getTeamA().getId();
                Long teamBId = match.getTeamB().getId();
                isInMatch = teamPlayers.stream()
                        .filter(tp -> tp.getTeam() != null)
                        .anyMatch(tp -> (teamAId.equals(tp.getTeam().getId()) ||
                                teamBId.equals(tp.getTeam().getId())));
            }
            if (!isInMatch) {
                throw new BadRequestException("You can only update scores for matches involving your team");
            }
        }
        // Organizer can always update, so skip this check if isOrganizer

        // Update scores
        match.setScoreA(scoreUpdateDto.getScoreA());
        match.setScoreB(scoreUpdateDto.getScoreB());

        // Determine winner
        if (match.getScoreA() > match.getScoreB()) {
            match.setTeamAWin(true);
            match.setTeamBWin(false);
        } else if (match.getScoreB() > match.getScoreA()) {
            match.setTeamAWin(false);
            match.setTeamBWin(true);
        } else {
            // It's a tie
            match.setTeamAWin(false);
            match.setTeamBWin(false);
        }

        // Reset verification status when scores are updated
        match.setScoreVerified(false);

        log.info("Updated match {} score: Team A {}, Team B {}",
                match.getId(), match.getScoreA(), match.getScoreB());

        return matchRepository.save(match);
    }

    private void validateBadmintonScore(int scoreA, int scoreB) {
        if ((scoreA < 21 && scoreB < 21) || Math.abs(scoreA - scoreB) < 2) {
            throw new BadRequestException("Invalid badminton score: winner must have at least 21 points and lead by at least 2.");
        }
    }

    /**
     * Finds a match by ID
     *
     * @param id the match ID
     * @return the match
     */
    @Transactional(readOnly = true)
    public Match findById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Match.class, "id", id.toString()));
    }

    /**
     * Verifies the score of a match
     *
     * @param matchId the match ID
     * @return the updated match
     */
    @Transactional
    public Match verifyMatchScore(Long matchId) {
        Match match = findById(matchId);

        if (match.getScoreA() == 0 && match.getScoreB() == 0) {
            throw new BadRequestException("Cannot verify a match with no scores");
        }

        match.setScoreVerified(true);
        log.info("Verified match {} score", match.getId());

        return matchRepository.save(match);
    }

    /**
     * Finds all matches for the current user
     *
     * @return the list of matches
     */
    @Transactional(readOnly = true)
    public List<Match> findMatchesForCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Not authenticated");
        }

        // Find all teams the current user is part of
        List<TeamPlayer> teamPlayers = teamPlayerRepository.findByUserId(currentUserId);

        if (teamPlayers.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all team IDs
        Set<Long> teamIds = teamPlayers.stream()
                .map(tp -> tp.getTeam().getId())
                .collect(Collectors.toSet());

        // Find all matches where the user's team is either team A or team B
        return matchRepository.findByTeamAIdInOrTeamBIdIn(teamIds, teamIds);
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findMatchesByGroupId(Long groupId) {
        List<Match> matches = matchRepository.findAllByMatchGroupIdOrderByMatchOrderAsc(groupId);
        return matchMapper.toDto(matches);
    }

    /**
     * Finds and groups matches by event
     *
     * @param eventId the event ID
     * @return the list of matches of an event
     */
    @Transactional(readOnly = true)
    public List<MatchDto> findMatchesByEventGrouped(Long eventId) {
        List<Match> matches = matchRepository.findByMatchGroupEventId(eventId);
        return matchMapper.toDto(matches);
    }

    /**
     * Submits all match scores for an event after validation. Discards matches with no scores entered.
     * Updates player ratings via RatingService for all submitted matches.
     *
     * @param eventId the event ID
     * @return number of matches submitted
     */
    @Transactional
    public int submitAllScores(Long eventId) {
        // Fetch all matches for the event
        List<Match> matches = matchRepository.findByMatchGroupEventId(eventId);

        // Validate: all matches must have scores entered (non-zero)
        List<Match> matchesWithoutScores = matches.stream()
                .filter(m -> (m.getScoreA() == 0 && m.getScoreB() == 0))
                .collect(Collectors.toList());
        if (!matchesWithoutScores.isEmpty()) {
            throw new BadRequestException("All matches must have scores submitted before final submission. Matches without scores: " + matchesWithoutScores.stream().map(Match::getId).toList());
        }
        List<Match> matchesWithScores = matches.stream()
                .filter(m -> (m.getScoreA() > 0 || m.getScoreB() > 0))
                .collect(Collectors.toList());
        if (matchesWithScores.isEmpty()) {
            throw new BadRequestException("No matches with scores to submit");
        }

        // Validate each scored match for badminton rules
        for (Match m : matchesWithScores) {
            validateBadmintonScore(m.getScoreA(), m.getScoreB());
        }

        // Mark all scored matches as verified
        matchesWithScores.forEach(m -> m.setScoreVerified(true));
        matchRepository.saveAll(matchesWithScores);

        // Update ratings for all matches
        for (Match match : matchesWithScores) {
            updateRatingsForMatch(match);
        }

        log.info("Submitted {} matches for event {}.", matchesWithScores.size(), eventId);
        return matchesWithScores.size();
    }

    /**
     * Updates ratings for a single match by invoking RatingService.
     * This method fetches all relevant player ratings and applies the rating update logic.
     */
    private void updateRatingsForMatch(Match match) {
        // Get event and format
        Event event = match.getMatchGroup().getEvent();
        //String sport = String.valueOf(event.getSportId()); // or event.getSportName() if available
        String sport = "Badminton";
        Format format = event.getFormat();

        // Get teams
        Team teamA = match.getTeamA();
        Team teamB = match.getTeamB();
        if (teamA == null || teamB == null) return;

        // Fetch team players
        List<TeamPlayer> teamAPlayers = teamPlayerRepository.findAllByTeamId(teamA.getId());
        List<TeamPlayer> teamBPlayers = teamPlayerRepository.findAllByTeamId(teamB.getId());
        if (format == Format.DOUBLE) {
            if (teamAPlayers.size() == 2 && teamBPlayers.size() == 2) {
                List<PlayerSportRating> teamARatings = teamAPlayers.stream()
                        .map(tp -> playerSportRatingRepository.findByPlayerIdAndSportAndFormat(tp.getPlayer().getId(), sport, Format.DOUBLE).orElse(null))
                        .filter(r -> r != null)
                        .toList();
                List<PlayerSportRating> teamBRatings = teamBPlayers.stream()
                        .map(tp -> playerSportRatingRepository.findByPlayerIdAndSportAndFormat(tp.getPlayer().getId(), sport, Format.DOUBLE).orElse(null))
                        .filter(r -> r != null)
                        .toList();
                if (teamARatings.size() == 2 && teamBRatings.size() == 2) {
                    double[] oldScores = { teamARatings.get(0).getRateScore(), teamARatings.get(1).getRateScore(), teamBRatings.get(0).getRateScore(), teamBRatings.get(1).getRateScore() };
                    ratingService.updateRatingsForDoubles(teamARatings, teamBRatings, match.getScoreA(), match.getScoreB());
                    playerSportRatingRepository.saveAll(teamARatings);
                    playerSportRatingRepository.saveAll(teamBRatings);
                    // Save to rating history
                    for (int i = 0; i < 2; i++) {
                        saveRatingHistory(teamAPlayers.get(i).getPlayer(), teamARatings.get(i), oldScores[i], match);
                        saveRatingHistory(teamBPlayers.get(i).getPlayer(), teamBRatings.get(i), oldScores[2+i], match);
                    }
                }
            }
        } else if (format == Format.SINGLE) {
            if (teamAPlayers.size() == 1 && teamBPlayers.size() == 1) {
                PlayerSportRating ratingA = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(teamAPlayers.get(0).getPlayer().getId(), sport, Format.SINGLE).orElse(null);
                PlayerSportRating ratingB = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(teamBPlayers.get(0).getPlayer().getId(), sport, Format.SINGLE).orElse(null);
                if (ratingA != null && ratingB != null) {
                    double oldA = ratingA.getRateScore();
                    double oldB = ratingB.getRateScore();
                    ratingService.updateRatingsForSingles(ratingA, ratingB, 0, 0, match.getScoreA(), match.getScoreB());
                    playerSportRatingRepository.save(ratingA);
                    playerSportRatingRepository.save(ratingB);
                    saveRatingHistory(teamAPlayers.get(0).getPlayer(), ratingA, oldA, match);
                    saveRatingHistory(teamBPlayers.get(0).getPlayer(), ratingB, oldB, match);
                }
            }
        }
    }

    private void saveRatingHistory(Player player, PlayerSportRating rating, double oldScore, Match match) {
        RatingHistory history = new RatingHistory();
        history.setPlayer(player);
        history.setRateScore(rating.getRateScore());
        history.setChanges(rating.getRateScore() - oldScore);
        history.setMatch(match);
        ratingHistoryRepository.save(history);
    }

    @Transactional
    public void withdrawMatch(Long matchId) {
        Match match = findById(matchId);
        match.setStatus(MatchStatus.WITHDRAWN);
        matchRepository.save(match);
        log.info("Match {} marked as WITHDRAWN", matchId);
    }
}
