package com.srr.event.service;

import com.srr.event.domain.Event;
import com.srr.event.domain.MatchGroup;
import com.srr.event.listener.MatchGroupCreatedEvent;
import com.srr.event.repository.EventRepository;
import com.srr.event.repository.MatchGroupRepository;
import com.srr.player.domain.Team;
import com.srr.player.repository.TeamRepository;
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
public class MatchGroupService {

    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final MatchGroupRepository matchGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    
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
        
        // Filter to only checked-in teams
        List<Team> checkedInTeams = teams.stream()
            .filter(t -> t.getStatus() == com.srr.enumeration.TeamStatus.CHECKED_IN)
            .collect(Collectors.toList());
        // Find teams that are REGISTERED (not withdrawn, not checked-in)
        List<Team> notCheckedInTeams = teams.stream()
            .filter(t -> t.getStatus() == com.srr.enumeration.TeamStatus.REGISTERED)
            .collect(Collectors.toList());
        if (!notCheckedInTeams.isEmpty()) {
            //String notCheckedInNames = notCheckedInTeams.stream().map(Team::getName).collect(Collectors.joining(", "));
            throw new BadRequestException("The teams are registered but not checked in. All teams must be checked in before group formation.");
        }
        if (checkedInTeams.isEmpty()) {
            throw new BadRequestException("No checked-in teams found for event with ID: " + eventId);
        }
        // Sort checked-in teams by their average score (descending, nulls last)
        List<Team> sortedTeams = checkedInTeams.stream()
                .sorted(Comparator.comparing(Team::getAverageScore, Comparator.nullsLast(Comparator.reverseOrder())))
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
     * Group teams by rating: highest rated teams in group 1, next highest in group 2, etc.
     */
    private List<List<Team>> createTeamGroups(List<Team> sortedTeams, int targetGroupCount) {
        int totalTeams = sortedTeams.size();
        int actualGroupCount = Math.min(targetGroupCount, totalTeams);
        List<List<Team>> groups = new ArrayList<>(actualGroupCount);
        for (int i = 0; i < actualGroupCount; i++) {
            groups.add(new ArrayList<>());
        }
        // Fill groups sequentially: group 1 gets top N/targetGroupCount, etc.
        for (int i = 0; i < totalTeams; i++) {
            int groupIndex = i / (int) Math.ceil((double) totalTeams / actualGroupCount);
            if (groupIndex >= actualGroupCount) groupIndex = actualGroupCount - 1; // last group gets leftovers
            groups.get(groupIndex).add(sortedTeams.get(i));
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
