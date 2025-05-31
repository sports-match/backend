package com.srr.service.impl;

import com.srr.domain.Match;
import com.srr.domain.Team;
import com.srr.domain.TeamPlayer;
import com.srr.dto.MatchScoreUpdateDto;
import com.srr.repository.MatchRepository;
import com.srr.repository.TeamPlayerRepository;
import com.srr.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of match service operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    @Override
    @Transactional
    public Match updateMatchScore(MatchScoreUpdateDto scoreUpdateDto) {
        Match match = findById(scoreUpdateDto.getMatchId());
        
        // Ensure the current user is a player in either Team A or Team B
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("Not authenticated");
        }
        
        List<TeamPlayer> teamPlayers = teamPlayerRepository.findByUserId(currentUserId);
        
        if (teamPlayers.isEmpty()) {
            throw new BadRequestException("No team assignments found for the current player");
        }
        
        // Safely check team IDs, handling potential null values
        boolean isInMatch = false;
        if (match.getTeamA() != null && match.getTeamB() != null) {
            Long teamAId = match.getTeamA().getId();
            Long teamBId = match.getTeamB().getId();
            
            isInMatch = teamPlayers.stream()
                    .filter(tp -> tp.getTeam() != null) // Filter out team players with null teams
                    .anyMatch(tp -> (teamAId.equals(tp.getTeam().getId()) || 
                                    teamBId.equals(tp.getTeam().getId())));
        }
        
        if (!isInMatch) {
            throw new BadRequestException("You can only update scores for matches involving your team");
        }
        
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

    @Override
    @Transactional(readOnly = true)
    public Match findById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Match.class, "id", id.toString()));
    }

    @Override
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

    @Override
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
}
