package com.mrdinner.service;

import com.mrdinner.entity.Inventory;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class InventorySchedulerService {
    @Autowired
    private InventoryRepository inventoryRepository;

    // 일일 보충: 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void restockDaily() {
        Map<MenuItemCode, Integer> dailyItems = new HashMap<>();
        dailyItems.put(MenuItemCode.WINE_BOTTLE, 5);
        dailyItems.put(MenuItemCode.CHAMPAGNE, 5);
        dailyItems.put(MenuItemCode.WINE_GLASS, 10);

        for (Map.Entry<MenuItemCode, Integer> entry : dailyItems.entrySet()) {
            restockItem(entry.getKey().name(), entry.getValue());
        }
    }

    // 주간 보충: 일주일에 2번 (월요일과 목요일 자정)
    @Scheduled(cron = "0 0 0 ? * MON,THU")
    @Transactional
    public void restockWeekly() {
        Map<MenuItemCode, Integer> weeklyItems = new HashMap<>();
        weeklyItems.put(MenuItemCode.STEAK, 15);
        weeklyItems.put(MenuItemCode.COFFEE, 30);
        weeklyItems.put(MenuItemCode.SALAD, 10);
        weeklyItems.put(MenuItemCode.EGG_SCRAMBLE, 10);
        weeklyItems.put(MenuItemCode.BACON, 10);
        weeklyItems.put(MenuItemCode.BREAD, 10);
        weeklyItems.put(MenuItemCode.BAGUETTE, 20);
        weeklyItems.put(MenuItemCode.COFFEE_POT, 5);

        for (Map.Entry<MenuItemCode, Integer> entry : weeklyItems.entrySet()) {
            restockItem(entry.getKey().name(), entry.getValue());
        }
    }

    private void restockItem(String itemName, Integer amount) {
        Inventory inventory = inventoryRepository.findByItemName(itemName)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setItemName(itemName);
                    newInventory.setQuantity(0);
                    return inventoryRepository.save(newInventory);
                });

        inventory.setQuantity(inventory.getQuantity() + amount);
        inventory.setLastRestocked(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }
}

