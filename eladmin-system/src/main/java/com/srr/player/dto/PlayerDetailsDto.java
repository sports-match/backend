package com.srr.player.dto;

import com.srr.enumeration.Format;

import java.util.List;
import java.util.Map;

public class PlayerDetailsDto {
    private PlayerDto player;
    private Map<PlayerSportRatingDto, List<RatingHistoryDto>> sportRatings;
    private Map<Format, Integer> eventJoined;
}
