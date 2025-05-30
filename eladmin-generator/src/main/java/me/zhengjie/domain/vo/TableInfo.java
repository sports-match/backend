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
package me.zhengjie.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Table data information
 * @author Zheng Jie
 * @date 2019-01-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {

    @ApiModelProperty(value = "Table name")
    private Object tableName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "Creation date: yyyy-MM-dd HH:mm:ss")
    private Object createTime;

    @ApiModelProperty(value = "Database engine")
    private Object engine;

    @ApiModelProperty(value = "Character set")
    private Object coding;

    @ApiModelProperty(value = "Remarks")
    private Object remark;
}
