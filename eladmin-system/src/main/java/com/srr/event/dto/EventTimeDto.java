package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
public class EventTimeDto {
    @ApiModelProperty(value = "时间", required = true)
    @NotNull(message = "Event time is mandatory")
    private Timestamp eventTime;
    
    @ApiModelProperty(value = "Check-in start time", required = true)
    private Timestamp checkInStart;

    @ApiModelProperty(value = "Check-in end time (optional, defaults to event start time)")
    private Timestamp checkInEnd;
}
