package com.mrdinner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {
    @NotEmpty(message = "주문 아이템은 필수입니다")
    @Valid
    private List<UpdateOrderItemRequest> orderItems;
}

