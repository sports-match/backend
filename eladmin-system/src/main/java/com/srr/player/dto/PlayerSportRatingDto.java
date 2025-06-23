package com.srr.player.dto;

import com.srr.enumeration.Format;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO for PlayerSportRating
 */
@Data
public class PlayerSportRatingDto implements Serializable {
    private Long id;
    private Long playerId;
    private String sport;
    private Format format;
    private Double rateScore;
    private String rateBand;
    private Boolean provisional;
    private Timestamp createTime;
    private Timestamp updateTime;
}
