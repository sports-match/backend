
package com.srr.event.repository;

import com.srr.enumeration.EventStatus;
import com.srr.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.sql.Timestamp;
import java.util.List;

/**
* @author Chanheng
* @date 2025-05-18
**/
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByStatusAndEventTimeLessThan(EventStatus status, Timestamp eventTimeIsLessThan);

    List<Event> findAllByStatusAndCheckInStartIsNotNullAndCheckInStartLessThan(EventStatus status, Timestamp eventTimeIsLessThan);

    List<Event> findAllByStatusAndCheckInEndIsNotNullAndCheckInStartLessThan(EventStatus status, Timestamp eventTimeIsLessThan);
}