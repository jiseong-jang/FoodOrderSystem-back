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
public class InventoryDto {
    private String itemCode;
    private String label;
    private Integer quantity;
    private LocalDateTime lastRestocked;
}

