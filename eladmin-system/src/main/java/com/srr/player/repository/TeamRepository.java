package com.srr.player.repository;

import com.srr.player.domain.Team;
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
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {
    @Query("SELECT COUNT(t) FROM Team t WHERE t.matchGroup.id = :groupId AND t.event.id = :eventId")
    int countTeamByGroupIdAndEventId(@Param("groupId") Long groupId, @Param("eventId") Long eventId);

    /**
     * Find all teams for a specific event
     *
     * @param eventId ID of the event
     * @return List of teams
     */
    @Query("SELECT t FROM Team t WHERE t.event.id = :eventId")
    List<Team> findByEventId(@Param("eventId") Long eventId);

    /**
     * Find all teams for a specific event (alias for findByEventId)
     *
     * @param eventId ID of the event
     * @return List of teams
     */
    List<Team> findAllByEventId(Long eventId);

    @Query(value = "select 1 from team_player tp join team t on tp.team_id where t.event_id = :eventId and tp.player_id = :playerId",
            nativeQuery = true)
    Integer checkIsJoined(@Param("eventId") Long eventId, @Param("playerId") Long playerId);

    /**
     * Delete all teams for a specific event.
     *
     * @param eventId The event ID.
     */
    @Modifying
    @Query("DELETE FROM Team t WHERE t.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);


    /**
     * Find all teams for a specific event and match group
     *
     * @param eventId      ID of the event
     * @param matchGroupId ID of the match group
     * @return List of teams
     */
    @Query("SELECT t FROM Team t WHERE t.event.id = :eventId AND t.matchGroup.id = :matchGroupId ORDER BY t.id ASC")
    List<Team> findByEventIdAndMatchGroupId(Long eventId, Long matchGroupId);
}
