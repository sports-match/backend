package com.srr.event.service;

import com.srr.event.domain.Event;
import com.srr.event.dto.EventDto;
import com.srr.dto.EventQueryCriteria;
import com.srr.event.dto.JoinEventDto;
import com.srr.dto.RemindDto;
import com.srr.enumeration.EventStatus;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Chanheng
 * @description Service Interface
 * @date 2025-05-18
 **/
public interface EventService {

    /**
     * Query data with pagination
     *
     * @param criteria criteria
     * @param pageable pagination parameters
     * @return Map<String, Object>
     */
    PageResult<EventDto> queryAll(EventQueryCriteria criteria, Pageable pageable);

    /**
     * Query all data without pagination
     *
     * @param criteria criteria parameters
     * @return List<EventDto>
     */
    List<EventDto> queryAll(EventQueryCriteria criteria);

    /**
     * Query by ID
     *
     * @param id ID
     * @return EventDto
     */
    EventDto findById(Long id);

    /**
     * Create
     *
     * @param resources /
     */
    EventDto create(EventDto resources);

    /**
     * Edit
     *
     * @param resources /
     */
    EventDto update(Event resources);

    EventDto updateStatus(Long id, EventStatus status);

    /**
     * Join an event
     *
     * @param joinEventDto Data for joining an event
     * @return Updated event data
     */
    EventDto joinEvent(JoinEventDto joinEventDto);

    /**
     * Delete multiple events
     *
     * @param ids Array of event IDs to delete
     * @return ExecutionResult containing information about the deleted entities
     */
    ExecutionResult deleteAll(Long[] ids);

    ExecutionResult remind(Long id, RemindDto remindDto);
}