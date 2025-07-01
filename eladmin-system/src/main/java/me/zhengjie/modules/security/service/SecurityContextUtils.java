package me.zhengjie.modules.security.service;

import me.zhengjie.modules.security.service.dto.JwtUserDto;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.utils.SecurityUtils;

public class SecurityContextUtils {

    public static Long getCurrentUserId() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return jwtUser == null ? null : jwtUser.getUser().getId();
    }

    public static boolean currentUserIsNotNull() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return jwtUser != null;
    }

    public static UserDto getCurrentUser() {
        JwtUserDto jwtUser = (JwtUserDto) SecurityUtils.getCurrentUser();
        return jwtUser == null ? null : jwtUser.getUser();
    }
}
