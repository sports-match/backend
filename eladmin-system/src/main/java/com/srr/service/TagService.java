package com.srr.service;

import com.srr.dto.TagDto;

import java.util.List;

public interface TagService {
    List<TagDto> findAll();
    void delete(Long id);
}
