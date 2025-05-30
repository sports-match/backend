/*
*  Copyright 2019-2025 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.srr.dto;

import com.srr.enumeration.EventStatus;
import com.srr.enumeration.Format;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
* @website https://eladmin.vip
* @description /
* @author Chanheng
* @date 2025-05-18
**/
@Data
public class EventDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "SINGLE, DOUBLE")
    private Format format;

    @ApiModelProperty(value = "位置")
    private String location;

    @ApiModelProperty(value = "图片")
    private String image;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "时间")
    private Timestamp eventTime;

    @ApiModelProperty(value = "clubId")
    private Long clubId;

    @ApiModelProperty(value = "publicLink")
    private String publicLink;

    @ApiModelProperty(value = "sportId")
    private Long sportId;

    @ApiModelProperty(value = "createBy")
    private Long createBy;

    private EventStatus status;

    private boolean isPublic;

    private boolean allowWaitList;
    
    @ApiModelProperty(value = "Check-in time")
    private Timestamp checkInAt;
    
    @ApiModelProperty(value = "Number of groups")
    private Integer groupCount;

    private String posterImage;
    
    @ApiModelProperty(value = "Current number of participants")
    private Integer currentParticipants;
    
    @ApiModelProperty(value = "Maximum number of participants")
    private Integer maxParticipants;
    
    @ApiModelProperty(value = "Co-host players")
    private List<PlayerDto> coHostPlayers;
    
    @ApiModelProperty(value = "Tags")
    private List<String> tags;
}