package com.srr.player.dto;

import com.srr.enumeration.Gender;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
* @description /
* @author Chanheng
* @date 2025-05-18
**/
@Data
public class PlayerDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "纬度")
    private Double latitude;

    @ApiModelProperty(value = "经度")
    private Double longitude;

    @ApiModelProperty(value = "图片")
    private String profileImage;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "评分")
    private Double rateScore;

    @ApiModelProperty(value = "userId")
    private Long userId;

    @ApiModelProperty(value = "性别")
    private Gender gender;

    @ApiModelProperty(value = "出生日期")
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "各运动评分")
    private List<PlayerSportRatingDto> sportRatings;
}