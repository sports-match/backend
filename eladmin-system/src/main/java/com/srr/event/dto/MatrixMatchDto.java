package com.srr.event.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.srr.player.dto.TeamDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
public class MatrixMatchDto implements Serializable {

    @ApiModelProperty(value = "Other team id")
    private TeamDto otherTeam;

    @NotNull(message = "My score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "My score", required = true)
    private Integer myScore;

    @NotNull(message = "Other score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @ApiModelProperty(value = "Other score", required = true)
    private Integer otherScore;

}
