package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for updating match scores by players
 */
@Data
public class MatchScoreUpdateDto implements Serializable {

    @NotNull(message = "Match ID is required")
    @ApiModelProperty(value = "Match ID", required = true)
    private Long matchId;
    
    @NotNull(message = "Team A score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "Score for Team A", required = true)
    private Integer scoreA;
    
    @NotNull(message = "Team B score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "Score for Team B", required = true)
    private Integer scoreB;
}
