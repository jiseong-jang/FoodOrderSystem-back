package com.mrdinner.dto;

import com.mrdinner.entity.DeliveryType;
import com.mrdinner.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private OrderStatus status;
    private DeliveryType deliveryType;
    private LocalDateTime reservationTime;
    private Integer finalPrice;
    private List<OrderItemDto> orderItems;
    private LocalDateTime createdAt;
    private String kitchenStaffId;
    private String deliveryStaffId;
    private CouponDto coupon;
    private String customerAddress;
}

