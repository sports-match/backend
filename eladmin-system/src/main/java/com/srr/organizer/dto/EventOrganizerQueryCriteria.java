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
package com.srr.organizer.dto;

import com.srr.enumeration.VerificationStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
@Data
public class EventOrganizerQueryCriteria {
    @Query
    private Long id;

    @Query
    @ApiModelProperty(value = "Event ID")
    private Long eventId;

    @Query
    @ApiModelProperty(value = "Organizer User ID")
    private Long userId;

    @Query(propName = "id", joinName = "clubs", type = Query.Type.NOT_EQUAL)
    @ApiModelProperty(value = "Club ID")
    private Long clubId;

    @Query(type = Query.Type.BETWEEN)
    @ApiModelProperty(value = "Create time range")
    private List<Timestamp> createTime;

    @Query(propName = "username", joinName = "user", type = Query.Type.INNER_LIKE)
    @ApiModelProperty(value = "Username of organizer")
    private String username;

    @Query(type = Query.Type.EQUAL)
    private VerificationStatus verificationStatus = VerificationStatus.VERIFIED;
}
