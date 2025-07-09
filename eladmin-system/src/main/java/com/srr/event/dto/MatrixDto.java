package com.srr.event.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.srr.player.dto.TeamDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class MatrixDto implements Serializable {

    @ApiModelProperty(value = "Team id")
    private TeamDto team;

    @ApiModelProperty(value = "Matches score for the team")
    private List<MatrixMatchDto> matches;

}
