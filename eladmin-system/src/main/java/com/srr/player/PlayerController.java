package com.srr.player;

import com.srr.player.domain.Player;
import com.srr.player.dto.PlayerAssessmentStatusDto;
import com.srr.player.dto.PlayerDetailsDto;
import com.srr.player.dto.PlayerDto;
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
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PageResult<PlayerDto>> queryPlayer(PlayerQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(playerService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get player by ID")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(playerService.findById(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/dashboard")
    @ApiOperation("Get player by ID")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerDetailsDto> getByIdForHomPage(@PathVariable Long id) {
        return new ResponseEntity<>(playerService.findPlayerDetailsById(id), HttpStatus.OK);
    }

    @PutMapping
    @Log("Modify player")
    @ApiOperation("Modify player")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> updatePlayer(@Validated @RequestBody Player resources){
        playerService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/assessment-status")
    @ApiOperation("Check if player has completed self-assessment")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerAssessmentStatusDto> checkAssessmentStatus() {
        PlayerAssessmentStatusDto status = playerService.checkAssessmentStatus();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}