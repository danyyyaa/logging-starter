package com.danya.loggingstarter.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonMaskingUtil {

    private static final String MASKED_VALUE = "****";
    private static final Logger log = LoggerFactory.getLogger(JsonMaskingUtil.class);

    public String maskFields(String json, List<String> maskFields) {
        if (json == null || (!json.trim().startsWith("{") && !json.trim().startsWith("["))) {
            return json;
        }
        try {
            DocumentContext context = JsonPath.parse(json);

            maskFields.forEach(rawFields -> {
                try {
                    String fieldName = cleanJsonPath(rawFields);
                    String jsonPath = "$.." + fieldName;
                    context.set(jsonPath, MASKED_VALUE);
                } catch (PathNotFoundException e) {
                    // Путь не найден, пропускаем
                }
            });

            return context.jsonString();
        } catch (Exception e) {
            log.error("Ошибка маскирования полей в json", e);
            return json;
        }
    }

    private String cleanJsonPath(String rawFields) {
        String cleaned = rawFields.replaceAll("<[^>]+>", "").trim();

        if (cleaned.startsWith("$.")) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("$")) {
            cleaned = cleaned.substring(1);
        }

        return cleaned.contains(".")
                ? cleaned.substring(cleaned.lastIndexOf('.') + 1)
                : cleaned;
    }
}
