package com.srr.event;

import com.srr.event.dto.TagDto;
import com.srr.service.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Api(tags = "Tag Management")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @ApiOperation("Get all tags")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<List<TagDto>> getAllTags() {
        return new ResponseEntity<>(tagService.findAll(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("Delete tag by ID")
    @PreAuthorize("hasAuthority('Organizer')")
    public ResponseEntity<Object> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
