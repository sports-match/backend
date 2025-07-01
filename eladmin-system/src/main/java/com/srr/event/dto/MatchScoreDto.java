package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * DTO for match score updates
 */
@Data
public class MatchScoreDto implements Serializable {
    
    @NotNull(message = "Team A score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "Score for Team A", required = true)
    private Integer scoreA;
    
    @NotNull(message = "Team B score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "Score for Team B", required = true)
    private Integer scoreB;
}
