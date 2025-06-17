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
package com.srr.club;

import com.srr.club.domain.Court;
import com.srr.club.dto.CourtDto;
import com.srr.dto.CourtQueryCriteria;
import com.srr.service.CourtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* @website https://eladmin.vip
* @author Chanheng
* @date 2025-05-18
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "Court Management")
@RequestMapping("/api/courts")
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    @ApiOperation("Query court")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<PageResult<CourtDto>> queryCourt(CourtQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(courtService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get court by ID")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<CourtDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(courtService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    @Log("Add court")
    @ApiOperation("Add court")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> createCourt(@Validated @RequestBody Court resources){
        ExecutionResult result = courtService.create(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.CREATED);
    }

    @PutMapping
    @Log("Modify court")
    @ApiOperation("Modify court")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> updateCourt(@Validated @RequestBody Court resources){
        ExecutionResult result = courtService.update(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }

    @DeleteMapping
    @Log("Delete court")
    @ApiOperation("Delete court")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> deleteCourt(@RequestBody Long[] ids) {
        ExecutionResult result = courtService.deleteAll(ids);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
}