package me.zhengjie.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import com.srr.enumeration.Format;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.service.EventOrganizerService;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.PlayerAssessmentStatusDto;
import com.srr.player.repository.PlayerSportRatingRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final UserFacade userFacade;
    private final SportService sportService;

    @Log("login")
    @ApiOperation("login")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) {
        // 密码解密
//        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        String password = authUser.getPassword();

        // 获取用户信息
        JwtUserDto jwtUser = userDetailsService.loadUserByUsername(authUser.getUsername());
        // 验证用户密码
        if (!passwordEncoder.matches(password, jwtUser.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        if (!jwtUser.isEnabled()) {
            throw new BadRequestException("账号未激活");
        }
        // 生成令牌
        String token = tokenProvider.createToken(jwtUser);
        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("token", properties.getTokenStartWith() + token);
        authInfo.put("user", jwtUser);

        // --- Add entity id if exists (playerId or organizerId) ---
        if (jwtUser.getUser() != null && jwtUser.getUser().getUserType() != null) {
            if (jwtUser.getUser().getUserType().name().equals("PLAYER")) {
                Player player = playerService.findByUserId(jwtUser.getUser().getId());
                if (player != null) {
                    authInfo.put("playerId", player.getId());
                }
            } else if (jwtUser.getUser().getUserType().name().equals("ORGANIZER")) {
                List<EventOrganizer> organizers = eventOrganizerService.findByUserId(jwtUser.getUser().getId());
                if (organizers != null && !organizers.isEmpty()) {
                    authInfo.put("organizerInfo", organizers.get(0));
                }
            }
        }
        // ---------------------------------------------------------

        // 评估状态（仅对玩家）
        if (jwtUser.getUser() != null && jwtUser.getUser().getUserType() != null && jwtUser.getUser().getUserType().name().equals("PLAYER")) {
            Player player = playerService.findByUserId(jwtUser.getUser().getId());
            boolean isAssessmentCompleted = false;
            final var badminton = sportService.getBadminton();
            String message = "Please complete your self-assessment before joining any events.";
            if (player != null) {
                Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(player.getId(), badminton.getId(), Format.DOUBLE);
                if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
                    isAssessmentCompleted = true;
                    message = "Self-assessment completed.";
                }
            }
            PlayerAssessmentStatusDto assessmentStatus = new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
            authInfo.put("assessmentStatus", assessmentStatus);
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

    @ApiOperation("get logged in user")
    @GetMapping(value = "/info")
    public ResponseEntity<UserDetails> getUserInfo() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return ResponseEntity.ok(jwtUser);
    }

    @ApiOperation("get verification code")
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

        final var emailVo = userFacade.sendEmail(email);
        return new ResponseEntity<>(emailVo, HttpStatus.OK);
    }
}
