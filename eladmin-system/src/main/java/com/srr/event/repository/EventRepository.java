
package com.srr.event.repository;

import com.srr.enumeration.EventStatus;
import com.srr.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * @author Chanheng
 * @date 2025-05-18
 **/
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByStatusAndEventTimeLessThan(EventStatus status, Timestamp eventTimeIsLessThan);

    List<Event> findAllByStatusAndCheckInStartIsNotNullAndCheckInStartLessThan(EventStatus status, Timestamp eventTimeIsLessThan);

    @Query(value = """
            select * from event e join team t on e.id = t.event_id
                         join team_player tp on t.id = tp.team_id and tp.player_id = :playerId 
                                                                and date(e.event_time) = CURDATE() order by e.id desc limit 1
            """, nativeQuery = true)
    Optional<Event> getPlayerEventToday(Long playerId);

    @Query(value = """
            select * from event e join team t on e.id = t.event_id
                         join team_player tp on t.id = tp.team_id and tp.player_id = :playerId 
                                 and e.status = 'COMPLETED' order by e.id desc
            """, nativeQuery = true)
    List<Event> getPlayerCompletedEvents(Long playerId);

    @Query(value = """
            select * from event e join team t on e.id = t.event_id
                         join team_player tp on t.id = tp.team_id and tp.player_id = :playerId 
                                                                and date(e.event_time) > CURDATE() order by e.id desc
            """, nativeQuery = true)
    LinkedList<Event> getPlayerUpcomingEvents(Long playerId);

    @Query(value = """
            select * from event e join team t on e.id = t.event_id
                         join team_player tp on t.id = tp.team_id and tp.player_id = :playerId 
                                 and e.status = 'COMPLETED' order by e.id desc limit 1
    """, nativeQuery = true)
    Optional<Event> getPlayerLastEvent(Long playerId);
}