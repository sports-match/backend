package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerAnswer;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.domain.Question;
import com.srr.player.dto.PlayerAnswerDto;
import com.srr.player.dto.PlayerSelfAssessmentRequest;
import com.srr.player.repository.PlayerAnswerRepository;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.QuestionRepository;
import com.srr.sport.service.SportService;
import com.srr.utils.RatingService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @description PlayerAnswer service implementation
 * @date 2025-05-31
 **/
@Service
@RequiredArgsConstructor
public class PlayerAnswerService {

    private final PlayerAnswerRepository playerAnswerRepository;
    private final QuestionRepository questionRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final RatingService ratingService;
    private final SportService sportService;


    public PageResult<PlayerAnswerDto> queryAll(PlayerAnswerDto criteria, Pageable pageable) {
        Page<PlayerAnswer> page = playerAnswerRepository.findAll((root, query, cb) ->
                QueryHelp.getPredicate(root, criteria, cb), pageable);
        return PageUtil.toPage(page.map(this::toDto));
    }


    public List<PlayerAnswerDto> getByPlayerId(Long playerId) {
        return playerAnswerRepository.findByPlayerId(playerId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    public List<PlayerAnswerDto> getByPlayerIdAndCategory(Long playerId, String category) {
        return playerAnswerRepository.findByPlayerIdAndQuestionCategory(playerId, category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    // Overloaded method to support sport/format from controller
    public List<PlayerAnswerDto> submitSelfAssessment(PlayerSelfAssessmentRequest request, Format format) {
        final var answers = request.getAnswers();
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
        var sportId = request.getSportId();
        if (sportId == null) {
            final var sport = sportService.getBadminton();
            sportId = sport.getId();
        }

        List<Question> questions = questionRepository.findBySportIdAndFormatOrderByCategoryAndOrderIndex(sportId, format);
        List<Long> questionIds = questions.stream().map(Question::getId).toList();
        List<PlayerAnswer> relevantAnswers = savedAnswers.stream()
                .filter(ans -> questionIds.contains(ans.getQuestionId()))
                .collect(Collectors.toList());
        updatePlayerSportRating(playerId, sportId, relevantAnswers);
        return savedAnswers.stream().map(this::toDto).collect(Collectors.toList());
    }


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
        updatePlayerSportRating(resources.getPlayerId(), resources.getSportId(), answers);
        return ExecutionResult.of(saved.getId());
    }


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
        updatePlayerSportRating(resources.getPlayerId(), resources.getSportId(), answers);
        return ExecutionResult.of(saved.getId());
    }


    @Transactional
    public ExecutionResult delete(Long id) {
        PlayerAnswer playerAnswer = playerAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PlayerAnswer.class, "id", id.toString()));
        Long playerId = playerAnswer.getPlayerId();
        playerAnswerRepository.deleteById(id);
        // Recalculate player sport rating after delete
        List<PlayerAnswer> answers = playerAnswerRepository.findByPlayerId(playerId);
        var badminton = sportService.getBadminton();
        updatePlayerSportRating(playerId, badminton.getId(), answers);
        return ExecutionResult.ofDeleted(id);
    }


    public PlayerAnswerDto findById(Long id) {
        Optional<PlayerAnswer> playerAnswer = playerAnswerRepository.findById(id);
        ValidationUtil.isNull(playerAnswer, "PlayerAnswer", "id", id);
        return toDto(playerAnswer.get());
    }


    public boolean hasCompletedSelfAssessment(Long playerId) {
        long count = playerAnswerRepository.countByPlayerId(playerId);
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

    private void updatePlayerSportRating(Long playerId, Long sportId, List<PlayerAnswer> answers) {
        if (answers.isEmpty()) {
            return;
        }

        double score = ratingService.calculateInitialRating(answers);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new EntityNotFoundException(Player.class, "id", playerId.toString()));

        EnumSet.of(Format.SINGLE, Format.DOUBLE).forEach(format ->
                saveOrUpdateRating(player, sportId, format, score)
        );
    }

    private void saveOrUpdateRating(Player player, Long sportId, Format format, double score) {
        PlayerSportRating rating = playerSportRatingRepository
                .findByPlayerIdAndSportIdAndFormat(player.getId(), sportId, format)
                .orElseGet(PlayerSportRating::new);

        rating.setPlayer(player);
        rating.setSportId(sportId);
        rating.setRateScore(score);
        rating.setRateBand(null);
        rating.setProvisional(true);
        rating.setFormat(format);

        playerSportRatingRepository.save(rating);
    }

}
