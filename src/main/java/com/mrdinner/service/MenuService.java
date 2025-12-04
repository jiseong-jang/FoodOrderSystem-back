package com.mrdinner.service;

import com.mrdinner.dto.ItemDto;
import com.mrdinner.dto.MenuDto;
import com.mrdinner.entity.Item;
import com.mrdinner.entity.Menu;
import com.mrdinner.entity.MenuType;
import com.mrdinner.menu.MenuComposition;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.repository.ItemRepository;
import com.mrdinner.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ItemRepository itemRepository;

    public List<MenuDto> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MenuDto getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다"));
        return convertToDto(menu);
    }

    public MenuDto getMenuByType(MenuType type) {
        Menu menu = menuRepository.findByType(type)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다"));
        return convertToDto(menu);
    }

    public List<ItemDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다"));
        return convertToDto(item);
    }

    private MenuDto convertToDto(Menu menu) {
        List<Item> remaining = new ArrayList<>(menu.getItems());
        List<ItemDto> itemDtos = new ArrayList<>();

        Map<MenuItemCode, Integer> composition = MenuComposition.getComposition(menu.getType());
        for (Map.Entry<MenuItemCode, Integer> entry : composition.entrySet()) {
            MenuItemCode code = entry.getKey();
            Item matched = remaining.stream()
                    .filter(item -> code == MenuItemCode.fromValue(item.getName()))
                    .findFirst()
                    .orElse(null);
            if (matched != null) {
                remaining.remove(matched);
            }
            itemDtos.add(convertToMenuItemDto(menu.getType(), matched, code, entry.getValue()));
        }

        for (Item leftover : remaining) {
            MenuItemCode code = MenuItemCode.fromValue(leftover.getName());
            itemDtos.add(convertToMenuItemDto(menu.getType(), leftover, code, code != null ? MenuComposition.getDefaultQuantity(menu.getType(), code) : null));
        }

        return new MenuDto(
                menu.getId(),
                menu.getType(),
                menu.getBasePrice(),
                itemDtos
        );
    }

    private ItemDto convertToMenuItemDto(MenuType type, Item item, MenuItemCode code, Integer defaultQtyOverride) {
        Long id = item != null ? item.getId() : null;
        Integer stock = item != null ? item.getStockQuantity() : null;
        String codeValue = code != null ? code.name() : item != null ? item.getName() : null;
        String label = code != null ? code.getLabelKo() : item != null ? item.getName() : "";
        int unitPrice = code != null ? code.getUnitPrice() : item != null ? item.getPrice() : 0;
        Integer defaultQty = defaultQtyOverride != null ? defaultQtyOverride : (code != null ? MenuComposition.getDefaultQuantity(type, code) : null);
        return new ItemDto(id, codeValue, label, unitPrice, defaultQty, stock);
    }

    private ItemDto convertToDto(Item item) {
        MenuItemCode code = MenuItemCode.fromValue(item.getName());
        String codeValue = code != null ? code.name() : item.getName();
        String label = code != null ? code.getLabelKo() : item.getName();
        int unitPrice = code != null ? code.getUnitPrice() : item.getPrice();
        return new ItemDto(item.getId(), codeValue, label, unitPrice, null, item.getStockQuantity());
    }
}

