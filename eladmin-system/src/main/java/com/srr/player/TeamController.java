package com.srr.player;

import com.srr.player.dto.TeamDto;
import com.srr.player.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Chanheng
 * @date 2025-05-30
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "Team Management")
@RequestMapping("/api")
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/teams/{id}")
    @ApiOperation("Get team details")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<TeamDto> getTeam(@PathVariable Long id) {
        return new ResponseEntity<>(teamService.findById(id), HttpStatus.OK);
    }

    @GetMapping("/events/{eventId}/teams")
    @ApiOperation("Get all teams for an event")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<List<TeamDto>> getTeamsByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(teamService.findByEventId(eventId), HttpStatus.OK);
    }
}
