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
package com.srr.rest;

import com.srr.domain.Player;
import com.srr.dto.PlayerAssessmentStatusDto;
import com.srr.dto.PlayerDto;
import com.srr.dto.PlayerQueryCriteria;
import com.srr.service.PlayerService;
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
* @author Chanheng
* @date 2025-05-18
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "Player Management")
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @ApiOperation("Query sport")
    @PreAuthorize("@el.check('player:list')")
    public ResponseEntity<PageResult<PlayerDto>> queryPlayer(PlayerQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(playerService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get player by ID")
    @PreAuthorize("@el.check('player:list')")
    public ResponseEntity<PlayerDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(playerService.findById(id), HttpStatus.OK);
    }

    @PutMapping
    @Log("Edit player")
    @ApiOperation("Edit player")
    @PreAuthorize("@el.check('player:edit')")
    public ResponseEntity<Object> updatePlayer(@Validated @RequestBody Player resources){
        ExecutionResult result = playerService.update(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
    
    @GetMapping("/assessment-status")
    @ApiOperation("Check if player has completed self-assessment")
    public ResponseEntity<PlayerAssessmentStatusDto> checkAssessmentStatus() {
        PlayerAssessmentStatusDto status = playerService.checkAssessmentStatus();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}