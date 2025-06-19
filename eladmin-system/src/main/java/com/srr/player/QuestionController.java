package com.srr.player;

import com.srr.player.dto.QuestionDto;
import com.srr.player.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.ExecutionResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @description Question controller for API endpoints
* @author Chanheng
* @date 2025-05-31
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "Self-Assessment Questions Management")
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    
    @ApiOperation("Get all questions with pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> getAll(QuestionDto criteria, Pageable pageable) {
        return new ResponseEntity<>(questionService.queryAll(criteria, pageable), HttpStatus.OK);
    }
    
    @ApiOperation("Get all questions for self-assessment")
    @GetMapping("/self-assessment")
    @PreAuthorize("hasAuthority('Player')")
    public ResponseEntity<List<QuestionDto>> getAllForSelfAssessment() {
        return new ResponseEntity<>(questionService.getAllForSelfAssessment(), HttpStatus.OK);
    }
    
    @ApiOperation("Get questions by category")
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<QuestionDto>> getByCategory(@PathVariable String category) {
        return new ResponseEntity<>(questionService.getByCategory(category), HttpStatus.OK);
    }
    
    @ApiOperation("Get question by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<QuestionDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(questionService.findById(id), HttpStatus.OK);
    }
    
    @ApiOperation("Create a new question")
    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<QuestionDto> create(@Validated @RequestBody QuestionDto resources) {
        return new ResponseEntity<>(questionService.create(resources), HttpStatus.CREATED);
    }
    
    @ApiOperation("Update a question")
    @PutMapping
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> update(@Validated @RequestBody QuestionDto resources) {
        ExecutionResult result = questionService.update(resources);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
    
    @ApiOperation("Delete a question")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        ExecutionResult result = questionService.delete(id);
        return new ResponseEntity<>(result.toMap(), HttpStatus.OK);
    }
}
