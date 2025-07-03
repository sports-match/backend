package com.srr.organizer.dto;

import com.srr.club.dto.ClubDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.modules.system.service.dto.UserDto;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Chanheng
 * @description Event organizer data transfer object
 * @date 2025-05-26
 **/
@Data
public class EventOrganizerDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "Club")
    private ClubDto club;

    @ApiModelProperty(value = "Description")
    private String description;

    @ApiModelProperty(value = "Creation time")
    private Timestamp createTime;

    @ApiModelProperty(value = "Update time")
    private Timestamp updateTime;

    @ApiModelProperty(value = "User")
    private UserDto user;
}
