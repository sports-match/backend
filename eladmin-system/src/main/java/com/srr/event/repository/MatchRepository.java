package com.srr.event.repository;

import com.srr.event.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Chanheng
 * @date 2025-05-25
 **/
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    /**
     * Delete all matches associated with a specific match group
     *
     * @param matchGroupId ID of the match group
     */
    @Modifying
    @Query(value = "DELETE FROM `event_match` m WHERE m.match_group_id = :matchGroupId", nativeQuery = true)
    void deleteByMatchGroupId(@Param("matchGroupId") Long matchGroupId);

    /**
     * Delete all matches associated with a given event ID by joining through the MatchGroup
     *
     * @param eventId ID of the event
     */
    @Modifying
    @Query(value = "DELETE FROM `event_match` where match_group_id in (select id from match_group where event_id = 1)", nativeQuery = true)
    void deleteByMatchGroupEventId(@Param("eventId") Long eventId);

    @Query(value = """
            SELECT *
            FROM event_match em
                     LEFT JOIN team ta ON em.team_a_id = ta.id
                     LEFT JOIN team tb ON em.team_b_id = tb.id
                     LEFT JOIN team_player tpa ON ta.id = tpa.team_id
                     LEFT JOIN team_player tpb ON tb.id = tpb.team_id
            WHERE tpa.player_id = :playerId
               OR tpb.player_id = :playerId
            """, nativeQuery = true)
    Match getByPlayerId(@Param("playerId") Long playerId);

    /**
     * Find all matches where the specified teams are either team A or team B
     *
     * @param teamAIds Set of team IDs to match against team A
     * @param teamBIds Set of team IDs to match against team B
     * @return List of matches involving any of the specified teams
     */
    @Query("SELECT m FROM Match m WHERE m.teamA.id IN :teamAIds OR m.teamB.id IN :teamBIds ORDER BY m.matchOrder ASC")
    List<Match> findByTeamAIdInOrTeamBIdIn(@Param("teamAIds") Set<Long> teamAIds, @Param("teamBIds") Set<Long> teamBIds);

    /**
     * Find all matches for a specific match group
     *
     * @param matchGroupId ID of the match group
     * @return List of matches
     */
    List<Match> findAllByMatchGroupId(Long matchGroupId);

    /**
     * Find all matches for a specific match group, ordered by match order
     *
     * @param matchGroupId ID of the match group
     * @return List of matches
     */
    List<Match> findAllByMatchGroupIdOrderByMatchOrderAsc(Long matchGroupId);

    /**
     * Find all matches for a given event ID.
     *
     * @param eventId ID of the event.
     * @return List of matches for the event.
     */
    List<Match> findByMatchGroupEventId(Long eventId);

    @Query("SELECT m FROM Match m WHERE m.matchGroup.event.id = :eventId AND (m.teamA.id IN (SELECT tp.team.id FROM TeamPlayer tp WHERE tp.player.id = :playerId) OR m.teamB.id IN (SELECT tp.team.id FROM TeamPlayer tp WHERE tp.player.id = :playerId))")
    List<Match> findByEventIdAndPlayerId(Long eventId, Long playerId);
}
