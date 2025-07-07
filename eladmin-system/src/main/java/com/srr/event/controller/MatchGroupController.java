package com.srr.event.controller;

import com.srr.event.service.MatchGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match-groups")
@Api(tags = "Match Group Management")
public class MatchGroupController {

    private final MatchGroupService matchGroupService;

    @PostMapping("/{id}/courts")
    @ApiOperation("Update court numbers for a match group")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Object> updateCourtNumbers(@PathVariable Long id, @RequestParam String courtNumber) {
        matchGroupService.updateCourtNumbers(id, courtNumber);
        final var map = new HashMap<String, Object>();
        map.put("eventId", id);
        map.put("message", "Successfully updated court groups");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/move-team")
    @ApiOperation("Move a team from one group to another")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Void> moveTeamToGroup(@RequestParam Long teamId, @RequestParam Long targetGroupId) {
        matchGroupService.moveTeamToGroup(teamId, targetGroupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
