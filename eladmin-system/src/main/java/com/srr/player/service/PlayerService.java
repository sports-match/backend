package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.event.domain.Match;
import com.srr.event.dto.EventMapper;
import com.srr.event.mapper.MatchMapper;
import com.srr.event.repository.EventRepository;
import com.srr.event.repository.MatchRepository;
import com.srr.event.service.MatchService;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.domain.TeamPlayer;
import com.srr.player.dto.*;
import com.srr.player.mapper.PlayerMapper;
import com.srr.player.mapper.RatingHistoryMapper;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.RatingHistoryRepository;
import com.srr.player.repository.TeamPlayerRepository;
import com.srr.sport.service.SportService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Chanheng
 * @description 服务实现
 * @date 2025-05-18
 **/
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final MatchService matchService;
    private final TeamPlayerRepository teamPlayerRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final MatchRepository matchRepository;
    private final SportService sportService;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final MatchMapper matchMapper;
    private final RatingHistoryMapper ratingHistoryMapper;

    public List<PlayerEventRatingDTO> getPlayerEventRating(Long eventId) {
        final List<TeamPlayer> teamPlayers = teamPlayerRepository.findByEventId(eventId);
        final List<PlayerEventRatingDTO> playerEventRatings = new ArrayList<>();

        for (TeamPlayer teamPlayer : teamPlayers) {
            final PlayerEventRatingDTO playerEventRating = new PlayerEventRatingDTO();
            double previousRating = 0;
            double ratingChanges = 0;
            int wins = 0;
            int losses = 0;

            // Get all matches for the event
            List<Match> eventMatches = teamPlayer.getTeam().getEvent().getMatchGroups().stream()
                    .flatMap(matchGroup -> matchGroup.getMatches().stream())
                    .toList();

            // Process each match
            for (Match match : eventMatches) {
                boolean isInTeamA = match.getTeamA().getTeamPlayers().stream()
                        .anyMatch(tp -> tp.getPlayer().getId().equals(teamPlayer.getPlayer().getId()));
                boolean isInTeamB = !isInTeamA && match.getTeamB().getTeamPlayers().stream()
                        .anyMatch(tp -> tp.getPlayer().getId().equals(teamPlayer.getPlayer().getId()));

                if (isInTeamA || isInTeamB) {
                    // Get rating history for this match and player
                    final var matchRatingHistory = ratingHistoryRepository.findByPlayerIdAndMatchId(
                            teamPlayer.getPlayer().getId(),
                            match.getId()
                    );

                    if (matchRatingHistory != null) {
                        if (previousRating == 0) {
                            // First match sets the initial rating
                            previousRating = matchRatingHistory.getRateScore() - matchRatingHistory.getChanges();
                        }
                        ratingChanges += matchRatingHistory.getChanges();
                    }

                    // Count wins and losses
                    if ((isInTeamA && match.isTeamAWin()) || (isInTeamB && !match.isTeamAWin())) {
                        wins++;
                    } else {
                        losses++;
                    }
                }
            }

            // Set the player's rating information
            playerEventRating.setName(teamPlayer.getPlayer().getName());
            playerEventRating.setWins(wins);
            playerEventRating.setLosses(losses);
            playerEventRating.setPreviousRating(previousRating);
            playerEventRating.setRatingChanges(ratingChanges);
            playerEventRating.setNewRating(previousRating + ratingChanges);

            playerEventRatings.add(playerEventRating);
        }

        return playerEventRatings;
    }

    public PageResult<PlayerDto> queryAll(PlayerQueryCriteria criteria, Pageable pageable) {

        Page<Player> page = playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
//        Page<PlayerDto> dtoPage = page.map(player -> {
//            PlayerDto dto = playerMapper.toDto(player);
////            dto.setPlayerSportRating(playerSportRatingRepository.findByPlayerId(player.getId())
////                    .stream()
////                    .map(rating -> {
////                        PlayerSportRatingDto dtoRating = new PlayerSportRatingDto();
////                        dtoRating.setId(rating.getId());
////                        dtoRating.setPlayerId(rating.getPlayer().getId());
////                        dtoRating.setSportId(rating.getSportId());
////                        dtoRating.setFormat(rating.getFormat());
////                        dtoRating.setRateScore(rating.getRateScore());
////                        dtoRating.setRateBand(rating.getRateBand());
////                        dtoRating.setProvisional(rating.getProvisional());
////                        dtoRating.setCreateTime(rating.getCreateTime());
////                        dtoRating.setUpdateTime(rating.getUpdateTime());
////                        return dtoRating;
////                    })
////                    .collect(java.util.stream.Collectors.toList()));
//            return dto;
        return PageUtil.toPage(page.map(playerMapper::toDto));
    }

    public List<PlayerDto> queryAll(PlayerQueryCriteria criteria) {
        return playerMapper.toDto(playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Transactional
    public PlayerDto findById(Long id) {
        Player player = playerRepository.findById(id).orElseGet(Player::new);
        ValidationUtil.isNull(player.getId(), "Player", "id", id);
        return playerMapper.toDto(player);
    }


    public PlayerDetailsDto  findPlayerDetailsById(Long id, PlayerDetailsRequest request) {
        var playerDto = playerRepository.findById(id)
                .map(playerMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Player.class, "id", id));

        var eventToday = eventRepository.getPlayerEventToday(id)
                .map(eventMapper::toDto)
                .orElse(null);

        var upcomingEvent = eventRepository.getPlayerUpcomingEvents(id)
                .stream()
                .map(eventMapper::toDto)
                .toList();

        double singleRating = 0;
        double singleRatingChanges = 0;
        double doubleRating = 0;
        double doubleRatingChanges = 0;

        // Check if there is a date filter
        var isNotFilterDate = request.getStartDate() == null || request.getEndDate() == null;
        // Get the rating history for the last 6 months
        var sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        var singleRatingsHistory = isNotFilterDate
                ? ratingHistoryRepository.findByPlayerIdAndFormatWithDate(id, Format.SINGLE, sixMonthsAgo) // get the last 6 months
                : ratingHistoryRepository.findByPlayerIdAndFormatWithDateRange(id, Format.SINGLE, request.getStartDate(), request.getEndDate());

        if (singleRatingsHistory != null && !singleRatingsHistory.isEmpty()) {
            singleRating = singleRatingsHistory.get(0).getRateScore();
            singleRatingChanges = singleRatingsHistory.get(0).getChanges();
        }

        var doubleRatingsHistory = isNotFilterDate
                ? ratingHistoryRepository.findByPlayerIdAndFormatWithDate(id, Format.DOUBLE, sixMonthsAgo) // get the last 6 months
                : ratingHistoryRepository.findByPlayerIdAndFormatWithDateRange(id, Format.DOUBLE, request.getStartDate(), request.getEndDate());

        if (doubleRatingsHistory != null && !doubleRatingsHistory.isEmpty()) {
            doubleRating = doubleRatingsHistory.get(0).getRateScore();
            doubleRatingChanges = doubleRatingsHistory.get(0).getChanges();
        }

        var playerEvents = eventRepository.getPlayerCompletedEvents(id);
        var allEventsJoined = playerEvents == null ? 0 : playerEvents.size();

        var lastMatch = matchRepository.getByPlayerId(id);

        return new PlayerDetailsDto()
                .setPlayer(playerDto)
                .setSingleEventRating(singleRating)
                .setSingleEventRatingChanges(singleRatingChanges)
                .setDoubleEventRating(doubleRating)
                .setDoubleEventRatingChanges(doubleRatingChanges)
                .setTotalEvent(allEventsJoined)
                .setEventToday(eventToday)
                .setLastMatch(matchMapper.toDto(lastMatch))
                .setUpcomingEvents(upcomingEvent)
                .setSingleEventRatingHistory(ratingHistoryMapper.toDto(singleRatingsHistory))
                .setDoubleEventRatingHistory(ratingHistoryMapper.toDto(doubleRatingsHistory));
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Player resources) {
        Player savedPlayer = playerRepository.save(resources);
        // No default answers; users will submit their own self-assessment
        return ExecutionResult.of(savedPlayer.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Player resources) {
        Player player = playerRepository.findById(resources.getId()).orElseGet(Player::new);
        ValidationUtil.isNull(player.getId(), "Player", "id", resources.getId());
        player.copy(resources);
        Player savedPlayer = playerRepository.save(player);
        return ExecutionResult.of(savedPlayer.getId());
    }

    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            playerRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }

    public Player findByUserId(Long userId) {
        return playerRepository.findByUserId(userId);
    }


    /**
     * @param sportId ID of sport for score rate validation
     * @return PlayerAssessmentStatusDto
     */
    public PlayerAssessmentStatusDto checkAssessmentStatus(Long sportId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return checkAssessmentStatus(sportId, currentUserId);
    }

    /**
     * @param sportId ID of sport for score rate validation
     * @param userId  User's ID used for checking assessment
     * @return PlayerAssessmentStatusDto
     */
    public PlayerAssessmentStatusDto checkAssessmentStatus(Long sportId, final Long userId) {
        Player player = findByUserId(userId);
        if (player == null) {
            return new PlayerAssessmentStatusDto(false, "Player profile not found. Please create your profile first.", userId);
        }

        boolean isAssessmentCompleted = false;
        Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(player.getId(), sportId, Format.DOUBLE);
        if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
            isAssessmentCompleted = true;
        }

        String message = isAssessmentCompleted
                ? "Self-assessment completed."
                : "Please complete your self-assessment before joining any events.";
        return new PlayerAssessmentStatusDto(isAssessmentCompleted, message, player.getId());
    }

    /**
     * Get paginated and filtered players with their doubles ranking, games played, wins, losses, and record.
     */
    public PageResult<PlayerDoublesStatsDto> getAllPlayersDoublesStats(PlayerQueryCriteria criteria, Pageable pageable) {
        Page<Player> page = playerRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        List<Player> players = page.getContent();
        List<PlayerDoublesStatsDto> result = new ArrayList<>();

        // For efficient lookup
        Map<Long, PlayerSportRating> doublesRatings = new HashMap<>();
        for (Player player : players) {
            List<PlayerSportRating> ratings = playerSportRatingRepository.findByPlayerId(player.getId());
            for (PlayerSportRating rating : ratings) {
                if (rating.getFormat() == Format.DOUBLE) {
                    doublesRatings.put(player.getId(), rating);
                    break;
                }
            }
        }

        // Get all matches (could be optimized further if needed)
        List<Match> matches = matchService.findAllMatches();
        // Map teamId to teamPlayers
        Map<Long, List<TeamPlayer>> teamPlayersMap = new HashMap<>();
        for (Match match : matches) {
            if (match.getTeamA() != null && match.getTeamB() != null) {
                if (match.getTeamA().getTeamSize() == 2 && match.getTeamB().getTeamSize() == 2) {
                    if (!teamPlayersMap.containsKey(match.getTeamA().getId())) {
                        teamPlayersMap.put(match.getTeamA().getId(), teamPlayerRepository.findAllByTeamId(match.getTeamA().getId()));
                    }
                    if (!teamPlayersMap.containsKey(match.getTeamB().getId())) {
                        teamPlayersMap.put(match.getTeamB().getId(), teamPlayerRepository.findAllByTeamId(match.getTeamB().getId()));
                    }
                }
            }
        }
        // For each player, count games played, wins, losses
        Map<Long, Integer> gamesPlayed = new HashMap<>();
        Map<Long, Integer> wins = new HashMap<>();
        Map<Long, Integer> losses = new HashMap<>();
        for (Match match : matches) {
            if (match.getTeamA() != null && match.getTeamB() != null) {
                if (match.getTeamA().getTeamSize() == 2 && match.getTeamB().getTeamSize() == 2) {
                    List<TeamPlayer> teamAPlayers = teamPlayersMap.get(match.getTeamA().getId());
                    List<TeamPlayer> teamBPlayers = teamPlayersMap.get(match.getTeamB().getId());
                    if (teamAPlayers != null && teamBPlayers != null && teamAPlayers.size() == 2 && teamBPlayers.size() == 2) {
                        for (TeamPlayer tp : teamAPlayers) {
                            gamesPlayed.put(tp.getPlayer().getId(), gamesPlayed.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            if (match.isTeamAWin()) {
                                wins.put(tp.getPlayer().getId(), wins.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            } else {
                                losses.put(tp.getPlayer().getId(), losses.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            }
                        }
                        for (TeamPlayer tp : teamBPlayers) {
                            gamesPlayed.put(tp.getPlayer().getId(), gamesPlayed.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            if (match.isTeamBWin()) {
                                wins.put(tp.getPlayer().getId(), wins.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            } else {
                                losses.put(tp.getPlayer().getId(), losses.getOrDefault(tp.getPlayer().getId(), 0) + 1);
                            }
                        }
                    }
                }
            }
        }
        // Build DTOs for paged players
        for (Player player : players) {
            PlayerDoublesStatsDto dto = new PlayerDoublesStatsDto();
            dto.setPlayerId(player.getId());
            dto.setPlayerName(player.getName());
            PlayerSportRating rating = doublesRatings.get(player.getId());
            dto.setDoublesRanking(rating != null ? rating.getRateScore() : null);
            int played = gamesPlayed.getOrDefault(player.getId(), 0);
            int win = wins.getOrDefault(player.getId(), 0);
            int loss = losses.getOrDefault(player.getId(), 0);
            dto.setGamesPlayed(played);
            dto.setWins(win);
            dto.setLosses(loss);
            dto.setRecord(win + "-" + loss);
            result.add(dto);
        }
        return PageUtil.toPage(result, page.getTotalElements());
    }

    /**
     * Get all events (completed) for a player, with matches and net rating change for each event
     */
    public List<PlayerEventSummaryDto> getAllEventsWithResultsAndRatingChange(Long playerId) {
        var events = eventRepository.getPlayerCompletedEvents(playerId);
        return events.stream().map(event -> {
            var eventDto = eventMapper.toDto(event);
            var matches = matchRepository.findByEventIdAndPlayerId(event.getId(), playerId)
                    .stream()
                    .map(matchMapper::toDto)
                    .toList();
            var ratingChanges = ratingHistoryRepository.findByPlayerIdAndEventIdOrderByCreateTimeDesc(playerId, event.getId())
                    .stream()
                    .map(ratingHistoryMapper::toDto)
                    .toList();
            double netChange = 0.0;
            if (!ratingChanges.isEmpty()) {
                netChange = ratingChanges.stream()
                        .mapToDouble(dto -> dto.getChanges() != null ? dto.getChanges() : 0.0)
                        .sum();
            }
            return new PlayerEventSummaryDto()
                    .setEvent(eventDto)
                    .setMatches(matches)
                    .setNetRatingChange(netChange);
        }).toList();
    }

    /**
     * Get the last event (completed) for a player, with matches and net rating change
     */
    public PlayerEventSummaryDto getLastEventWithResultsAndRatingChange(Long playerId) {
        var lastEvent = eventRepository.getPlayerLastEvent(playerId)
                .orElse(null);
        if (lastEvent == null) return null;
        var eventDto = eventMapper.toDto(lastEvent);
        var matches = matchRepository.findByEventIdAndPlayerId(lastEvent.getId(), playerId)
                .stream()
                .map(matchMapper::toDto)
                .toList();
        var ratingChanges = ratingHistoryRepository.findByPlayerIdAndEventIdOrderByCreateTimeDesc(playerId, lastEvent.getId())
                .stream()
                .map(ratingHistoryMapper::toDto)
                .toList();
        double netChange = 0.0;
        if (!ratingChanges.isEmpty()) {
            netChange = ratingChanges.stream()
                    .mapToDouble(dto -> dto.getChanges() != null ? dto.getChanges() : 0.0)
                    .sum();
        }
        return new PlayerEventSummaryDto()
                .setEvent(eventDto)
                .setMatches(matches)
                .setNetRatingChange(netChange);
    }
}