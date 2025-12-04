package com.mrdinner.dto;

import com.mrdinner.entity.StyleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private MenuDto menu;
    private StyleType selectedStyle;
    private Map<String, Integer> customizedQuantities;
    private Integer quantity;
    private Integer subTotal;
}

