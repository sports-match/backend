package com.srr.service.impl;

import com.srr.event.domain.Event;
import com.srr.domain.MatchGroup;
import com.srr.domain.Team;
import com.srr.event.listener.MatchGroupCreatedEvent;
import com.srr.event.domain.EventRepository;
import com.srr.repository.MatchGroupRepository;
import com.srr.repository.TeamRepository;
import com.srr.service.MatchGroupService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Service
@RequiredArgsConstructor
public class MatchGroupServiceImpl implements MatchGroupService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final MatchGroupRepository matchGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Integer generateMatchGroups(Long eventId) {
        // Find the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", eventId.toString()));
        
        // Check if the event has a group count
        if (event.getGroupCount() == null || event.getGroupCount() <= 0) {
            throw new BadRequestException("Event has no valid group count defined");
        }
        
        // Get all teams for the event
        List<Team> teams = event.getTeams();
        
        if (teams.isEmpty()) {
            throw new BadRequestException("No teams found for event with ID: " + eventId);
        }
        
        // Clear existing match groups for this event
        List<MatchGroup> existingGroups = event.getMatchGroups();
        if (existingGroups != null && !existingGroups.isEmpty()) {
            for (MatchGroup group : existingGroups) {
                // Detach teams from groups
                for (Team team : group.getTeams()) {
                    team.setMatchGroup(null);
                }
                matchGroupRepository.delete(group);
            }
        }
        
        // Sort teams by their average score (which is now stored on the Team entity)
        List<Team> sortedTeams = teams.stream()
                .sorted(Comparator.comparing(Team::getAverageScore, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        // Group teams based on their sorted order and the target group count
        int targetGroupCount = event.getGroupCount();
        List<List<Team>> teamGroups = createTeamGroups(sortedTeams, targetGroupCount);
        
        // Create match groups
        int groupCount = 0;
        for (List<Team> teamGroup : teamGroups) {
            if (!teamGroup.isEmpty()) {
                createMatchGroup(event, teamGroup, "Group " + (++groupCount), teamGroup.size());
            }
        }
        
        return groupCount;
    }
    
    /**
     * Group teams based strictly on their score order
     */
    private List<List<Team>> createTeamGroups(List<Team> sortedTeams, int targetGroupCount) {
        int totalTeams = sortedTeams.size();
        
        // Don't create more groups than we have teams
        int actualGroupCount = Math.min(targetGroupCount, totalTeams);
        
        // Initialize the groups
        List<List<Team>> groups = new ArrayList<>(actualGroupCount);
        for (int i = 0; i < actualGroupCount; i++) {
            groups.add(new ArrayList<>());
        }
        
        // Distribute teams to groups in a round-robin fashion based on their sorted order
        // Teams with similar scores will naturally be placed in different groups
        for (int i = 0; i < sortedTeams.size(); i++) {
            Team team = sortedTeams.get(i);
            // Use modulo to distribute teams evenly among groups
            int groupIndex = i % actualGroupCount;
            groups.get(groupIndex).add(team);
        }
        
        return groups;
    }
    
    /**
     * Create a match group from a list of teams
     */
    private void createMatchGroup(Event event, List<Team> teams, String name, int groupTeamSize) {
        MatchGroup matchGroup = new MatchGroup();
        matchGroup.setName(name);
        matchGroup.setEvent(event);
        matchGroup.setGroupTeamSize(groupTeamSize);
        
        // Save the match group first
        matchGroup = matchGroupRepository.save(matchGroup);
        
        // Update the teams with the match group
        for (Team team : teams) {
            team.setMatchGroup(matchGroup);
            teamRepository.save(team);
        }
        
        // Publish an event to trigger match generation
        eventPublisher.publishEvent(new MatchGroupCreatedEvent(this, matchGroup));
    }
}
