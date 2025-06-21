package com.srr.player;

import com.srr.player.dto.TeamPlayerDto;
import com.srr.player.dto.TeamPlayerReassignDto;
import com.srr.player.service.TeamPlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "Team Player Management")
@RequestMapping("/api/team-players")
public class TeamPlayerController {

    private final TeamPlayerService teamPlayerService;

    @GetMapping("/{id}")
    @ApiOperation("Get team player details")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<TeamPlayerDto> getTeamPlayer(@PathVariable Long id) {
        return new ResponseEntity<>(teamPlayerService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/check-in")
    @Log("Check in player")
    @ApiOperation("Check in player for an event")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<TeamPlayerDto> checkIn(@PathVariable Long id) {
        return new ResponseEntity<>(teamPlayerService.checkIn(id), HttpStatus.OK);
    }
    
    @PostMapping("/reassign")
    @Log("Reassign player to another team")
    @ApiOperation("Reassign player to another team")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<TeamPlayerDto> reassignPlayer(@Validated @RequestBody TeamPlayerReassignDto dto) {
        return new ResponseEntity<>(teamPlayerService.reassignPlayer(dto), HttpStatus.OK);
    }

    /**
     * Withdraw a team from an event
     */
    @PostMapping("/teams/{id}/withdraw")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Object> withdrawTeam(@PathVariable Long id) {
        teamPlayerService.withdrawTeam(id);
        return ResponseEntity.ok().build();
    }
}
