package com.mrdinner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "아이디는 필수입니다")
    private String id;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자리 이상이어야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String confirmPassword;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    @NotBlank(message = "카드번호는 필수입니다")
    private String cardNumber;

    @NotBlank(message = "유효기간은 필수입니다")
    private String cardExpiry;

    @NotBlank(message = "CVC는 필수입니다")
    private String cardCVC;

    @NotBlank(message = "카드소유자명은 필수입니다")
    private String cardHolderName;

    @NotNull(message = "개인정보 활용 동의는 필수입니다")
    private Boolean privacyAgreed;
}

