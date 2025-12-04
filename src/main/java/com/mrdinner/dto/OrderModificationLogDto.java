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
public class OrderModificationLogDto {
    private Long id;
    private Long orderId;
    private LocalDateTime modifiedAt;
    private String previousOrderItems;
    private String newOrderItems;
    private Integer priceDifference;
}

