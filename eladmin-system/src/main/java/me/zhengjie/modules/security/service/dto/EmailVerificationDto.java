package me.zhengjie.modules.security.service.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class EmailVerificationDto implements Serializable {
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    
    @NotBlank(message = "Verification code cannot be blank")
    private String code;
}
