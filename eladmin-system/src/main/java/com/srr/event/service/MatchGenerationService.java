package com.srr.event.service;

import com.srr.event.domain.Match;
import com.srr.event.domain.MatchGroup;
import com.srr.event.repository.MatchGroupRepository;
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
    private final MatchGroupRepository matchGroupRepository;

    /**
     * Generate matches for an entire event by iterating through its match groups.
     * This method will also clear all existing matches for the event before generation.
     *
     * @param eventId the ID of the event for which to generate matches
     */
    @Transactional
    public void generateMatchesForEvent(Long eventId) {
        // Clear all existing matches for the event to ensure a clean slate
        matchRepository.deleteByMatchGroupEventId(eventId);

        // Find all match groups for the event
        List<MatchGroup> matchGroups = matchGroupRepository.findAllByEventId(eventId);

        if (matchGroups.isEmpty()) {
            log.warn("No match groups found for event {}, cannot generate matches.", eventId);
            return;
        }

        int totalMatchesGenerated = 0;
        // Generate matches for each group
        for (MatchGroup group : matchGroups) {
            totalMatchesGenerated += generateMatchesForGroup(group);
        }

        log.info("Successfully generated a total of {} matches for event {}.", totalMatchesGenerated, eventId);
    }

    /**
     * Generate matches for a single match group.
     *
     * @param matchGroup the match group for which to generate matches
     * @return the number of matches generated
     */
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
        // Note: This is now redundant if called from generateMatchesForEvent, but kept for standalone use
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
