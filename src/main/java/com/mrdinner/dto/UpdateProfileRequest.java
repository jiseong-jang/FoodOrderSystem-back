package com.mrdinner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    private String name;
    private String address;
    private String cardNumber;
    private String cardExpiry;
    private String cardCVC;
    private String cardHolderName;
    private String newPassword;
}

