package com.srr.event.controller;

import com.srr.event.service.MatchGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match-groups")
@Api(tags = "Match Group Management")
public class MatchGroupController {

    private final MatchGroupService matchGroupService;

    @PostMapping("/{id}/courts")
    @ApiOperation("Update court numbers for a match group")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Void> updateCourtNumbers(@PathVariable Long id, @RequestParam String courtNumbers) {
        matchGroupService.updateCourtNumbers(id, courtNumbers);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/move-team")
    @ApiOperation("Move a team from one group to another")
    @PreAuthorize("hasAnyAuthority('Organizer', 'Admin')")
    public ResponseEntity<Void> moveTeamToGroup(@RequestParam Long teamId, @RequestParam Long targetGroupId) {
        matchGroupService.moveTeamToGroup(teamId, targetGroupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
