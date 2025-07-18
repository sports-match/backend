package com.srr.player.dto;

import com.srr.enumeration.TeamPlayerStatus;
import com.srr.utils.NumberConverter;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Chanheng
 * @description /
 * @date 2025-05-25
 **/
@Data
public class TeamPlayerDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "Team id")
    private Long teamId;
    
    private PlayerDto player;

    @ApiModelProperty(value = "Score")
    private Double score;

    @ApiModelProperty(value = "Player's email")
    private String email;

    @ApiModelProperty(value = "Is checked in")
    private Boolean isCheckedIn;

    private TeamPlayerStatus status;

    private PlayerDto partner;

    @ApiModelProperty(value = "Combine average score for the team")
    private Double combinedScore;

    // Custom getters
    public Long getScore() {
        return NumberConverter.doubleToLong(score);
    }

    public Long getCombinedScore() {
        return NumberConverter.doubleToLong(combinedScore);
    }
}
