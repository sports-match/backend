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
package me.zhengjie.modules.system.service;

import me.zhengjie.modules.security.service.dto.UserRegisterDto;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.dto.UserQueryCriteria;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
public interface UserService {

    /**
     * Get current user logged in
     *
     * @return User
     */
    User findCurrentUser();

    /**
     * Verify email and validate whether the email has been verified.
     *
     * @param email User email for verification
     * @return User
     */
    User verifyEmail(String email);

    /**
     * 根据ID查询
     *
     * @param id ID
     * @return /
     */
    UserDto findById(long id);

    /**
     * 新增用户
     *
     * @param user /
     * @return User
     */
    User createUser(User user);

    /**
     * 新增用户
     *
     * @param registerDto /
     * @return User
     */
    User create(UserRegisterDto registerDto);

    /**
     * 编辑用户
     *
     * @param resources /
     * @return ExecutionResult with user ID
     * @throws Exception /
     */
    ExecutionResult update(User resources) throws Exception;

    /**
     * 删除用户
     *
     * @param ids /
     * @return ExecutionResult with count and deleted IDs
     */
    ExecutionResult delete(Set<Long> ids);

    /**
     * 根据用户名查询
     *
     * @param userName /
     * @return /
     */
    UserDto findByName(String userName);

    /**
     * 根据用户名查询
     *
     * @param userName /
     * @return /
     */
    UserDto getLoginData(String userName);

    /**
     * 修改密码
     *
     * @param username        用户名
     * @param encryptPassword 密码
     * @return ExecutionResult with user ID
     */
    ExecutionResult updatePass(String username, String encryptPassword);

    /**
     * 修改头像
     *
     * @param file 文件
     * @return /
     */
    Map<String, String> updateAvatar(MultipartFile file);

    /**
     * 修改邮箱
     *
     * @param username 用户名
     * @param email    邮箱
     */
    void updateEmail(String username, String email);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User findByEmail(String email);

    /**
     * 更新用户邮箱验证状态
     *
     * @param user 用户对象
     * @return ExecutionResult with user ID
     */
    ExecutionResult updateEmailVerificationStatus(User user);

    /**
     * 查询全部
     *
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    PageResult<UserDto> queryAll(UserQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部不分页
     *
     * @param criteria 条件
     * @return /
     */
    List<UserDto> queryAll(UserQueryCriteria criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 用户自助修改资料
     *
     * @param resources /
     * @return ExecutionResult with user ID
     */
    ExecutionResult updateCenter(User resources);

    /**
     * 重置密码
     *
     * @param ids 用户id
     * @param pwd 密码
     * @return ExecutionResult with count and reset user IDs
     */
    ExecutionResult resetPwd(Set<Long> ids, String pwd);
}
