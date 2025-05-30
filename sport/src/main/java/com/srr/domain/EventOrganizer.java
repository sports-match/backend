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
package com.srr.domain;

import lombok.Data;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModelProperty;
import cn.hutool.core.bean.copier.CopyOptions;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.*;
import java.sql.Timestamp;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
* @website https://eladmin.vip
* @description Event organizer entity
* @author Chanheng
* @date 2025-05-26
**/
@Entity
@Data
@Table(name="event_organizer")
public class EventOrganizer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "`description`")
    @ApiModelProperty(value = "Description")
    private String description;

    @Column(name = "`create_time`")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "`update_time`")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    @Column(name = "`user_id`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "userId")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    public void copy(EventOrganizer source){
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
