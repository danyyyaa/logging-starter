package com.danya.loggingstarter.util;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.danya.loggingstarter.util.Constants.MASKED_VALUE;

public class JsonMaskingUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonMaskingUtil.class);

    @Autowired
    private LoggingExclusionProperties loggingExclusionProperties;

    @Autowired
    private ObjectMapper objectMapper;

    public String maskBody(Object body) {
        String json = body instanceof String bodyAsString
                ? bodyAsString
                : convertBodyToJson(body);

        List<String> maskFields = loggingExclusionProperties.getMaskFields();

        return maskFields(json, maskFields);
    }

    private String maskFields(String json, List<String> maskFields) {
        try {
            Configuration configuration = Configuration.defaultConfiguration();
            DocumentContext context = JsonPath.using(configuration).parse(json);

            maskFields.forEach(rawFields -> {
                try {
                    String fieldName = cleanJsonPath(rawFields);
                    String jsonPath = "$.." + fieldName;
                    context.map(jsonPath, (o, cfg) -> MASKED_VALUE);
                } catch (PathNotFoundException e) {
                    // Путь не найден, пропускаем
                }
            });

            return context.jsonString();
        } catch (Exception e) {
            log.warn("Ошибка маскирования полей в json", e);
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

    private String convertBodyToJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.warn("Ошибка сериализации тела запроса", e);
            return null;
        }
    }

}
