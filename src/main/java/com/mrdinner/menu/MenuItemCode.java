package com.mrdinner.menu;

import java.util.Arrays;

public enum MenuItemCode {
    STEAK("스테이크", 45000),
    WINE_BOTTLE("와인(병)", 60000),
    CHAMPAGNE("샴페인", 90000),
    WINE_GLASS("와인(잔)", 12000),
    COFFEE("커피", 6000),
    SALAD("샐러드", 18000),
    EGG_SCRAMBLE("에그 스크램블", 15000),
    BACON("베이컨", 7000),
    BREAD("빵", 4000),
    BAGUETTE("바게트빵", 5000),
    COFFEE_POT("커피 포트", 10000);

    private final String labelKo;
    private final int unitPrice;

    MenuItemCode(String labelKo, int unitPrice) {
        this.labelKo = labelKo;
        this.unitPrice = unitPrice;
    }

    public String getLabelKo() {
        return labelKo;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public static MenuItemCode fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return MenuItemCode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return Arrays.stream(values())
                    .filter(code -> code.labelKo.equals(value))
                    .findFirst()
                    .orElse(null);
        }
    }
}

