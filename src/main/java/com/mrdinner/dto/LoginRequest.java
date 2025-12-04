package com.mrdinner.dto;

import com.mrdinner.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "아이디는 필수입니다")
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private UserRole role;
}

