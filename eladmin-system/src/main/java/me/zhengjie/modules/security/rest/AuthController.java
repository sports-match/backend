package me.zhengjie.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import com.srr.organizer.service.EventOrganizerService;
import com.srr.player.dto.PlayerAssessmentStatusDto;
import com.srr.player.service.PlayerService;
import com.srr.sport.service.SportService;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.annotation.Log;
import me.zhengjie.annotation.rest.AnonymousDeleteMapping;
import me.zhengjie.annotation.rest.AnonymousGetMapping;
import me.zhengjie.annotation.rest.AnonymousPostMapping;
import me.zhengjie.exception.BadRequestException;
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
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.VerifyService;
import me.zhengjie.modules.system.service.impl.UserFacade;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.RedisUtils;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 * Authorization, get user details based on token
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "System: System authorization interface")
public class AuthController {
    private static final String REGISTER_KEY_PREFIX = "register:email:";

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
    private final PlayerService playerService;
    private final EventOrganizerService eventOrganizerService;
    private final UserFacade userFacade;
    private final SportService sportService;

    @Log("login")
    @ApiOperation("login")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) {
//        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        String password = authUser.getPassword();

        JwtUserDto jwtUser = userDetailsService.loadUserByUsername(authUser.getUsername());
        if (!passwordEncoder.matches(password, jwtUser.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        String token = tokenProvider.createToken(jwtUser);
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("token", properties.getTokenStartWith() + token);
        authInfo.put("user", jwtUser);

        if (jwtUser.getUser().getUserType() != null) {
            Long userId = jwtUser.getUser().getId();
            String userType = jwtUser.getUser().getUserType().name();
            switch (userType) {
                case "PLAYER" -> {
                    // verify and set player assessment status
                    final var badminton = sportService.getBadminton();
                    final PlayerAssessmentStatusDto assessmentStatus = playerService.checkAssessmentStatus(badminton.getId(), userId);
                    authInfo.put("playerId", assessmentStatus.getPlayerId());
                    authInfo.put("assessmentStatus", assessmentStatus);
                }
                // set organizer information
                case "ORGANIZER" -> eventOrganizerService.findByUserId(userId)
                        .ifPresent(organizer -> {
                            authInfo.put("organizerId", organizer.getId());
                            authInfo.put("organizerInfo", organizer);
                            authInfo.put("completedClubSelection", !organizer.getClubs().isEmpty());
                        });
            }
        }

        if (loginProperties.isSingleLogin()) {
            onlineUserService.kickOutForUsername(authUser.getUsername());
        }

        onlineUserService.save(jwtUser, token, request);
        return ResponseEntity.ok(authInfo);
    }

    @ApiOperation("get logged in user")
    @GetMapping(value = "/info")
    public ResponseEntity<UserDetails> getUserInfo() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(jwtUser);
    }

    @ApiOperation("get verification code")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // Get the result of the operation
        Captcha captcha = captchaConfig.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        // When verification code type is arithmetic and length >= 2, captcha.text() result may be a floating point
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.ARITHMETIC.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // Save
        redisUtils.set(uuid, captchaValue, captchaConfig.getExpiration(), TimeUnit.MINUTES);
        // Verification code information
        Map<String, Object> imgResult = new HashMap<>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("Logout")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("register user")
    @AnonymousPostMapping(value = "/register")
    public ResponseEntity<Object> register(@Valid @RequestBody UserRegisterDto registerDto) {
        final ExecutionResult user = userFacade.createUserTransactional(registerDto);
        final Long newUserId = user.id();
        log.info("New user registered: {}", newUserId);
        userFacade.sendEmail(registerDto.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", newUserId);
        response.put("email", registerDto.getEmail());
        response.put("username", registerDto.getUsername());
        response.put("requireEmailVerification", true);
        response.put("message", "Please check your email to verify your account");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @ApiOperation("Verify email")
    @AnonymousPostMapping(value = "/verify-email")
    public ResponseEntity<Object> verifyEmail(@Validated @RequestBody EmailVerificationDto verificationDto) {
        String key = REGISTER_KEY_PREFIX + verificationDto.getEmail();

        // Validate OTP code
        verifyService.validated(key, verificationDto.getCode());

        // Verify email
        User user = userService.verifyEmail(verificationDto.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);

        ExecutionResult result = userService.updateEmailVerificationStatus(user);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation("Resend verification email")
    @AnonymousPostMapping(value = "/resend-verification")
    public ResponseEntity<Object> resendVerification(@RequestParam String email) {
        userService.verifyEmail(email); // Verify email before sending mail
        final var emailVo = userFacade.sendEmail(email);
        return new ResponseEntity<>(emailVo, HttpStatus.OK);
    }
}
