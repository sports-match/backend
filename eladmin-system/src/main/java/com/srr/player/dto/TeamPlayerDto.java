package com.srr.player.dto;

import com.srr.enumeration.TeamPlayerStatus;
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

    private PlayerDto player;

    @ApiModelProperty(value = "Score")
    private Double score;

    @ApiModelProperty(value = "Is checked in")
    private Boolean isCheckedIn;

    private TeamPlayerStatus status;
}
