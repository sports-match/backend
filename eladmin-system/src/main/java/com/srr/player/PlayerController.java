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
package com.srr.player;

import com.srr.player.domain.Player;
import com.srr.player.dto.PlayerAssessmentStatusDto;
import com.srr.player.dto.PlayerDto;
import com.srr.player.dto.PlayerDoublesStatsDto;
import com.srr.player.dto.PlayerQueryCriteria;
import com.srr.player.service.PlayerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiOperation("Query player")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<PageResult<PlayerDto>> queryPlayer(PlayerQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(playerService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get player by ID")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<PlayerDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(playerService.findById(id), HttpStatus.OK);
    }

    @PutMapping
    @Log("Modify player")
    @ApiOperation("Modify player")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> updatePlayer(@Validated @RequestBody Player resources){
        playerService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/assessment-status")
    @ApiOperation("Check if player has completed self-assessment")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<PlayerAssessmentStatusDto> checkAssessmentStatus() {
        PlayerAssessmentStatusDto status = playerService.checkAssessmentStatus();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/doubles-stats")
    @ApiOperation("Get all players' doubles stats (ranking, games played, record) with filter and pagination")
    @PreAuthorize("hasAnyAuthority('Organizer','Player')")
    public ResponseEntity<PageResult<PlayerDoublesStatsDto>> getAllPlayersDoublesStats(
            PlayerQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(playerService.getAllPlayersDoublesStats(criteria, pageable), HttpStatus.OK);
    }
}