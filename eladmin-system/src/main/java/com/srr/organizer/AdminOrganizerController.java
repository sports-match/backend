package com.srr.organizer;

import com.srr.dto.OrganizerVerificationDto;
import com.srr.service.EventOrganizerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.ExecutionResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Admin: Event Organizer Management")
@RestController
@RequestMapping("/api/organizers")
@RequiredArgsConstructor
public class AdminOrganizerController {

    private final EventOrganizerService eventOrganizerService;

    @GetMapping
    @ApiOperation("Update Event Organizer Verification Status")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> updateOrganizerVerificationStatus(Pageable pageable) {
        return ResponseEntity.ok(eventOrganizerService.findAll(pageable));
    }

    @PutMapping("/{organizerId}/status")
    @ApiOperation("Update Event Organizer Verification Status")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> updateOrganizerVerificationStatus(
            @PathVariable Long organizerId,
            @Validated @RequestBody OrganizerVerificationDto verificationDto) {
        ExecutionResult result = eventOrganizerService.updateVerificationStatus(organizerId, verificationDto.getStatus());
        if (result.id() != null) {
            return ResponseEntity.ok(result);
        } else {
            // Consider a more specific error response if needed based on ExecutionResult structure
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update organizer status.");
        }
    }
}
