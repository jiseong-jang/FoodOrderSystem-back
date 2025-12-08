package com.mrdinner.service;

import com.mrdinner.entity.Menu;
import com.mrdinner.entity.MenuType;
import com.mrdinner.entity.StyleType;
import com.mrdinner.menu.MenuComposition;
import com.mrdinner.menu.MenuItemCode;
import com.mrdinner.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PriceCalculationService {
    @Autowired
    private MenuRepository menuRepository;

    public Integer calculateSubTotal(Long menuId, StyleType styleType, Map<String, Integer> customizedQuantities, Integer quantity) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다"));

        // 스타일 추가 금액
        int stylePrice = 0;
        if (styleType == StyleType.GRAND) {
            stylePrice = 10000;
        } else if (styleType == StyleType.DELUXE) {
            stylePrice = 20000;
        }

        // 기본 가격
        int basePrice = menu.getBasePrice();

        // 커스터마이징 가격 변동
        int customizationPrice = 0;
        if (customizedQuantities != null && !customizedQuantities.isEmpty()) {
            for (Map.Entry<String, Integer> entry : customizedQuantities.entrySet()) {
                MenuItemCode code = MenuItemCode.fromValue(entry.getKey());
                if (code == null) {
                    continue;
                }
                Integer customQuantity = entry.getValue();
                if (customQuantity == null || customQuantity <= 0) {
                    continue;
                }

                int defaultQuantity = MenuComposition.getDefaultQuantity(menu.getType(), code);
                // 메뉴에 없는 아이템의 경우 기본 수량이 0이므로, 수량이 있으면 그대로 가격 계산
                int quantityDiff = customQuantity - defaultQuantity;

                if (quantityDiff != 0) {
                    customizationPrice += code.getUnitPrice() * quantityDiff;
                }
            }
        }

        // 총 가격 = (기본가 + 스타일 추가금 + 커스터마이징 변동) * 수량
        return (basePrice + stylePrice + customizationPrice) * quantity;
    }

    public void validateStyleForMenu(MenuType menuType, StyleType styleType) {
        if (menuType == MenuType.CHAMPAGNE_FESTIVAL) {
            if (styleType == StyleType.SIMPLE) {
                throw new RuntimeException("샴페인 축제 디너는 그랜드 또는 디럭스 스타일만 선택 가능합니다");
            }
        }
    }
}

