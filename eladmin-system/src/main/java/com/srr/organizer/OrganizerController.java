package com.srr.organizer;

import com.srr.club.domain.Club;
import com.srr.organizer.dto.EventOrganizerDto;
import com.srr.organizer.dto.EventOrganizerQueryCriteria;
import com.srr.organizer.service.EventOrganizerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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


    @ApiOperation("Get event organizers for the club")
    @GetMapping("/club")
    @PreAuthorize("hasAnyAuthority('Admin', 'Organizer')")
    public ResponseEntity<List<EventOrganizerDto>> getOrganizersForOtherClubs(EventOrganizerQueryCriteria criteria,
                                                                              Pageable pageable) {
        final List<EventOrganizerDto> organizers = organizerService.findEventOrganizersForOtherClubs(criteria, pageable);
        return new ResponseEntity<>(organizers, HttpStatus.OK);
    }
}
