package com.mrdinner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest {
    @NotBlank(message = "쿠폰 코드는 필수입니다")
    private String code;

    @NotNull(message = "할인 금액은 필수입니다")
    @Min(value = 1, message = "할인 금액은 1원 이상이어야 합니다")
    private Integer discountAmount;
}

