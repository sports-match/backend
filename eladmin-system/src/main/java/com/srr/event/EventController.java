package com.srr.event;

import com.srr.enumeration.EventStatus;
import com.srr.enumeration.TeamPlayerStatus;
import com.srr.event.dto.*;
import com.srr.event.service.EventService;
import com.srr.event.service.MatchGenerationService;
import com.srr.event.service.MatchGroupService;
import com.srr.event.service.MatchService;
import com.srr.player.dto.TeamPlayerDto;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.RatingHistoryRepository;
import com.srr.player.service.TeamPlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.annotation.rest.AnonymousGetMapping;
import me.zhengjie.modules.security.service.SecurityContextUtils;
import me.zhengjie.modules.security.service.enums.UserType;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chanheng
 * @date 2025-05-18
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "Event Management")
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final TeamPlayerService teamPlayerService;
    private final MatchGroupService matchGroupService;
    private final MatchGenerationService matchGenerationService;
    private final PlayerRepository playerRepository;
    private final MatchService matchService;
    private final RatingHistoryRepository ratingHistoryRepository;

    @GetMapping
    @ApiOperation("Query event")
    @AnonymousGetMapping
    public ResponseEntity<PageResult<EventDto>> queryEvent(EventQueryCriteria criteria, Pageable pageable) {
        final var result = eventService.queryAll(criteria, pageable);
        String token = SecurityUtils.getToken();
        if (token != null && !token.isBlank() && SecurityContextUtils.currentUserIsNotNull()) {
            var user = SecurityContextUtils.getCurrentUser();
            if (user == null) {
                throw new RuntimeException("Current user is not found.");
            }

            setPlayerMatchInformation(user, result, criteria.getPlayerId());
        } else {
            for (var event : result.getContent()) {
                event.setPlayerStatus(TeamPlayerStatus.NOT_REGISTERED);
            }
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get event by ID")
    @AnonymousGetMapping
    public ResponseEntity<EventDto> getById(@PathVariable Long id) {
        final var event = eventService.findById(id);
        String token = SecurityUtils.getToken();
        if (token != null && !token.isBlank() && SecurityContextUtils.currentUserIsNotNull()) {
            var user = SecurityContextUtils.getCurrentUser();
            var player = playerRepository.findByUserId(user.getId());
            if (UserType.PLAYER.equals(user.getUserType()) && player != null) {
                var status = teamPlayerService.getTeamPlayerStatus(event.getId(), player.getId());
                event.setPlayerStatus(status);
            }
        }
        return new ResponseEntity<>(event, HttpStatus.OK);
    }


    @PostMapping
    @Log("Add event")
    @ApiOperation("Add event")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<Object> createEvent(@Validated @RequestBody EventDto resources) {
        final var result = eventService.create(resources);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping
    @Log("Modify event")
    @ApiOperation("Modify event")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<EventDto> update(@Valid @RequestBody EventUpdateDto resources) {
        return ResponseEntity.ok(eventService.update(resources));
    }

    @PatchMapping("/{id}/status/{status}")
    @Log("Update event status")
    @ApiOperation("Update event status")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<Object> updateEventStatus(
            @PathVariable Long id,
            @PathVariable EventStatus status) {
        final var result = eventService.updateStatus(id, status);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{id}/join")
    @Log("Join event")
    @ApiOperation("Join event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> joinEvent(@PathVariable Long id, @RequestBody JoinEventDto joinEventDto) {
        // Ensure ID in path matches ID in DTO
        joinEventDto.setEventId(id);
        final EventDto result = eventService.joinEvent(joinEventDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{id}/withdraw")
    @Log("Withdraw from event")
    @ApiOperation("Withdraw from event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> withdrawFromEvent(@PathVariable Long id, @RequestBody EventActionDTO request) {
        final EventDto result = eventService.withdrawFromEvent(id, request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{id}/check-in")
    @Log("Check-in for event")
    @ApiOperation("Check-in for event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> checkInForEvent(@PathVariable Long id, @RequestBody EventActionDTO request) {
        final TeamPlayerDto result = teamPlayerService.checkInForEvent(id, request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}/players")
    @ApiOperation("Find all team players in an event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<List<TeamPlayerDto>> findEventPlayers(@PathVariable("id") Long eventId) {
        return new ResponseEntity<>(teamPlayerService.findByEventId(eventId), HttpStatus.OK);
    }

    @GetMapping("/{id}/participants")
    @ApiOperation("Find all team participants in an event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<List<TeamPlayerDto>> findEventParticipants(@PathVariable("id") Long eventId) {
        return new ResponseEntity<>(teamPlayerService.findParticipantsByEventId(eventId), HttpStatus.OK);
    }

    @PostMapping("/{id}/generate-groups")
    @Log("Generate match groups")
    @ApiOperation("Generate match groups based on team scores")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<Object> generateMatchGroups(@PathVariable("id") Long id) {
        Integer groupsCreated = matchGroupService.generateMatchGroups(id);

        Map<String, Object> result = new HashMap<>();
        result.put("groupsCreated", groupsCreated);
        result.put("message", "Successfully created " + groupsCreated + " match groups based on team scores");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/{id}/generate-matches")
    @ApiOperation("Generate matches for an event")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<Object> generateMatches(@PathVariable Long id) {
        matchGenerationService.generateMatchesForEvent(id);
        Map<String, Object> result = new HashMap<>();
        result.put("matchesCreated", id);
        result.put("message", "Successfully created matches for each group for event " + id);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/matches")
    @ApiOperation("Get all matches for an event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> getMatchesByGroup(@PathVariable Long id) {
        return new ResponseEntity<>(matchService.findMatchesByEventGrouped(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/groups")
    @ApiOperation("Get all matches for an event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> getEventsGroup(@PathVariable Long id) {
        return new ResponseEntity<>(eventService.findGroup(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/teams/relocate")
    @ApiOperation("Relocate one team from one group to another")
    @PreAuthorize("hasAnyAuthority('Admin', 'Organizer')")
    public ResponseEntity<Object> relocateTeam(@PathVariable Long id, @Validated @RequestBody TeamRelocationDTO request) {
        eventService.relocateTeam(request);
        final var map = new HashMap<String, Object>();
        map.put("eventId", id);
        map.put("message", "Successfully relocated the team");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/{id}/groups/finalized")
    @ApiOperation("Finalize all groups for the event")
    @PreAuthorize("hasAnyAuthority('Admin', 'Organizer')")
    public ResponseEntity<Object> finalizeGroup(@PathVariable Long id) {
        eventService.finalizedGroup(id);
        final var map = new HashMap<String, Object>();
        map.put("eventId", id);
        map.put("message", "Successfully finalized groups");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping(value = "/{eventId}/groups/{groupId}/matches")
    @ApiOperation("Get all matches for a specific match group in an event")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> getMatchesByGroupId(@PathVariable Long eventId, @PathVariable Long groupId) {
        return new ResponseEntity<>(matchService.findMatchesByGroupId(groupId), HttpStatus.OK);
    }

    @DeleteMapping
    @Log("Delete event")
    @ApiOperation("Delete event")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Object> deleteEvent(@RequestBody Long[] ids) {
        final var result = eventService.deleteAll(ids);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Log("Delete event")
    @ApiOperation("Delete event")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Object> deleteEventById(@PathVariable Long id) {
        final var result = eventService.deleteById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping("/{id}/remind")
    @Log("Send reminder for event")
    @ApiOperation("Send reminder for event")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Object> remind(@PathVariable Long id, @RequestBody @Validated RemindDto remindDto) {
        final var result = eventService.remind(id, remindDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private void setPlayerMatchInformation(UserDto user, PageResult<EventDto> result, Long playerId) {
        var player = playerRepository.findByUserId(user.getId());
        if (player != null && UserType.PLAYER.equals(user.getUserType())) {
            for (var event : result.getContent()) {
                var status = teamPlayerService.getTeamPlayerStatus(event.getId(), player.getId());
                event.setPlayerStatus(status);

                // Set rating and match result to only the player
                if (playerId != null) {
                    final List<MatchDto> matches = matchService.findMatchesByEventGrouped(event.getId());
                    int wins = 0;
                    int losses = 0;
                    double initialRating = 0;
                    double finalRating = 0;
                    List<MatchDto> playerMatches = new ArrayList<>();
                    var index = 0;

                    // Check if the player is in either team
                    for (MatchDto match : matches) {
                        boolean isInTeamA = match.getTeamA().getTeamPlayers().stream()
                                .anyMatch(tp -> tp.getPlayer().getId().equals(player.getId()));
                        boolean isInTeamB = !isInTeamA && match.getTeamB().getTeamPlayers().stream()
                                .anyMatch(tp -> tp.getPlayer().getId().equals(player.getId()));

                        if (match.getTeamAWin() != null) {

                            // Set rating changes for current player
                            if (isInTeamA || isInTeamB) {
                                final var matchRatingHistory = ratingHistoryRepository.findByPlayerIdAndMatchId(player.getId(), match.getId());
                                if (index == 0 && matchRatingHistory != null) {
                                    initialRating = matchRatingHistory.getRateScore() - matchRatingHistory.getChanges();
                                }
                                if (matchRatingHistory != null) {
                                    finalRating = matchRatingHistory.getRateScore();
                                }
                                index++;
                            }

                            playerMatches.add(match);
                            // Count match wins and loses
                            if ((isInTeamA && match.getTeamAWin()) || (isInTeamB && !match.getTeamAWin())) {
                                wins++;
                            } else if (isInTeamA || isInTeamB) {
                                losses++;
                            }
                        }
                    }
                    event.setInitialRating(initialRating);
                    event.setFinalRating(finalRating);
                    event.setWins(wins);
                    event.setLoses(losses);
                    event.setMatches(playerMatches);
                }


            }
        }
    }
}