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
package com.srr.sport.service;

import com.srr.sport.domain.Sport;
import com.srr.sport.dto.SportDto;
import com.srr.dto.SportQueryCriteria;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
* @website https://eladmin.vip
* @description Service Interface
* @author Chanheng
* @date 2025-05-17
**/
public interface SportService {

    /**
    * Query data with pagination
    * @param criteria criteria
    * @param pageable pagination parameters
    * @return Map<String,Object>
    */
    PageResult<SportDto> queryAll(SportQueryCriteria criteria, Pageable pageable);

    /**
    * Query all data without pagination
    * @param criteria criteria parameters
    * @return List<SportDto>
    */
    List<SportDto> queryAll(SportQueryCriteria criteria);

    /**
     * Query by ID
     * @param id ID
     * @return SportDto
     */
    SportDto findById(Long id);

    /**
    * Create
    * @param resources /
    * @return ExecutionResult containing the created entity ID
    */
    ExecutionResult create(Sport resources);

    /**
    * Edit
    * @param resources /
    * @return ExecutionResult containing the updated entity ID
    */
    ExecutionResult update(Sport resources);

    /**
    * Multi-select delete
    * @param ids /
    * @return ExecutionResult containing the deleted IDs
    */
    ExecutionResult deleteAll(Long[] ids);

    /**
    * Export data
    * @param all data to be exported
    * @param response /
    * @throws IOException /
    */
    void download(List<SportDto> all, HttpServletResponse response) throws IOException;
}