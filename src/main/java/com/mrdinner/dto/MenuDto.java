package com.mrdinner.dto;

import com.mrdinner.entity.MenuType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {
    private Long id;
    private MenuType type;
    private Integer basePrice;
    private List<ItemDto> items;
}

