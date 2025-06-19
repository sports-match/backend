package com.srr.event.service;

import com.srr.event.domain.Match;
import com.srr.event.domain.MatchGroup;
import com.srr.event.repository.MatchRepository;
import com.srr.player.domain.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for generating matches for match groups
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchGenerationService {

    private final MatchRepository matchRepository;

    
    @Transactional
    public int generateMatchesForGroup(MatchGroup matchGroup) {
        // Get all teams in the group
        List<Team> teams = new ArrayList<>(matchGroup.getTeams());
        
        if (teams.size() < 2) {
            log.warn("Not enough teams in match group {} to generate matches", matchGroup.getId());
            return 0;
        }
        
        // Delete any existing matches for this group
        // This is important for idempotency in case matches need to be regenerated
        matchRepository.deleteByMatchGroupId(matchGroup.getId());
        
        int matchCount = 0;
        int matchOrder = 1;
        
        // Generate round-robin matches
        // Each team plays against every other team exactly once
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Team teamA = teams.get(i);
                Team teamB = teams.get(j);
                
                Match match = new Match();
                match.setMatchGroup(matchGroup);
                match.setTeamA(teamA);
                match.setTeamB(teamB);
                match.setScoreA(0);
                match.setScoreB(0);
                match.setTeamAWin(false);
                match.setTeamBWin(false);
                match.setScoreVerified(false);
                match.setMatchOrder(matchOrder++);
                
                matchRepository.save(match);
                matchCount++;
            }
        }
        
        log.info("Generated {} matches for match group {}", matchCount, matchGroup.getId());
        return matchCount;
    }
}
