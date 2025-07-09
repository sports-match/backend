package com.srr.player.repository;

import com.srr.player.domain.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Chanheng
 * @date 2025-05-25
 **/
public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long>, JpaSpecificationExecutor<TeamPlayer> {
    boolean existsByTeamIdAndPlayerId(Long teamId, Long playerId);

    List<TeamPlayer> findByTeamId(Long teamId);

    TeamPlayer findByTeamIdAndPlayerId(Long teamId, Long playerId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN tp.team t WHERE t.event.id = :eventId AND tp.player.id = :playerId")
    TeamPlayer findByEventIdAndPlayerId(@Param("eventId") Long eventId, @Param("playerId") Long playerId);

    @Query("SELECT tp FROM TeamPlayer tp JOIN tp.team t JOIN t.event e WHERE e.id = :eventId")
    List<TeamPlayer> findByEventId(@Param("eventId") Long eventId);

    /**
     * Find all team player entries for a specific player by user ID
     *
     * @param eventId   The event that players has registered to
     * @param playerIds List of player IDs who has registered to the event
     * @return List of team player entries
     */
    @Query("SELECT tp FROM TeamPlayer tp " +
            "JOIN tp.team t JOIN t.event e " +
            "WHERE e.id = :eventId AND tp.isCheckedIn = false AND " +
            "(:allPlayers = true OR tp.player.id IN :playerIds)")
    List<TeamPlayer> findByEventIdAndPlayerIdsOrAllPlayers(
            @Param("eventId") Long eventId,
            @Param("playerIds") Set<Long> playerIds,
            @Param("allPlayers") boolean allPlayers
    );

    /**
     * Find all team player entries for a specific player by user ID
     *
     * @param userId User's ID
     * @return List of team player entries
     */
    @Query("SELECT tp FROM TeamPlayer tp JOIN tp.player p WHERE p.user.id = :userId")
    List<TeamPlayer> findByUserId(@Param("userId") Long userId);

    /**
     * Find team player entries by team IDs
     *
     * @param teamIds List of team IDs
     * @return List of team player entries
     */
    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id IN :teamIds")
    List<TeamPlayer> findByTeamIdIn(@Param("teamIds") List<Long> teamIds);

    /**
     * Find all team player entries for a specific team
     *
     * @param teamId Team ID
     * @return List of team player entries
     */
    List<TeamPlayer> findAllByTeamId(Long teamId);

    /**
     * Delete all team players associated with teams belonging to a specific event.
     *
     * @param eventId The event ID.
     */
    @Modifying
    @Query(value = "DELETE FROM team_player " +
            "WHERE team_id IN (SELECT id FROM team WHERE event_id = :eventId)", nativeQuery = true)
    void deleteByTeamEventId(@Param("eventId") Long eventId);
}
