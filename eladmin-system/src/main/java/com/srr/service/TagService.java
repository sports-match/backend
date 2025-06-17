package com.srr.service;

import com.srr.event.dto.TagDto;

import java.util.List;

public interface TagService {
    List<TagDto> findAll();
    void delete(Long id);
}
