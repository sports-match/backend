package com.srr.player;

import com.srr.player.domain.Player;
import com.srr.player.dto.*;
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

import javax.validation.constraints.Min;
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
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PageResult<PlayerDto>> queryPlayer(PlayerQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(playerService.queryAll(criteria, pageable), HttpStatus.OK);
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
    public ResponseEntity<PlayerDetailsDto> getByIdForHomPage(@PathVariable @Min(1) Long id,
                                                              PlayerDetailsRequest request) {
        return new ResponseEntity<>(playerService.findPlayerDetailsById(id, request), HttpStatus.OK);
    }

    @PutMapping
    @Log("Modify player")
    @ApiOperation("Modify player")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<Object> updatePlayer(@Validated @RequestBody Player resources) {
        playerService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/assessment-status")
    @ApiOperation("Check if player has completed self-assessment")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerAssessmentStatusDto> checkAssessmentStatus(@RequestParam Long sportId) {
        PlayerAssessmentStatusDto status = playerService.checkAssessmentStatus(sportId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/doubles-stats")
    @ApiOperation("Get all players' doubles stats (ranking, games played, record) with filter and pagination")
    @PreAuthorize("hasAnyAuthority('Organizer','Player')")
    public ResponseEntity<PageResult<PlayerDoublesStatsDto>> getAllPlayersDoublesStats(
            PlayerQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(playerService.getAllPlayersDoublesStats(criteria, pageable), HttpStatus.OK);
    }


    @GetMapping("/{id}/events-summary")
    @ApiOperation("Get all events with matches and net rating change for player")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<List<PlayerEventSummaryDto>> getAllEventsWithResultsAndRatingChange(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getAllEventsWithResultsAndRatingChange(id));
    }

    @GetMapping("/{id}/last-event-summary")
    @ApiOperation("Get the last event with matches and net rating change for player")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerEventSummaryDto> getLastEventWithResultsAndRatingChange(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getLastEventWithResultsAndRatingChange(id));
    }

    @GetMapping("/event/{id}/player-rating")
    public ResponseEntity<List<PlayerEventRatingDTO>> getEventPlayersRating(@PathVariable() Long id) {
        final List<PlayerEventRatingDTO> playerEventRatings = playerService.getPlayerEventRating(id);
        return new ResponseEntity<>(playerEventRatings, HttpStatus.OK);
    }
}