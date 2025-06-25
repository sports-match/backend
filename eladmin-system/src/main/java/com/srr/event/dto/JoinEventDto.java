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
    
    // Removed teamId, as team assignment is only via /reassign
}
