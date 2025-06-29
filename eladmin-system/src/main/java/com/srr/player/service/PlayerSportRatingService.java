package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.PlayerSportRatingDto;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.sport.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerSportRatingService {
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final SportService sportService;

    public List<PlayerSportRatingDto> getRatingsForPlayer(Long playerId) {
        return playerSportRatingRepository.findByPlayerId(playerId)
            .stream().map(this::toDto).collect(Collectors.toList());
    }

    public PlayerSportRatingDto getRatingForPlayerSportFormat(Long playerId, String sport, Format format) {
        final var sportId = sportService.getByName(sport).getId();
        return playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(playerId, sportId, format)
            .map(this::toDto).orElse(null);
    }

    private PlayerSportRatingDto toDto(PlayerSportRating entity) {
        PlayerSportRatingDto dto = new PlayerSportRatingDto();
        dto.setId(entity.getId());
        dto.setPlayerId(entity.getPlayerId());
        dto.setSportId(entity.getSportId());
        dto.setFormat(entity.getFormat());
        dto.setRateScore(entity.getRateScore());
        dto.setRateBand(entity.getRateBand());
        dto.setProvisional(entity.getProvisional());
        return dto;
    }
}
