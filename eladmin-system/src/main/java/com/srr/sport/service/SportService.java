package com.srr.sport.service;

import com.srr.sport.domain.Sport;
import com.srr.sport.dto.SportDto;
import com.srr.sport.dto.SportMapper;
import com.srr.sport.dto.SportQueryCriteria;
import com.srr.sport.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chanheng
 * @description 服务实现
 * @date 2025-05-17
 **/
@Service
@RequiredArgsConstructor
public class SportService {

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;


    public PageResult<SportDto> queryAll(SportQueryCriteria criteria, Pageable pageable) {
        Page<Sport> page = sportRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sportMapper::toDto));
    }


    public List<SportDto> queryAll(SportQueryCriteria criteria) {
        return sportMapper.toDto(sportRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    public Sport getBadminton() {
        return getByName("Badminton");
    }

    public Sport getByName(String name) {
        return sportRepository.findByName(name).orElseThrow(() -> new EntityNotFoundException(Sport.class, "name", name));
    }

    @Transactional
    public SportDto findById(Long id) {
        Sport sport = sportRepository.findById(id).orElseGet(Sport::new);
        ValidationUtil.isNull(sport.getId(), "Sport", "id", id);
        return sportMapper.toDto(sport);
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Sport resources) {
        Sport savedSport = sportRepository.save(resources);
        return ExecutionResult.of(savedSport.getId());
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Sport resources) {
        Sport sport = sportRepository.findById(resources.getId()).orElseGet(Sport::new);
        ValidationUtil.isNull(sport.getId(), "Sport", "id", resources.getId());
        sport.copy(resources);
        Sport savedSport = sportRepository.save(sport);
        return ExecutionResult.of(savedSport.getId());
    }


    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            sportRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }


    public void download(List<SportDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SportDto sport : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("名称", sport.getName());
            map.put("描述", sport.getDescription());
            map.put("创建时间", sport.getCreateTime());
            map.put("更新时间", sport.getUpdateTime());
            map.put("图标", sport.getIcon());
            map.put("排序", sport.getSort());
            map.put("是否启用", sport.getEnabled());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}