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

import me.zhengjie.annotation.Log;
import com.srr.domain.Player;
import com.srr.service.PlayerService;
import com.srr.dto.PlayerQueryCriteria;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import me.zhengjie.utils.PageResult;
import com.srr.dto.PlayerDto;

/**
* @website https://eladmin.vip
* @author Chanheng
* @date 2025-05-18
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "player")
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;

    @ApiOperation("Export Data")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('player:list')")
    public void exportPlayer(HttpServletResponse response, PlayerQueryCriteria criteria) throws IOException {
        playerService.download(playerService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("Query sport")
    @PreAuthorize("@el.check('player:list')")
    public ResponseEntity<PageResult<PlayerDto>> queryPlayer(PlayerQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(playerService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("Add sport")
    @ApiOperation("Add sport")
    @PreAuthorize("@el.check('player:add')")
    public ResponseEntity<Object> createPlayer(@Validated @RequestBody Player resources){
        playerService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("Edit sport")
    @ApiOperation("Edit sport")
    @PreAuthorize("@el.check('player:edit')")
    public ResponseEntity<Object> updatePlayer(@Validated @RequestBody Player resources){
        playerService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("Delete sport")
    @ApiOperation("Delete sport")
    @PreAuthorize("@el.check('player:del')")
    public ResponseEntity<Object> deletePlayer(@ApiParam(value = "Pass ID array []") @RequestBody Long[] ids) {
        playerService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}