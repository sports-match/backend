package com.srr.event.dto;

import com.srr.club.dto.ClubDto;
import com.srr.enumeration.EventStatus;
import com.srr.enumeration.Format;
import com.srr.enumeration.TeamPlayerStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Chanheng
 * @description /
 * @date 2025-05-18
 **/
@Data
public class EventDto extends EventTimeDto implements Serializable {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "名称", required = true)
    @NotBlank(message = "Event name is mandatory")
    private String name;

    @ApiModelProperty(value = "描述", required = true)
    @NotBlank(message = "Event description is mandatory")
    private String description;

    @ApiModelProperty(value = "SINGLE, DOUBLE", required = true)
    @NotNull(message = "Event format is mandatory")
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

    @ApiModelProperty(value = "clubId")
    @NotNull(message = "Club is mandatory")
    private Long clubId;

    @ApiModelProperty(value = "publicLink")
    private String publicLink;

    @ApiModelProperty(value = "sportId")
    private Long sportId;

    @ApiModelProperty(value = "createBy")
    private Long createBy;

    private EventStatus status;

    @ApiModelProperty(value = "Is event public?", required = true)
    private Boolean isPublic;

    private boolean allowWaitList;

    @ApiModelProperty(value = "Check-in time")
    private Timestamp checkInAt;

    @ApiModelProperty(value = "Allow self check-in")
    private Boolean allowSelfCheckIn = true;

    @ApiModelProperty(value = "Number of groups", required = true)
    @NotNull(message = "Number of groups is mandatory")
    private Integer groupCount;

    private String posterImage;

    @ApiModelProperty(value = "Current number of participants")
    private Integer currentParticipants;

    @ApiModelProperty(value = "Maximum number of participants", required = true)
    @NotNull(message = "Max participants is mandatory")
    private Integer maxParticipants;

    @ApiModelProperty(value = "Co-host organizers of the event")
    private List<Long> coHostOrganizers;

    @ApiModelProperty(value = "Tags")
    private Set<String> tags = new HashSet<>();

    private boolean isJoined;

    // Indicates if the current user can sign up for this event (for unauthenticated users/future events)
    private boolean canSignUp;

    // Player's status for this event (for authenticated users)
    private TeamPlayerStatus playerStatus;


    private ClubDto club;
}