package me.zhengjie.modules.security.service.dto;

import lombok.Data;
import me.zhengjie.modules.security.service.enums.UserType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class UserRegisterDto implements Serializable {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Nickname cannot be blank")
    private String nickName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{09,11}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "User type cannot be blank")
    private UserType userType;

    private Long clubId;
}
