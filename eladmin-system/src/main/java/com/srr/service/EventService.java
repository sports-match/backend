/*
*  Copyright 2019-2025 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.srr.service;

import com.srr.domain.Event;
import com.srr.dto.EventDto;
import com.srr.dto.EventQueryCriteria;
import com.srr.dto.JoinEventDto;
import com.srr.enumeration.EventStatus;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
* @website https://eladmin.vip
* @description Service Interface
* @author Chanheng
* @date 2025-05-18
**/
public interface EventService {

    /**
    * Query data with pagination
    * @param criteria criteria
    * @param pageable pagination parameters
    * @return Map<String,Object>
    */
    PageResult<EventDto> queryAll(EventQueryCriteria criteria, Pageable pageable);

    /**
    * Query all data without pagination
    * @param criteria criteria parameters
    * @return List<EventDto>
    */
    List<EventDto> queryAll(EventQueryCriteria criteria);

    /**
     * Query by ID
     * @param id ID
     * @return EventDto
     */
    EventDto findById(Long id);

    /**
    * Create
    * @param resources /
    */
    EventDto create(EventDto resources);

    /**
    * Edit
    * @param resources /
    */
    EventDto update(Event resources);

    EventDto updateStatus(Long id, EventStatus status);

    /**
     * Join an event
     * @param joinEventDto Data for joining an event
     * @return Updated event data
     */
    EventDto joinEvent(JoinEventDto joinEventDto);

    /**
     * Delete multiple events
     * @param ids Array of event IDs to delete
     * @return ExecutionResult containing information about the deleted entities
     */
    ExecutionResult deleteAll(Long[] ids);

    /**
    * Export data
    * @param all data to be exported
    * @param response /
    * @throws IOException /
    */
    void download(List<EventDto> all, HttpServletResponse response) throws IOException;

}