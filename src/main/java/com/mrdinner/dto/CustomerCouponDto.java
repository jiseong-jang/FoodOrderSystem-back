package com.mrdinner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCouponDto {
    private Long id;
    private CouponDto coupon;
    private Boolean isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime receivedAt;
}

