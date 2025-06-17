package com.srr.service.impl;

import com.srr.domain.*;
import com.srr.dto.PlayerAnswerDto;
import com.srr.repository.PlayerAnswerRepository;
import com.srr.repository.PlayerRepository;
import com.srr.repository.PlayerSportRatingRepository;
import com.srr.repository.QuestionRepository;
import com.srr.service.PlayerAnswerService;
import com.srr.service.RatingService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @description PlayerAnswer service implementation
* @author Chanheng
* @date 2025-05-31
**/
@Service
@RequiredArgsConstructor
public class PlayerAnswerServiceImpl implements PlayerAnswerService {

    private final PlayerAnswerRepository playerAnswerRepository;
    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final RatingService ratingService;

    @Override
    public PageResult<PlayerAnswerDto> queryAll(PlayerAnswerDto criteria, Pageable pageable) {
        Page<PlayerAnswer> page = playerAnswerRepository.findAll((root, query, cb) -> 
                QueryHelp.getPredicate(root, criteria, cb), pageable);
        return PageUtil.toPage(page.map(this::toDto));
    }

    @Override
    public List<PlayerAnswerDto> getByPlayerId(Long playerId) {
        return playerAnswerRepository.findByPlayerId(playerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlayerAnswerDto> getByPlayerIdAndCategory(Long playerId, String category) {
        return playerAnswerRepository.findByPlayerIdAndQuestionCategory(playerId, category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<PlayerAnswerDto> submitSelfAssessment(List<PlayerAnswerDto> answers) {
        return submitSelfAssessment(answers, "Badminton", "DOUBLES");
    }

    // Overloaded method to support sport/format from controller
    public List<PlayerAnswerDto> submitSelfAssessment(List<PlayerAnswerDto> answers, String sport, String format) {
        if (answers.isEmpty()) {
            throw new BadRequestException("No answers provided");
        }
        Long playerId = answers.get(0).getPlayerId();
        if (!playerRepository.existsById(playerId)) {
            throw new BadRequestException("Player not found");
        }
        // Delete existing answers if any
        List<PlayerAnswer> existingAnswers = playerAnswerRepository.findByPlayerId(playerId);
        if (!existingAnswers.isEmpty()) {
            playerAnswerRepository.deleteAll(existingAnswers);
        }
        List<PlayerAnswer> savedAnswers = new ArrayList<>();
        for (PlayerAnswerDto answerDto : answers) {
            if (!questionRepository.existsById(answerDto.getQuestionId())) {
                throw new BadRequestException("Question with ID " + answerDto.getQuestionId() + " not found");
            }
            PlayerAnswer answer = new PlayerAnswer();
            answer.setPlayerId(playerId);
            answer.setQuestionId(answerDto.getQuestionId());
            answer.setAnswerValue(answerDto.getAnswerValue());
            savedAnswers.add(playerAnswerRepository.save(answer));
        }
        // Query sportId for given sport name
        Long sportId = questionRepository.findAll().stream()
            .filter(q -> q.getSport() != null && q.getSport().getName().equalsIgnoreCase(sport))
            .map(q -> q.getSport().getId())
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Sport not found: " + sport));
        // Query questions by sportId and format
        List<Question> questions = questionRepository.findBySportIdAndFormatOrderByCategoryAndOrderIndex(sportId, MatchFormat.valueOf(format));
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        // Only consider answers for these questions
        List<PlayerAnswer> relevantAnswers = savedAnswers.stream()
            .filter(ans -> questionIds.contains(ans.getQuestionId()))
            .collect(Collectors.toList());
        updatePlayerSportRating(playerId, sport, format, relevantAnswers);
        return savedAnswers.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(PlayerAnswerDto resources) {
        PlayerAnswer playerAnswer = new PlayerAnswer();
        playerAnswer.setPlayerId(resources.getPlayerId());
        playerAnswer.setQuestionId(resources.getQuestionId());
        playerAnswer.setAnswerValue(resources.getAnswerValue());
        PlayerAnswer saved = playerAnswerRepository.save(playerAnswer);
        // Recalculate player sport rating after creating an answer
        List<PlayerAnswer> answers = playerAnswerRepository.findByPlayerId(resources.getPlayerId());
        // Use default sport/format for single answer creation
        updatePlayerSportRating(resources.getPlayerId(), "Badminton", "DOUBLES", answers);
        return ExecutionResult.of(saved.getId());
    }

    @Override
    @Transactional
    public ExecutionResult update(PlayerAnswerDto resources) {
        PlayerAnswer playerAnswer = playerAnswerRepository.findById(resources.getId())
                .orElseThrow(() -> new EntityNotFoundException(PlayerAnswer.class, "id", resources.getId().toString()));
        Player player = playerRepository.findById(resources.getPlayerId())
                .orElseThrow(() -> new EntityNotFoundException(Player.class, "id", resources.getPlayerId().toString()));
        Question question = questionRepository.findById(resources.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException(Question.class, "id", resources.getQuestionId().toString()));
        playerAnswer.setPlayer(player);
        playerAnswer.setQuestion(question);
        playerAnswer.setAnswerValue(resources.getAnswerValue());
        PlayerAnswer saved = playerAnswerRepository.save(playerAnswer);
        // Recalculate player sport rating after update
        List<PlayerAnswer> answers = playerAnswerRepository.findByPlayerId(resources.getPlayerId());
        updatePlayerSportRating(resources.getPlayerId(), "Badminton", "DOUBLES", answers);
        return ExecutionResult.of(saved.getId());
    }

    @Override
    @Transactional
    public ExecutionResult delete(Long id) {
        PlayerAnswer playerAnswer = playerAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PlayerAnswer.class, "id", id.toString()));
        Long playerId = playerAnswer.getPlayerId();
        playerAnswerRepository.deleteById(id);
        // Recalculate player sport rating after delete
        List<PlayerAnswer> answers = playerAnswerRepository.findByPlayerId(playerId);
        updatePlayerSportRating(playerId, "Badminton", "DOUBLES", answers);
        return ExecutionResult.ofDeleted(id);
    }

    @Override
    public PlayerAnswerDto findById(Long id) {
        Optional<PlayerAnswer> playerAnswer = playerAnswerRepository.findById(id);
        ValidationUtil.isNull(playerAnswer, "PlayerAnswer", "id", id);
        return toDto(playerAnswer.get());
    }

    @Override
    public boolean hasCompletedSelfAssessment(Long playerId) {
        long count = playerAnswerRepository.countByPlayerId(playerId);
        // Consider assessment complete if at least one answer exists
        // In a real implementation, you might want to check if all required questions are answered
        return count > 0;
    }

    private PlayerAnswerDto toDto(PlayerAnswer playerAnswer) {
        PlayerAnswerDto dto = new PlayerAnswerDto();
        dto.setId(playerAnswer.getId());
        dto.setPlayerId(playerAnswer.getPlayerId());
        dto.setQuestionId(playerAnswer.getQuestionId());
        dto.setAnswerValue(playerAnswer.getAnswerValue());
        dto.setCreateTime(playerAnswer.getCreateTime());
        dto.setUpdateTime(playerAnswer.getUpdateTime());
        
        // Load question details if available
        if (playerAnswer.getQuestion() != null) {
            dto.setQuestionText(playerAnswer.getQuestion().getText());
            dto.setQuestionCategory(playerAnswer.getQuestion().getCategory());
        } else {
            // Lazy load question details if not already loaded
            questionRepository.findById(playerAnswer.getQuestionId()).ifPresent(question -> {
                dto.setQuestionText(question.getText());
                dto.setQuestionCategory(question.getCategory());
            });
        }
        
        return dto;
    }
    
    // New updatePlayerSportRating overload to accept relevant answers
    private void updatePlayerSportRating(Long playerId, String sport, String format, List<PlayerAnswer> answers) {
        if (answers.isEmpty()) {
            return;
        }
        double srrd = ratingService.calculateInitialRating(answers);
        // Apply rating to both singles and doubles
        for (MatchFormat fmt : MatchFormat.values()) {
            PlayerSportRating rating = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(playerId, sport, fmt.name())
                    .orElse(new PlayerSportRating());
            rating.setPlayerId(playerId);
            rating.setSport(sport);
            rating.setFormat(fmt.name());
            rating.setRateScore(srrd);
            rating.setRateBand(null);
            rating.setProvisional(true);
            playerSportRatingRepository.save(rating);
        }
    }
}
