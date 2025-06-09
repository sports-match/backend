package com.srr.rest;

import com.srr.domain.Match;
import com.srr.dto.MatchDto;
import com.srr.dto.MatchScoreUpdateDto;
import com.srr.dto.mapstruct.MatchMapper;
import com.srr.service.MatchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing match operations
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "Match Management")
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @ApiOperation("Get match by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<MatchDto> getMatch(@PathVariable Long id) {
        Match match = matchService.findById(id);
        return new ResponseEntity<>(matchMapper.toDto(match), HttpStatus.OK);
    }
    
    @ApiOperation("Get all matches for the current user")
    @GetMapping("/my-matches")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<List<MatchDto>> getMyMatches() {
        List<Match> matches = matchService.findMatchesForCurrentUser();
        List<MatchDto> result = matches.stream()
                .map(matchMapper::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation("Update match score")
    @PutMapping("/{matchId}/score")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<MatchDto> updateMatchScore(
            @PathVariable Long matchId,
            @Validated @RequestBody MatchScoreUpdateDto scoreDto) {
        
        // Set the match ID from the path variable
        scoreDto.setMatchId(matchId);
        
        Match match = matchService.updateMatchScore(scoreDto);
        return new ResponseEntity<>(matchMapper.toDto(match), HttpStatus.OK);
    }

    @ApiOperation("Verify match score (admin only)")
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<MatchDto> verifyMatchScore(@PathVariable Long id) {
        Match match = matchService.verifyMatchScore(id);
        return new ResponseEntity<>(matchMapper.toDto(match), HttpStatus.OK);
    }
}
