package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
@Data
public class JoinEventDto {
    
    @ApiModelProperty(value = "Event ID")
    @NotNull
    private Long eventId;
    
    @ApiModelProperty(value = "Player ID")
    @NotNull
    private Long playerId;
    
    @ApiModelProperty(value = "Team ID (optional)")
    private Long teamId;
    
    @ApiModelProperty(value = "Join as wait list")
    private Boolean joinWaitList = false;
}
