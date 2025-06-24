package com.srr.player.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
* @description /
* @author Chanheng
* @date 2025-05-25
**/
@Data
public class TeamPlayerDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "Team id")
    private Long teamId;

    @ApiModelProperty(value = "Player id")
    private Long playerId;
    
    @ApiModelProperty(value = "Player name")
    private String playerName;

    @ApiModelProperty(value = "Score")
    private Double score;

    @ApiModelProperty(value = "Is checked in")
    private Boolean isCheckedIn;
}
