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
package me.zhengjie.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import com.srr.domain.EventOrganizer;
import com.srr.domain.Player;
import com.srr.dto.PlayerAssessmentStatusDto;
import com.srr.repository.EventOrganizerRepository;
import com.srr.service.PlayerService;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.annotation.Log;
import me.zhengjie.annotation.rest.AnonymousDeleteMapping;
import me.zhengjie.annotation.rest.AnonymousGetMapping;
import me.zhengjie.annotation.rest.AnonymousPostMapping;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.config.CaptchaConfig;
import me.zhengjie.modules.security.config.LoginProperties;
import me.zhengjie.modules.security.config.SecurityProperties;
import me.zhengjie.modules.security.config.enums.LoginCodeEnum;
import me.zhengjie.modules.security.security.TokenProvider;
import me.zhengjie.modules.security.service.OnlineUserService;
import me.zhengjie.modules.security.service.UserDetailsServiceImpl;
import me.zhengjie.modules.security.service.dto.AuthUserDto;
import me.zhengjie.modules.security.service.dto.EmailVerificationDto;
import me.zhengjie.modules.security.service.dto.JwtUserDto;
import me.zhengjie.modules.security.service.dto.UserRegisterDto;
import me.zhengjie.modules.security.service.enums.UserType;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.VerifyService;
import me.zhengjie.service.EmailService;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 * 授权、根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthController {
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final LoginProperties loginProperties;
    private final CaptchaConfig captchaConfig;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final VerifyService verifyService;
    private final UserService userService;
    private final EmailService emailService;
    private final PlayerService playerService;
    private final EventOrganizerRepository eventOrganizerRepository;

    private final String REGISTER_KEY_PREFIX = "register:email:";

    @Log("用户登录")
    @ApiOperation("登录授权")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) {
        // 密码解密
//        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        String password = authUser.getPassword();
        // 查询验证码
//        String code = redisUtils.get(authUser.getUuid(), String.class);
        // 清除验证码
//        redisUtils.del(authUser.getUuid());
//        if (StringUtils.isBlank(code)) {
//            throw new BadRequestException("验证码不存在或已过期");
//        }
//        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
//            throw new BadRequestException("验证码错误");
//        }
        // 获取用户信息
        JwtUserDto jwtUser = userDetailsService.loadUserByUsername(authUser.getUsername());
        // 验证用户密码
        if (!passwordEncoder.matches(password, jwtUser.getPassword())) {
            throw new BadRequestException("登录密码错误");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌
        String token = tokenProvider.createToken(jwtUser);
        // 返回 token 与 用户信息
        Map<String, Object> authInfo = new HashMap<>(3) {{
            put("token", properties.getTokenStartWith() + token);
            put("user", jwtUser);
        }};
        
        // Check if the user is a player and include assessment status in the response
        if (jwtUser.getUser().getUserType() == UserType.PLAYER) {
            // Get player assessment status
            Player player = playerService.findByUserId(jwtUser.getUser().getId());
            if (player != null) {
                Double rateScore = player.getRateScore();
                boolean isAssessmentCompleted = rateScore != null && rateScore > 0;
                
                String message = isAssessmentCompleted 
                    ? "Self-assessment completed." 
                    : "Please complete your self-assessment before joining any events.";
                    
                PlayerAssessmentStatusDto assessmentStatus = new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
                authInfo.put("assessmentStatus", assessmentStatus);
            }
        }
        
        if (loginProperties.isSingleLogin()) {
            // 踢掉之前已经登录的token
            onlineUserService.kickOutForUsername(authUser.getUsername());
        }
        // 保存在线信息
        onlineUserService.save(jwtUser, token, request);
        // 返回登录信息
        return ResponseEntity.ok(authInfo);
    }

    @ApiOperation("获取用户信息")
    @GetMapping(value = "/info")
    public ResponseEntity<UserDetails> getUserInfo() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(jwtUser);
    }

    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 获取运算的结果
        Captcha captcha = captchaConfig.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        //当验证码类型为 arithmetic时且长度 >= 2 时，captcha.text()的结果有几率为浮点型
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.ARITHMETIC.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // 保存
        redisUtils.set(uuid, captchaValue, captchaConfig.getExpiration(), TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("退出登录")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("register user")
    @AnonymousPostMapping(value = "/register")
    public ResponseEntity<Object> register(@Valid @RequestBody UserRegisterDto registerDto) {
        try {
            // Check if user already exists
            if (userService.findByName(registerDto.getUsername()) != null) {
                throw new EntityExistException(User.class, "username", registerDto.getUsername());
            }

            // Check if email is already used
            if (userService.findByEmail(registerDto.getEmail()) != null) {
                throw new EntityExistException(User.class, "email", registerDto.getEmail());
            }
        } catch (EntityNotFoundException ignored) {

        }
        if (registerDto.getUserType() == null) {
            throw new BadRequestException("User type cannot be null");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setNickName(registerDto.getNickName());
        user.setEmail(registerDto.getEmail());
        user.setPhone(registerDto.getPhone());
        user.setEnabled(false); // User is disabled until email is verified
        user.setEmailVerified(false);
        user.setUserType(registerDto.getUserType()); // Save user type

        ExecutionResult result = userService.create(user);

        sendEmail(registerDto.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", result.id());
        response.put("email", registerDto.getEmail());
        response.put("username", registerDto.getUsername());
        response.put("requireEmailVerification", true);
        response.put("message", "Please check your email to verify your account");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ApiOperation("验证邮箱")
    @AnonymousPostMapping(value = "/verify-email")
    public ResponseEntity<Object> verifyEmail(@Validated @RequestBody EmailVerificationDto verificationDto) {
        String key = REGISTER_KEY_PREFIX + verificationDto.getEmail();

        // Validate OTP code
        verifyService.validated(key, verificationDto.getCode());

        // Find user by email
        User user = userService.findByEmail(verificationDto.getEmail());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // Update user status
        user.setEmailVerified(true);
        user.setEnabled(true);
        ExecutionResult result = userService.updateEmailVerificationStatus(user);

        // Create the appropriate entity based on user type
        createUserTypeEntity(user);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation("重新发送验证邮件")
    @AnonymousPostMapping(value = "/resend-verification")
    public ResponseEntity<Object> resendVerification(@RequestParam String email) {
        // Find user by email
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // Check if already verified
        if (user.getEmailVerified()) {
            throw new BadRequestException("邮箱已验证");
        }

        final var emailVo = sendEmail(email);

        return new ResponseEntity<>(emailVo, HttpStatus.OK);
    }

    private EmailVo sendEmail(String email) {
        // Send verification email
        EmailVo emailVo = verifyService.sendEmail(email, REGISTER_KEY_PREFIX);

        final var config = emailService.find();

        // Send email
        emailService.send(emailVo, config);
        return emailVo;
    }

    /**
     * Creates the appropriate entity based on the user's type
     *
     * @param user The user to create entity for
     */
    private void createUserTypeEntity(User user) {
        // Get user type from user entity
        UserType userType = user.getUserType();

        try {

            switch (userType) {
                case PLAYER:
                    createPlayerEntity(user);
                    break;
                case ORGANIZER:
                    createEventOrganizerEntity(user);
                    break;
                case ADMIN:
                    // No entity to create for ADMIN
                    break;
            }
        } catch (IllegalArgumentException e) {
            // Invalid user type, log and ignore
            log.error("Invalid user type: {}", userType, e);
        }
    }

    /**
     * Creates a Player entity for the given user
     *
     * @param user The user to create a Player for
     */
    private void createPlayerEntity(User user) {
        // Check if player already exists
        Player existingPlayer = playerService.findByUserId(user.getId());
        if (existingPlayer != null) {
            return; // Player already exists
        }

        // Create new player
        Player player = new Player();
        player.setName(user.getNickName());
        player.setUserId(user.getId());
        player.setRateScore(0D);
        player.setDescription("Player created upon registration");

        playerService.create(player);
        log.info("Created player for user: {}", user.getUsername());
    }

    /**
     * Creates an EventOrganizer entity for the given user
     *
     * @param user The user to create an EventOrganizer for
     */
    private void createEventOrganizerEntity(User user) {
        // Check if event organizer already exists
        List<EventOrganizer> existingOrganizers = eventOrganizerRepository.findByUserId(user.getId());
        if (!existingOrganizers.isEmpty()) {
            return; // Organizer already exists
        }

        // Create new event organizer
        EventOrganizer organizer = new EventOrganizer();
        organizer.setUserId(user.getId());
        organizer.setDescription("Event organizer created upon registration");

        eventOrganizerRepository.save(organizer);
        log.info("Created event organizer for user: {}", user.getUsername());
    }
}
