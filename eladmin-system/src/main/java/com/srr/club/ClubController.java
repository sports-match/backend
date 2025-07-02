package com.srr.club;

import com.srr.club.domain.Club;
import com.srr.club.dto.ClubDto;
import com.srr.club.dto.ClubQueryCriteria;
import com.srr.club.service.ClubService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Api(tags = "Club Management")
@Slf4j
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    @ApiOperation("Query clubs")
    @PreAuthorize("hasAnyAuthority('Admin', 'Organizer')")
    public ResponseEntity<PageResult<ClubDto>> queryClub(ClubQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(clubService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation("Get club by ID")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<ClubDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(clubService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    @Log("Add clubs")
    @ApiOperation("Add clubs")
    public ResponseEntity<Object> createClub(@Validated @RequestBody Club resources) {
        ExecutionResult result = clubService.create(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.CREATED);
    }

    @PutMapping
    @Log("Modify clubs")
    @ApiOperation("Modify clubs")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> updateClub(@Validated @RequestBody Club resources) {
        ExecutionResult result = clubService.update(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }

    @DeleteMapping
    @Log("Delete clubs")
    @ApiOperation("Delete clubs")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> deleteClub(@RequestBody Long[] ids) {
        ExecutionResult result = clubService.deleteAll(ids);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
}