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
import me.zhengjie.config.properties.FileProperties;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserCacheManager;
import me.zhengjie.modules.security.service.dto.UserRegisterDto;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.repository.RoleRepository;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.RoleSmallDto;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.dto.UserQueryCriteria;
import me.zhengjie.modules.system.service.mapstruct.UserMapper;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FileProperties properties;
    private final RedisUtils redisUtils;
    private final UserCacheManager userCacheManager;
    private final OnlineUserService onlineUserService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public PageResult<UserDto> queryAll(UserQueryCriteria criteria, Pageable pageable) {
        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(userMapper::toDto));
    }

    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return userMapper.toDto(users);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(long id) {
        String key = CacheKey.USER_ID + id;
        User user = redisUtils.get(key, User.class);
        if (user == null) {
            user = userRepository.findById(id).orElseGet(User::new);
            ValidationUtil.isNull(user.getId(), "User", "id", id);
            redisUtils.set(key, user, 1, TimeUnit.DAYS);
        }
        return userMapper.toDto(user);
    }

    private User createUserEntity(UserRegisterDto registerDto) {
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setNickName(registerDto.getNickName());
        user.setEmail(registerDto.getEmail());
        user.setPhone(registerDto.getPhone());
        user.setEnabled(false);
        user.setEmailVerified(false);
        user.setUserType(registerDto.getUserType());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(UserRegisterDto registerDto) {
        final User userResource = createUserEntity(registerDto);
        return createUser(userResource);
    }


    public User createUser(User user) {
        final var userData = userRepository.findByUsernameOrEmailOrPhone(user.getUsername(),
                user.getEmail(), user.getPhone());

        if (userData == null) {
            return userRepository.save(user);
        }

        if (userData.getUsername().equals(user.getUsername())) {
            throw new EntityExistException(User.class, "username", user.getUsername());
        }
        if (userData.getEmail().equals(user.getEmail())) {
            throw new EntityExistException(User.class, "email", user.getEmail());
        }
        if (userData.getPhone().equals(user.getPhone())) {
            throw new EntityExistException(User.class, "phone", user.getPhone());
        }

        return userData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(User resources) throws Exception {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", resources.getId());
        User user1 = userRepository.findByUsername(resources.getUsername());
        User user2 = userRepository.findByEmail(resources.getEmail());
        User user3 = userRepository.findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (user2 != null && !user.getId().equals(user2.getId())) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }
        if (user3 != null && !user.getId().equals(user3.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        // If the user's role changes
        if (!resources.getRoles().equals(user.getRoles())) {
            redisUtils.del(CacheKey.DATA_USER + resources.getId());
            redisUtils.del(CacheKey.MENU_USER + resources.getId());
            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
            redisUtils.del(CacheKey.ROLE_USER + resources.getId());
        }
        // If the user is disabled, clear the user's login information
        if (!resources.getEnabled()) {
            onlineUserService.kickOutForUsername(resources.getUsername());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        userRepository.save(user);
        // Clear cache
        delCaches(user.getId(), user.getUsername());
        return new ExecutionResult(user.getId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult updateCenter(User resources) {
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        User user1 = userRepository.findByPhone(resources.getPhone());
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "phone", resources.getPhone());
        }
        user.setNickName(resources.getNickName());
        user.setPhone(resources.getPhone());
        userRepository.save(user);
        // Clear cache
        delCaches(user.getId(), user.getUsername());
        return new ExecutionResult(user.getId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult delete(Set<Long> ids) {
        Map<String, Object> result = new HashMap<>();
        result.put("count", ids.size());
        result.put("ids", ids);

        for (Long id : ids) {
            // Clear cache
            UserDto user = findById(id);
            delCaches(user.getId(), user.getUsername());
        }
        userRepository.deleteAllByIdIn(ids);
        return new ExecutionResult(null, result);
    }

    @Override
    public UserDto findByName(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new EntityNotFoundException(User.class, "name", userName);
        } else {
            return userMapper.toDto(user);
        }
    }

    @Override
    public UserDto getLoginData(String userName) {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            return null;
        } else {
            return userMapper.toDto(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult updatePass(String username, String pass) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException(User.class, "username", username);
        }

        userRepository.updatePass(username, pass, new Date());
        flushCache(username);
        return new ExecutionResult(user.getId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult resetPwd(Set<Long> ids, String pwd) {
        Map<String, Object> result = new HashMap<>();
        result.put("count", ids.size());
        result.put("ids", ids);

        userRepository.resetPwd(ids, pwd);
        return new ExecutionResult(null, result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {
        // File size validation
        FileUtil.checkSize(properties.getAvatarMaxSize(), multipartFile.getSize());
        // Validate file upload format
        String image = "gif jpg png jpeg";
        String fileType = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        if (fileType != null && !image.contains(fileType)) {
            throw new BadRequestException("File format error! Only supports " + image + " format");
        }
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
        String oldPath = user.getAvatarPath();
        File file = FileUtil.upload(multipartFile, properties.getPath().getAvatar());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        user.setAvatarName(file.getName());
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            FileUtil.del(oldPath);
        }
        @NotBlank String username = user.getUsername();
        flushCache(username);
        return new HashMap<String, String>(1) {{
            put("avatar", file.getName());
        }};
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        userRepository.updateEmail(username, email);
        flushCache(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult updateEmailVerificationStatus(User user) {
        User existingUser = userRepository.findById(user.getId()).orElseGet(User::new);
        ValidationUtil.isNull(existingUser.getId(), "User", "id", user.getId());

        existingUser.setEmailVerified(user.getEmailVerified());
        existingUser.setEnabled(user.getEnabled());
        userRepository.save(existingUser);

        // Clear cache
        delCaches(existingUser.getId(), existingUser.getUsername());

        return new ExecutionResult(existingUser.getId(), null);
    }

    @Override
    public void download(List<UserDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDTO : queryAll) {
            List<String> roles = userDTO.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("Username", userDTO.getUsername());
            map.put("Nickname", userDTO.getNickName());
            map.put("Phone", userDTO.getPhone());
            map.put("Email", userDTO.getEmail());
            map.put("Status", userDTO.getEnabled() ? "Enabled" : "Disabled");
            map.put("Creation date", userDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    /**
     * Clear cache
     *
     * @param id /
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        flushCache(username);
    }

    /**
     * Clear user cache information at login
     *
     * @param username /
     */
    private void flushCache(String username) {
        userCacheManager.cleanUserCache(username);
    }
}
