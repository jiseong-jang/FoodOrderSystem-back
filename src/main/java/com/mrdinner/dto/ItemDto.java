package com.mrdinner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String code;
    private String label;
    private Integer unitPrice;
    private Integer defaultQuantity;
    private Integer stockQuantity;
}

