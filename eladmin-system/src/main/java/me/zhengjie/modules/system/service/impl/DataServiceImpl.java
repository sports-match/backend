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
package me.zhengjie.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.modules.system.service.DataService;
import me.zhengjie.modules.system.service.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zheng Jie
 * @description 数据权限服务实现
 * @date 2020-05-07
 **/
@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    /**
     * 获取数据权限
     * @param user /
     * @return / 返回一个空列表，因为部门功能被移除
     */
    @Override
    public List<Long> getDeptIds(UserDto user) {
        // Departments and department-based data scopes are being removed.
        // Therefore, always return an empty list, signifying no department-based data restrictions.
        return new ArrayList<>();
    }
}
