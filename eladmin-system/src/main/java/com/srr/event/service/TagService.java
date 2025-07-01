package com.srr.event.service;

import com.srr.event.dto.TagDto;
import com.srr.event.dto.TagMapper;
import com.srr.event.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    
    @Transactional(readOnly = true)
    public List<TagDto> findAll() {
        return tagMapper.toDto(tagRepository.findAll());
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
