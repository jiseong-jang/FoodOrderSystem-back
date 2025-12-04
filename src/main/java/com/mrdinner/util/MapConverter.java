package com.mrdinner.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

@Converter
public class MapConverter implements AttributeConverter<Map<String, Integer>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : attribute.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        Map<String, Integer> map = new HashMap<>();
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("{}")) {
            return map;
        }
        // 간단한 JSON 파싱
        dbData = dbData.trim();
        if (dbData.startsWith("{") && dbData.endsWith("}")) {
            dbData = dbData.substring(1, dbData.length() - 1);
            String[] pairs = dbData.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    Integer value = Integer.parseInt(keyValue[1].trim());
                    map.put(key, value);
                }
            }
        }
        return map;
    }
}

