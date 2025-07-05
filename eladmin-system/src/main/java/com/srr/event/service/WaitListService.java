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
package com.srr.event.service;

import com.srr.enumeration.WaitListStatus;
import com.srr.event.domain.Event;
import com.srr.event.domain.WaitList;
import com.srr.event.dto.WaitListDto;
import com.srr.event.dto.WaitListQueryCriteria;
import com.srr.event.repository.EventRepository;
import com.srr.event.repository.WaitListRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
@Service
@RequiredArgsConstructor
public class WaitListService {

    private final WaitListRepository waitListRepository;
    private final EventRepository eventRepository;

    
    @Transactional
    public ExecutionResult create(WaitList resources) {
        // Validate event exists
        Event event = eventRepository.findById(resources.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(resources.getEventId())));
        
        // Check if player is already in wait list
        if (waitListRepository.findByEventIdAndPlayerId(resources.getEventId(), resources.getPlayerId()) != null) {
            throw new BadRequestException("Player is already in wait list");
        }
        
        // Set default status
        resources.setStatus(WaitListStatus.WAITING);
        
        WaitList saved = waitListRepository.save(resources);
        return ExecutionResult.of(saved.getId(), Map.of("status", saved.getStatus()));
    }

    
    @Transactional
    public ExecutionResult update(WaitList resources) {
        WaitList waitList = waitListRepository.findById(resources.getId())
                .orElseThrow(() -> new EntityNotFoundException(WaitList.class, "id", String.valueOf(resources.getId())));
        waitList.copy(resources);
        waitListRepository.save(waitList);
        return ExecutionResult.of(resources.getId());
    }

    
    @Transactional
    public ExecutionResult delete(Long id) {
        waitListRepository.deleteById(id);
        return ExecutionResult.ofDeleted(id);
    }

    
    @Transactional
    public ExecutionResult deleteAll(List<Long> ids) {
        waitListRepository.deleteAllById(ids);
        return ExecutionResult.of(null, Map.of("count", ids.size(), "ids", ids));
    }

    
    public WaitListDto findById(Long id) {
        WaitList waitList = waitListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(WaitList.class, "id", String.valueOf(id)));
        return mapToDto(waitList);
    }

    
    public List<WaitListDto> findByEventId(Long eventId) {
        // Validate event exists
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException(Event.class, "id", String.valueOf(eventId));
        }
        
        return waitListRepository.findByEventId(eventId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    
    public List<WaitListDto> findByPlayerId(Long playerId) {
        return waitListRepository.findByPlayerId(playerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    
    public WaitListDto findByEventAndPlayer(Long eventId, Long playerId) {
        WaitList waitList = waitListRepository.findByEventIdAndPlayerId(eventId, playerId);
        return waitList != null ? mapToDto(waitList) : null;
    }

    
    @Transactional
    public boolean promoteToParticipant(Long waitListId) {
        // Find wait list entry
        WaitList waitList = waitListRepository.findById(waitListId)
                .orElseThrow(() -> new EntityNotFoundException(WaitList.class, "id", String.valueOf(waitListId)));
        
        // Find event
        Event event = eventRepository.findById(waitList.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(waitList.getEventId())));
        
        // Check if event is full
        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            return false;
        }
        
        // Update wait list status
        waitList.setStatus(WaitListStatus.PROMOTED);
        waitListRepository.save(waitList);
        
        // Increment participant count
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        eventRepository.save(event);
        
        // TODO: Add player to event participants (implementation depends on your data model)
        
        return true;
    }

    
    public PageResult<WaitListDto> queryAll(WaitListQueryCriteria criteria, Pageable pageable) {
        Page<WaitList> page = waitListRepository.findAll((root, criteriaQuery, criteriaBuilder) -> 
                QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(this::mapToDto));
    }

    
    public List<WaitListDto> queryAll(WaitListQueryCriteria criteria) {
        return waitListRepository.findAll((root, criteriaQuery, criteriaBuilder) -> 
                QueryHelp.getPredicate(root, criteria, criteriaBuilder))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Map entity to DTO
     */
    private WaitListDto mapToDto(WaitList waitList) {
        if (waitList == null) {
            return null;
        }
        
        WaitListDto dto = new WaitListDto();
        dto.setId(waitList.getId());
        dto.setEventId(waitList.getEventId());
        dto.setPlayerId(waitList.getPlayerId());
        dto.setNotes(waitList.getNotes());
        dto.setStatus(waitList.getStatus());
        dto.setCreateTime(waitList.getCreateTime());
        dto.setUpdateTime(waitList.getUpdateTime());
        
        // Load relationships if needed (can be implemented based on requirements)
        
        return dto;
    }
}
