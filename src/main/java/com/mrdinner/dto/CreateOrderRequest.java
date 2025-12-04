package com.mrdinner.dto;

import com.mrdinner.entity.DeliveryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "배달 타입은 필수입니다")
    private DeliveryType deliveryType;

    private LocalDateTime reservationTime;
}

