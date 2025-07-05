package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Data
public class MatchGroupGenerationDto {
    
    @NotNull
    @ApiModelProperty(value = "Event ID")
    private Long eventId;
}
