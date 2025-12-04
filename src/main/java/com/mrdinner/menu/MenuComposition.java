package com.mrdinner.menu;

import com.mrdinner.entity.MenuType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MenuComposition {
    private static final Map<MenuType, Map<MenuItemCode, Integer>> COMPOSITIONS;
    private static final Map<MenuType, Integer> BASE_PRICES;

    static {
        Map<MenuType, Map<MenuItemCode, Integer>> map = new EnumMap<>(MenuType.class);
        Map<MenuType, Integer> prices = new EnumMap<>(MenuType.class);

        map.put(MenuType.VALENTINE, immutableMap(builder -> {
            builder.put(MenuItemCode.WINE_BOTTLE, 1);
            builder.put(MenuItemCode.STEAK, 1);
        }));
        prices.put(MenuType.VALENTINE, 100000);

        map.put(MenuType.FRENCH, immutableMap(builder -> {
            builder.put(MenuItemCode.COFFEE, 1);
            builder.put(MenuItemCode.WINE_GLASS, 1);
            builder.put(MenuItemCode.SALAD, 1);
            builder.put(MenuItemCode.STEAK, 1);
        }));
        prices.put(MenuType.FRENCH, 80000);

        map.put(MenuType.ENGLISH, immutableMap(builder -> {
            builder.put(MenuItemCode.EGG_SCRAMBLE, 1);
            builder.put(MenuItemCode.BACON, 1);
            builder.put(MenuItemCode.BREAD, 1);
            builder.put(MenuItemCode.STEAK, 1);
        }));
        prices.put(MenuType.ENGLISH, 70000);

        map.put(MenuType.CHAMPAGNE_FESTIVAL, immutableMap(builder -> {
            builder.put(MenuItemCode.CHAMPAGNE, 1);
            builder.put(MenuItemCode.BAGUETTE, 4);
            builder.put(MenuItemCode.COFFEE_POT, 1);
            builder.put(MenuItemCode.WINE_BOTTLE, 1);
            builder.put(MenuItemCode.STEAK, 2);
        }));
        prices.put(MenuType.CHAMPAGNE_FESTIVAL, 250000);

        COMPOSITIONS = Collections.unmodifiableMap(map);
        BASE_PRICES = Collections.unmodifiableMap(prices);
    }

    private MenuComposition() {
    }

    public static Map<MenuItemCode, Integer> getComposition(MenuType type) {
        return COMPOSITIONS.getOrDefault(type, Collections.emptyMap());
    }

    public static int getDefaultQuantity(MenuType type, MenuItemCode code) {
        return getComposition(type).getOrDefault(code, 0);
    }

    public static int getBasePrice(MenuType type) {
        return BASE_PRICES.getOrDefault(type, 0);
    }

    private static Map<MenuItemCode, Integer> immutableMap(java.util.function.Consumer<Map<MenuItemCode, Integer>> consumer) {
        Map<MenuItemCode, Integer> builder = new LinkedHashMap<>();
        consumer.accept(builder);
        return Collections.unmodifiableMap(builder);
    }
}

