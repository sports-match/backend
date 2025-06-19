package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class TagDto implements Serializable {

    @ApiModelProperty(value = "Tag ID", hidden = true)
    private Long id;

    @ApiModelProperty(value = "Tag Name", required = true)
    private String name;
}
