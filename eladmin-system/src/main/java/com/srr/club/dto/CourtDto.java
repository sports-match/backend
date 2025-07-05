package com.srr.club.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description /
* @author Chanheng
* @date 2025-05-18
**/
@Data
public class CourtDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "clubId")
    private Long clubId;

    @ApiModelProperty(value = "sportId")
    private Long sportId;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "数量")
    private Integer amount;
}