package com.srr.player.repository;

import com.srr.player.domain.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
* @author Chanheng
* @date 2025-05-25
**/
public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long>, JpaSpecificationExecutor<TeamPlayer> {
    boolean existsByTeamIdAndPlayerId(Long teamId, Long playerId);
    
    TeamPlayer findByTeamIdAndPlayerId(Long teamId, Long playerId);
    
    @Query("SELECT tp FROM TeamPlayer tp JOIN tp.team t JOIN t.event e WHERE e.id = :eventId")
    List<TeamPlayer> findByEventId(@Param("eventId") Long eventId);
    
    /**
     * Find all team player entries for a specific player by user ID
     * @param userId User's ID
     * @return List of team player entries
     */
    @Query("SELECT tp FROM TeamPlayer tp JOIN tp.player p WHERE p.userId = :userId")
    List<TeamPlayer> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find team player entries by team IDs
     * @param teamIds List of team IDs
     * @return List of team player entries
     */
    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id IN :teamIds")
    List<TeamPlayer> findByTeamIdIn(@Param("teamIds") List<Long> teamIds);
    
    /**
     * Find all team player entries for a specific team
     * @param teamId Team ID
     * @return List of team player entries
     */
    List<TeamPlayer> findAllByTeamId(Long teamId);

    /**
     * Delete all team players associated with teams belonging to a specific event.
     * @param eventId The event ID.
     */
    @Modifying
    @Query("DELETE FROM TeamPlayer tp WHERE tp.team.event.id = :eventId")
    void deleteByTeamEventId(@Param("eventId") Long eventId);
}
