package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.util.HeaderMaskingUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WebLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Autowired(required = false)
    private LoggingExclusionProperties loggingExclusionProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);

        List<String> maskHeaders = loggingExclusionProperties != null
                ? loggingExclusionProperties.getMaskHeaders()
                : Collections.emptyList();

        String headers = inlineHeaders(request, maskHeaders);

        List<String> excludePaths = loggingExclusionProperties != null
                ? loggingExclusionProperties.getExcludePaths()
                : Collections.emptyList();

        if (excludePaths.stream().anyMatch(requestUri::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        log.info("Запрос: {} {} {}", method, requestUri, headers);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            super.doFilter(request, responseWrapper, chain);

            String responseBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (loggingExclusionProperties != null) {
                responseBody = maskJsonFields(responseBody, loggingExclusionProperties.getMaskFields());
            }
            log.info("Ответ: {} {} {} {}", method, requestUri, response.getStatus(), responseBody);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    private String inlineHeaders(HttpServletRequest request, List<String> maskHeaders) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(it -> it, request::getHeader));

        HeaderMaskingUtil.maskHeaders(headersMap, maskHeaders);
        String inlineHeaders = headersMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));

        return "headers={" + inlineHeaders + "}";
    }

    private String formatQueryString(HttpServletRequest request) {
        return Optional.ofNullable(request.getQueryString())
                .map(qs -> "=" + qs)
                .orElse(Strings.EMPTY);
    }

    private String maskJsonFields(String json, List<String> maskFields) {
        try {
            JsonNode root = objectMapper.readTree(json);
            maskJsonNode(root, maskFields);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("Ошибка при маскировке полей JSON", e);
            return json;
        }
    }

    private void maskJsonNode(JsonNode node, List<String> maskFields) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (maskFields.contains(entry.getKey())) {
                    objectNode.put(entry.getKey(), "****");
                } else {
                    maskJsonNode(entry.getValue(), maskFields);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                maskJsonNode(arrayItem, maskFields);
            }
        }
    }
}
