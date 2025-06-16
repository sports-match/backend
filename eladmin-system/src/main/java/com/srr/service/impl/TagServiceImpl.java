package com.srr.service.impl;

import com.srr.dto.TagDto;
import com.srr.dto.mapstruct.TagMapper;
import com.srr.repository.TagRepository;
import com.srr.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> findAll() {
        return tagMapper.toDto(tagRepository.findAll());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
