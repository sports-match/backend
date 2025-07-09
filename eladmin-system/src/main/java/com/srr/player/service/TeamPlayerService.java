/*
 *  Copyright 2019-2025 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.enumeration.TeamPlayerStatus;
import com.srr.enumeration.TeamStatus;
import com.srr.event.domain.Event;
import com.srr.event.dto.EventActionDTO;
import com.srr.player.domain.Team;
import com.srr.player.domain.TeamPlayer;
import com.srr.player.dto.PlayerDto;
import com.srr.player.dto.TeamPlayerDto;
import com.srr.player.dto.TeamPlayerReassignDto;
import com.srr.player.mapper.PlayerMapper;
import com.srr.player.mapper.TeamPlayerMapper;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.TeamPlayerRepository;
import com.srr.player.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.service.enums.UserType;
import me.zhengjie.modules.system.service.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.zhengjie.modules.security.service.SecurityContextUtils.getCurrentUser;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Service
@RequiredArgsConstructor
public class TeamPlayerService {

    private final TeamPlayerRepository teamPlayerRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerMapper teamPlayerMapper;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final PlayerMapper playerMapper;

    /**
     * Get TeamPlayer by id
     *
     * @param id ID of TeamPlayer
     * @return TeamPlayerDto
     */
    @Transactional(readOnly = true)
    public TeamPlayerDto findById(Long id) {
        TeamPlayer teamPlayer = teamPlayerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(TeamPlayer.class, "id", id.toString()));
        return teamPlayerMapper.toDto(teamPlayer);
    }

    /**
     * Check in TeamPlayer
     *
     * @param id ID of TeamPlayer
     * @return TeamPlayerDto
     */
    @Transactional
    public TeamPlayerDto checkIn(Long id, boolean isOrganizerOrAdmin) {
        TeamPlayer teamPlayer = teamPlayerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(TeamPlayer.class, "id", id.toString()));

        // Only allow check-in if event status is CHECK_IN
        Team team = teamPlayer.getTeam();
        if (team == null || team.getEvent() == null || team.getEvent().getStatus() != com.srr.enumeration.EventStatus.CHECK_IN) {
            throw new BadRequestException("Check-in is only allowed when the event status is CHECK_IN.");
        }

        if (teamPlayer.isCheckedIn() && !isOrganizerOrAdmin) {
            throw new BadRequestException("Player is already checked in");
        }

        teamPlayer.setCheckedIn(true);
        teamPlayer.setStatus(TeamPlayerStatus.CHECKED_IN);
        teamPlayer.setCheckInTime(new Timestamp(System.currentTimeMillis()));
        teamPlayerRepository.save(teamPlayer);

        // Use new helper for team update
        updateTeamStateAndStatus(teamPlayer.getTeam());

        return teamPlayerMapper.toDto(teamPlayer);
    }

    /**
     * Check in TeamPlayer for event
     *
     * @param eventId ID of event
     * @param request The request to check in
     * @return TeamPlayerDto
     */
    @Transactional
    public TeamPlayerDto checkInForEvent(final Long eventId, final EventActionDTO request) {
        TeamPlayer teamPlayer = teamPlayerRepository.findByEventIdAndPlayerId(eventId, request.playerId());
        if (teamPlayer == null) {
            throw new BadRequestException("You haven't registered for this event.");
        }

        // Check-in validation for organizer in double format events
        final UserDto currentUser = getCurrentUser();
        final boolean isOrganizerOrAdmin = currentUser != null && (UserType.ORGANIZER.equals(currentUser.getUserType())
                || UserType.ADMIN.equals(currentUser.getUserType()));
        if (isOrganizerOrAdmin) {
            Team team = teamPlayer.getTeam();
            Event event = team.getEvent();

            if (Format.DOUBLE.equals(event.getFormat()) && team.getTeamSize() != 2) {
                throw new BadRequestException("Cannot check in. A double format team must have exactly 2 players.");
            }

            List<TeamPlayer> teamPlayers = teamPlayerRepository.findByTeamId(team.getId());
            for (TeamPlayer tp : teamPlayers) {
                checkIn(tp.getId(), true);
            }

            return teamPlayerMapper.toDto(teamPlayer);
        }

        return checkIn(teamPlayer.getId(), false);
    }

    @Transactional(readOnly = true)
    public List<TeamPlayerDto> findParticipantsByEventId(Long eventId) {
        List<TeamPlayer> teamPlayers = teamPlayerRepository.findByEventId(eventId);
        return teamPlayers
                .stream()
                .map(teamPlayer -> teamPlayerMapper.toTeamPlayerDto(teamPlayer, playerMapper.toDto(teamPlayer.getPlayer())
                        , teamPlayer.getPlayer().getUser().getEmail(), teamPlayer.getTeam().getEvent().getSportId()))
                .toList();
    }

    /**
     * Get TeamPlayer by event id
     *
     * @param eventId ID of event
     * @return List of TeamPlayerDto
     */
    @Transactional(readOnly = true)
    public List<TeamPlayerDto> findByEventId(Long eventId) {
        List<Team> teams = teamRepository.findByEventId(eventId);
        if (teams == null || teams.isEmpty()) {
            return Collections.emptyList();
        }

        return teams.stream()
                .filter(Objects::nonNull)
                .map(Team::getTeamPlayers)
                .filter(players -> players != null && !players.isEmpty() && players.get(0) != null)
                .map(teamPlayers -> {
                    teamPlayers.sort((p1, p2) -> p2.getId().compareTo(p1.getId()));
                    final TeamPlayer mainTeamPlayer = teamPlayers.get(0);
                    PlayerDto mainPlayerDto = playerMapper.toDto(mainTeamPlayer.getPlayer());
                    final TeamPlayer partnerTeamPlayer = teamPlayers.size() > 1 ? teamPlayers.get(1) : null;
                    PlayerDto partnerDto = (partnerTeamPlayer != null)
                            ? playerMapper.toDto(partnerTeamPlayer.getPlayer())
                            : null;

                    TeamStatus status = teamPlayers.get(0).getTeam().getStatus();
                    final Long sportId = teamPlayers.get(0).getTeam().getEvent().getSportId();
                    return teamPlayerMapper.toDto(mainPlayerDto, partnerDto, status, sportId, mainTeamPlayer.getTeam());
                })
                .sorted((t1, t2) -> { // Rank teams by combine score
                    Double score1 = t1.getCombinedScore() != null ? t1.getCombinedScore() : 0.0;
                    Double score2 = t2.getCombinedScore() != null ? t2.getCombinedScore() : 0.0;
                    return score2.compareTo(score1);
                })
                .collect(Collectors.toList());
    }


    /**
     * Update team averageScore, updateTime, and checked-in/registered status based on current players.
     * If the team has no players, deletes the team.
     *
     * @param team the team to update
     */
    public void updateTeamStateAndStatus(Team team) {
        List<TeamPlayer> players = teamPlayerRepository.findAllByTeamId(team.getId());
        if (players.isEmpty()) {
            teamRepository.delete(team);
            return;
        }
        double avg = players.stream()
                .map(tp -> playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(tp.getPlayer().getId(), team.getEvent().getSportId(), Format.DOUBLE))
                .filter(java.util.Optional::isPresent)
                .mapToDouble(opt -> opt.get().getRateScore() != null ? opt.get().getRateScore() : 0)
                .average().orElse(0.0);
        team.setAverageScore(avg);
        team.setUpdateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        boolean allCheckedIn = players.stream()
                .allMatch(tp -> tp.isCheckedIn() || tp.getStatus() == com.srr.enumeration.TeamPlayerStatus.CHECKED_IN);
        if (allCheckedIn && team.getStatus() != com.srr.enumeration.TeamStatus.CHECKED_IN) {
            team.setStatus(com.srr.enumeration.TeamStatus.CHECKED_IN);
        } else if (!allCheckedIn && team.getStatus() == com.srr.enumeration.TeamStatus.CHECKED_IN) {
            team.setStatus(com.srr.enumeration.TeamStatus.REGISTERED);
        }
        teamRepository.save(team);
    }

    /**
     * Reassign TeamPlayer to new team
     *
     * @param dto TeamPlayerReassignDto
     * @return TeamPlayerDto
     */
    @Transactional
    public TeamPlayerDto reassignPlayer(TeamPlayerReassignDto dto) {
        // Find the team player
        TeamPlayer teamPlayer = teamPlayerRepository.findById(dto.getTeamPlayerId())
                .orElseThrow(() -> new EntityNotFoundException(TeamPlayer.class, "id", dto.getTeamPlayerId().toString()));
        Long playerId = teamPlayer.getPlayer().getId();
        Long targetTeamId = dto.getTargetTeamId();
        Team targetTeam = teamRepository.findById(targetTeamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", targetTeamId.toString()));
        Long eventId = targetTeam.getEvent().getId();

        // Call the centralized logic
        boolean assigned = ensurePlayerInTargetTeam(eventId, playerId, targetTeamId);
        if (!assigned) {
            throw new BadRequestException("Player could not be assigned to target team");
        }
        // Return updated TeamPlayer info
        TeamPlayer updated = teamPlayerRepository.findByTeamIdAndPlayerId(targetTeamId, playerId);
        return teamPlayerMapper.toDto(updated);
    }

    /**
     * Merge teams if needed and return true if player is already in target team after merge.
     */
    private boolean ensurePlayerInTargetTeam(Long eventId, Long joiningPlayerId, Long targetTeamId) {
        // Find the joining player's existing TeamPlayer for this event
        TeamPlayer joiningTeamPlayer = teamPlayerRepository.findByEventId(eventId).stream()
                .filter(tp -> tp.getPlayer().getId().equals(joiningPlayerId))
                .findFirst().orElse(null);
        if (joiningTeamPlayer == null) {
            return false; // player not in any team yet
        }
        Team oldTeam = joiningTeamPlayer.getTeam();
        if (oldTeam.getId().equals(targetTeamId)) {
            throw new BadRequestException("Player is already in the target team");
        }

        // Prevent merge if target team is full
        Team targetTeam = teamRepository.findById(targetTeamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", String.valueOf(targetTeamId)));
        if (targetTeam.getTeamPlayers().size() >= targetTeam.getTeamSize()) {
            throw new BadRequestException("Target team is already full.");
        }
        if (targetTeam.getStatus() == TeamStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot join a withdrawn team.");
        }

        // Prevent merge if player status withdrawn
        if (joiningTeamPlayer.getStatus() == TeamPlayerStatus.WITHDRAWN) {
            throw new BadRequestException("Withdrawn player cannot join a team.");
        }

        // Move the player to the target team
        joiningTeamPlayer.setTeam(targetTeam);
        teamPlayerRepository.save(joiningTeamPlayer);

        // Reload target team to ensure up-to-date teamPlayers
        targetTeam = teamRepository.findById(targetTeamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", String.valueOf(targetTeamId)));

        // Update team timestamp
        targetTeam.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        var event = targetTeam.getEvent();

        // Update averageScore for the team
        double avg = targetTeam.getTeamPlayers().stream()
                .map(tp -> playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(tp.getPlayer().getId(), event.getSportId(), Format.DOUBLE))
                .filter(Optional::isPresent)
                .mapToDouble(opt -> opt.get().getRateScore() != null ? opt.get().getRateScore() : 0)
                .average().orElse(0.0);
        targetTeam.setAverageScore(avg);

        // If all players are checked in, set team status to CHECKED_IN
        boolean allCheckedIn = targetTeam.getTeamPlayers().stream().allMatch(tp -> tp.isCheckedIn() || tp.getStatus() == com.srr.enumeration.TeamPlayerStatus.CHECKED_IN);
        if (allCheckedIn && targetTeam.getStatus() != com.srr.enumeration.TeamStatus.CHECKED_IN) {
            targetTeam.setStatus(com.srr.enumeration.TeamStatus.CHECKED_IN);
        }

        teamRepository.save(targetTeam);

        // If old team is now empty, delete it
        if (teamPlayerRepository.findAllByTeamId(oldTeam.getId()).isEmpty()) {
            teamRepository.deleteById(oldTeam.getId());
        }
        return true;
    }

    /**
     * Withdraw a team from an event. Sets all TeamPlayers to WITHDRAWN and the Team to WITHDRAWN.
     */
    @Transactional
    public void withdrawTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", teamId.toString()));
        team.setStatus(TeamStatus.WITHDRAWN);
        teamRepository.save(team);

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findAllByTeamId(teamId);
        for (TeamPlayer tp : teamPlayers) {
            tp.setStatus(TeamPlayerStatus.WITHDRAWN);
            teamPlayerRepository.save(tp);
        }
    }

    public TeamPlayerStatus getTeamPlayerStatus(Long eventId, Long playerId) {
        TeamPlayer teamPlayer = teamPlayerRepository.findByEventIdAndPlayerId(eventId, playerId);
        if (teamPlayer == null) {
            return TeamPlayerStatus.NOT_REGISTERED;
        }
        return teamPlayer.getStatus();
    }
}
