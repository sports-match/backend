package com.srr.player.dto;

import lombok.Data;

@Data
public class PlayerDoublesStatsDto {
    private Long playerId;
    private String playerName;
    private Double doublesRanking;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private String record; // e.g. "10-5"
}
