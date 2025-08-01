package com.srr.player;

import com.srr.enumeration.Format;
import com.srr.player.dto.PlayerSportRatingDto;
import com.srr.player.service.PlayerSportRatingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Player Sport Rating Management")
@RestController
@RequestMapping("/api/player-sport-ratings")
@RequiredArgsConstructor
public class PlayerSportRatingController {

    private final PlayerSportRatingService playerSportRatingService;

    @ApiOperation("Get all ratings for a player")
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<List<PlayerSportRatingDto>> getRatingsForPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerSportRatingService.getRatingsForPlayer(playerId));
    }

    @ApiOperation("Get rating for player, sport, and format")
    @GetMapping("/player/{playerId}/sport/{sport}/format/{format}")
    @PreAuthorize("hasAnyAuthority('Player', 'Organizer')")
    public ResponseEntity<PlayerSportRatingDto> getRatingForPlayerSportFormat(@PathVariable Long playerId, @PathVariable String sport, @PathVariable String format) {
        var formatEnum = Format.valueOf(format.toUpperCase());
        PlayerSportRatingDto dto = playerSportRatingService.getRatingForPlayerSportFormat(playerId, sport, formatEnum);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

}
