package com.srr.service;

import com.srr.domain.Club;
import com.srr.domain.EventOrganizer;
import com.srr.enumeration.VerificationStatus;
import me.zhengjie.utils.ExecutionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Service for managing event organizers
 */
public interface EventOrganizerService {
    
    /**
     * Create a new event organizer
     * @param resources The event organizer to create
     * @return Execution result with the created entity's ID
     */
    ExecutionResult create(EventOrganizer resources);

    /**
     * Find event organizers by user ID
     * @param userId The user ID to search for
     * @return List of event organizers associated with the user
     */
    List<EventOrganizer> findByUserId(Long userId);

    Page<EventOrganizer> findAll(Pageable pageable);

    /**
     * Update the verification status of an event organizer.
     * @param organizerId The ID of the event organizer to update.
     * @param status The new verification status.
     * @return ExecutionResult containing the updated entity's ID.
     */
    ExecutionResult updateVerificationStatus(Long organizerId, VerificationStatus status);

    /**
     * Link clubs to an event organizer
     * @param organizerId The ID of the event organizer
     * @param clubIds The IDs of the clubs to link
     */
    void linkClubs(Long organizerId, List<Long> clubIds);

    /**
     * Get the clubs associated with an event organizer
     * @param organizerId The ID of the event organizer
     * @return Set of clubs associated with the event organizer
     */
    Set<Club> getClubs(Long organizerId);
}
