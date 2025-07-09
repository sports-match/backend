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

    /**
     * Generate match groups for an event
     *
     * @param eventId the ID of the event
     * @return the number of groups generated
     */
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
        for (int i = 0; i < teamGroups.size(); i++) {
            List<Team> teamGroup = teamGroups.get(i);
            if (!teamGroup.isEmpty()) {
                String defaultCourts = String.valueOf(i + 1); // e.g., Group 1 gets court "1"
                createMatchGroup(event, teamGroup, "Group " + (i + 1), teamGroup.size(), defaultCourts);
            }
        }

        return teamGroups.size();
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
    private void createMatchGroup(Event event, List<Team> teams, String name, int groupTeamSize, String courtNumbers) {
        MatchGroup matchGroup = new MatchGroup();
        matchGroup.setName(name);
        matchGroup.setEvent(event);
        matchGroup.setGroupTeamSize(groupTeamSize);
        matchGroup.setCourtNumbers(courtNumbers);
        matchGroup = matchGroupRepository.save(matchGroup);
        for (Team team : teams) {
            team.setMatchGroup(matchGroup);
            teamRepository.save(team);
        }
        eventPublisher.publishEvent(new MatchGroupCreatedEvent(this, matchGroup));
    }

    /**
     * Update court numbers for a match group
     *
     * @param matchGroupId the ID of the match group
     * @param courtNumbers the new court numbers
     */
    public void updateCourtNumbers(Long matchGroupId, String courtNumbers) {
        MatchGroup matchGroup = matchGroupRepository.findById(matchGroupId)
                .orElseThrow(() -> new EntityNotFoundException(MatchGroup.class, "id", matchGroupId.toString()));

        final boolean isFinalized = matchGroup.getFinalized();
        if (isFinalized) {
            throw new BadRequestException("Cannot update court numbers for a finalized group.");
        }

        matchGroup.setCourtNumbers(courtNumbers);
        matchGroupRepository.save(matchGroup);
    }

    /**
     * Move a team from its current group to another group, with validations.
     */
    @Transactional
    public void moveTeamToGroup(Long teamId, Long targetGroupId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", teamId.toString()));
        MatchGroup targetGroup = matchGroupRepository.findById(targetGroupId)
                .orElseThrow(() -> new EntityNotFoundException(MatchGroup.class, "id", targetGroupId.toString()));
        MatchGroup currentGroup = team.getMatchGroup();

        if (currentGroup == null) {
            throw new BadRequestException("Team is not currently assigned to any group.");
        }

        if (currentGroup.getId().equals(targetGroupId)) {
            throw new BadRequestException("Team is already in the target group.");
        }
        // Must be same event
        if (!currentGroup.getEvent().getId().equals(targetGroup.getEvent().getId())) {
            throw new BadRequestException("Target group is not in the same event as the team's current group.");
        }
        // Only allow checked-in, not withdrawn teams
        if (team.getStatus() != com.srr.enumeration.TeamStatus.CHECKED_IN) {
            throw new BadRequestException("Only checked-in teams can be moved between groups.");
        }
        team.setMatchGroup(targetGroup);
        teamRepository.save(team);
    }
}
