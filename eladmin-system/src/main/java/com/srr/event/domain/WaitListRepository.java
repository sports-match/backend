package com.srr.event.domain;

import com.srr.enumeration.WaitListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
public interface WaitListRepository extends JpaRepository<WaitList, Long>, JpaSpecificationExecutor<WaitList> {
    
    /**
     * Find all wait list entries for an event
     * @param eventId The event ID
     * @return List of wait list entries
     */
    List<WaitList> findByEventId(Long eventId);
    
    /**
     * Find all wait list entries for an event (alias for findByEventId)
     * @param eventId The event ID
     * @return List of wait list entries
     */
    List<WaitList> findAllByEventId(Long eventId);
    
    /**
     * Find all wait list entries for a player
     * @param playerId The player ID
     * @return List of wait list entries
     */
    List<WaitList> findByPlayerId(Long playerId);


    /**
     * Delete all wait list entries for a specific event.
     * @param eventId The event ID.
     */
    void deleteByEventId(Long eventId);

    /**
     * Find wait list entry for a specific player and event
     * @param eventId The event ID
     * @param playerId The player ID
     * @return WaitList entry if exists
     */
    WaitList findByEventIdAndPlayerId(Long eventId, Long playerId);
    
    /**
     * Find wait list entries by status
     * @param status The status (WAITING, PROMOTED, CANCELLED, EXPIRED)
     * @return List of wait list entries
     */
    List<WaitList> findByStatus(WaitListStatus status);
    
    /**
     * Count wait list entries for an event
     * @param eventId The event ID
     * @return Count of wait list entries
     */
    long countByEventId(Long eventId);
}
