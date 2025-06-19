package com.srr.player.repository;

import com.srr.player.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
* @website https://eladmin.vip
* @author Chanheng
* @date 2025-05-25
**/
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {
    /**
     * Find all teams for a specific event
     * @param eventId ID of the event
     * @return List of teams
     */
    @Query("SELECT t FROM Team t WHERE t.event.id = :eventId")
    List<Team> findByEventId(@Param("eventId") Long eventId);
    
    /**
     * Find all teams for a specific event (alias for findByEventId)
     * @param eventId ID of the event
     * @return List of teams
     */
    List<Team> findAllByEventId(Long eventId);

    /**
     * Delete all teams for a specific event.
     * @param eventId The event ID.
     */
    @Modifying
    @Query("DELETE FROM Team t WHERE t.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}
