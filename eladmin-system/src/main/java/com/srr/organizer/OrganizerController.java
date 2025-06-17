package com.srr.organizer;

import com.srr.club.domain.Club;
import com.srr.service.EventOrganizerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Api(tags = "Organizer Management")
@RequestMapping("/api/organizers")
public class OrganizerController {

    private final EventOrganizerService organizerService;

    @ApiOperation("Link clubs to organizer")
    @PostMapping("/{organizerId}/clubs")
    @PreAuthorize("hasAnyAuthority('Organizer')")
    public ResponseEntity<?> linkClubsToOrganizer(
            @PathVariable Long organizerId,
            @RequestBody List<Long> clubIds) {
        organizerService.linkClubs(organizerId, clubIds);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get clubs for organizer")
    @GetMapping("/{organizerId}/clubs")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Set<Club>> getClubsForOrganizer(@PathVariable Long organizerId) {
        Set<Club> clubs = organizerService.getClubs(organizerId);
        return ResponseEntity.ok(clubs);
    }
}
