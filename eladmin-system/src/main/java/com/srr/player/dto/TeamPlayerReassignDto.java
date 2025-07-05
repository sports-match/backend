package com.srr.player.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Data
public class TeamPlayerReassignDto {
    
    @NotNull
    @ApiModelProperty(value = "Team player ID to reassign")
    private Long teamPlayerId;
    
    @NotNull
    @ApiModelProperty(value = "Target team ID")
    private Long targetTeamId;
}
