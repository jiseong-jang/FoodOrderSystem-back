package com.mrdinner.dto;

import com.mrdinner.entity.StyleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderItemRequest {
    @NotNull(message = "메뉴 ID는 필수입니다")
    private Long menuId;

    @NotNull(message = "스타일 타입은 필수입니다")
    private StyleType styleType;

    private Map<String, Integer> customizedQuantities;

    @NotNull(message = "수량은 필수입니다")
    @Positive(message = "수량은 1 이상이어야 합니다")
    private Integer quantity;
}

