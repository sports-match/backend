package com.srr.service.impl;

import com.srr.domain.MatchFormat;
import com.srr.domain.Question;
import com.srr.sport.domain.Sport;
import com.srr.dto.QuestionDto;
import com.srr.repository.QuestionRepository;
import com.srr.sport.domain.SportRepository;
import com.srr.service.QuestionService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @description Question service implementation
* @author Chanheng
* @date 2025-05-31
**/
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final SportRepository sportRepository;

    @Override
    public PageResult<QuestionDto> queryAll(QuestionDto criteria, Pageable pageable) {
        Page<Question> page = questionRepository.findAll((root, query, cb) -> 
                QueryHelp.getPredicate(root, criteria, cb), pageable);
        return PageUtil.toPage(page.map(this::toDto));
    }

    @Override
    public List<QuestionDto> getAllForSelfAssessment() {
        Long sportId = getSportIdByName("badminton");
        return questionRepository.findBySportIdAndFormatOrderByCategoryAndOrderIndex(sportId, MatchFormat.DOUBLES).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionDto> getByCategory(String category) {
        return questionRepository.findByCategoryOrderByOrderIndex(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionDto create(QuestionDto resources) {
        Question question = new Question();
        question.setText(resources.getText());
        question.setCategory(resources.getCategory());
        question.setOrderIndex(resources.getOrderIndex());
        question.setMinValue(resources.getMinValue());
        question.setMaxValue(resources.getMaxValue());
        return toDto(questionRepository.save(question));
    }

    @Override
    @Transactional
    public ExecutionResult update(QuestionDto resources) {
        Question question = questionRepository.findById(resources.getId())
                .orElseThrow(() -> new EntityNotFoundException(Question.class, "id", resources.getId().toString()));
        Question updated = new Question();
        updated.setId(question.getId());
        updated.setText(resources.getText());
        updated.setCategory(resources.getCategory());
        updated.setOrderIndex(resources.getOrderIndex());
        updated.setMinValue(resources.getMinValue());
        updated.setMaxValue(resources.getMaxValue());
        updated.setCreateTime(question.getCreateTime());
        questionRepository.save(updated);
        return ExecutionResult.of(updated.getId());
    }

    @Override
    @Transactional
    public ExecutionResult delete(Long id) {
        // Verify question exists
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException(Question.class, "id", id.toString());
        }
        // Delete question
        questionRepository.deleteById(id);
        return ExecutionResult.ofDeleted(id);
    }

    @Override
    public QuestionDto findById(Long id) {
        Optional<Question> question = questionRepository.findById(id);
        ValidationUtil.isNull(question, "Question", "id", id);
        return toDto(question.get());
    }

    private QuestionDto toDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setCategory(question.getCategory());
        dto.setOrderIndex(question.getOrderIndex());
        dto.setMinValue(question.getMinValue());
        dto.setMaxValue(question.getMaxValue());
        dto.setCreateTime(question.getCreateTime());
        dto.setUpdateTime(question.getUpdateTime());
        return dto;
    }

    private Long getSportIdByName(String sportName) {
        return sportRepository.findAll().stream()
            .filter(s -> s.getName().equalsIgnoreCase(sportName))
            .findFirst()
            .map(Sport::getId)
            .orElseThrow(() -> new BadRequestException("Sport not found: " + sportName));
    }
}
