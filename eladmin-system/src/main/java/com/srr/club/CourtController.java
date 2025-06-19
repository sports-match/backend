package com.srr.club;

import com.srr.club.domain.Court;
import com.srr.club.dto.CourtDto;
import com.srr.club.dto.CourtQueryCriteria;
import com.srr.club.service.CourtService;
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