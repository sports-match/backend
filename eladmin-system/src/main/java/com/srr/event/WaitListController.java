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
package com.srr.event;

import com.srr.event.domain.WaitList;
import com.srr.event.dto.WaitListDto;
import com.srr.event.dto.WaitListQueryCriteria;
import com.srr.event.service.WaitListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "Wait List Management")
@RequestMapping("/api/waitlists")
public class WaitListController {

    private final WaitListService waitListService;
    private static final String ENTITY_NAME = "waitList";

    @ApiOperation("Query wait list entries")
    @GetMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<PageResult<WaitListDto>> queryWaitList(WaitListQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(waitListService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("Query wait list entries by event ID")
    @GetMapping(value = "/event/{eventId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<WaitListDto>> queryWaitListByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(waitListService.findByEventId(eventId), HttpStatus.OK);
    }

    @ApiOperation("Query wait list entries by player ID")
    @GetMapping(value = "/player/{playerId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<WaitListDto>> queryWaitListByPlayer(@PathVariable Long playerId) {
        return new ResponseEntity<>(waitListService.findByPlayerId(playerId), HttpStatus.OK);
    }

    @ApiOperation("Get wait list entry by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<WaitListDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(waitListService.findById(id), HttpStatus.OK);
    }

    @ApiOperation("Add to wait list")
    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> create(@Validated @RequestBody WaitList resources) {
        ExecutionResult result = waitListService.create(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.CREATED);
    }

    @ApiOperation("Update wait list entry")
    @PutMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> update(@Validated @RequestBody WaitList resources) {
        ExecutionResult result = waitListService.update(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }

    @ApiOperation("Promote player from wait list to participant")
    @PutMapping(value = "/{id}/promote")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> promoteWaitListEntry(@PathVariable Long id) {
        boolean success = waitListService.promoteToParticipant(id);
        if (success) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("promoted", true);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @ApiOperation("Delete wait list entry")
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> deleteWaitList(@PathVariable Long id) {
        ExecutionResult result = waitListService.delete(id);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
}
