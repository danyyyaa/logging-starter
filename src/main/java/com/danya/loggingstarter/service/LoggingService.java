package com.danya.loggingstarter.service;

import com.danya.loggingstarter.dto.RequestDirection;
import com.danya.loggingstarter.util.HeaderMaskingUtil;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoggingService {

    private static final Logger log = LoggerFactory.getLogger(LoggingService.class);

    @Autowired
    private JsonMaskingUtil jsonMaskingUtil;

    @Autowired
    private HeaderMaskingUtil headerMaskingUtil;

    public void logRequest(String method, String requestUri, HttpServletRequest request) {
        String headers = inlineHeaders(request);
        log.info("Запрос: {} {} {} {}", RequestDirection.IN, method, requestUri, headers);
    }

    public void logResponse(String method, String requestUri,
                            HttpServletResponse response,
                            String responseBody) {
        responseBody = jsonMaskingUtil.maskBody(responseBody);
        log.info("Ответ: {} {} {} {} body = {}", RequestDirection.OUT, method, requestUri, response.getStatus(), responseBody);
    }

    public void logFeignRequest(Request request) {
        String method = request.httpMethod().name();
        String requestUri = request.url();
        String headers = inlineHeaders(request.headers());
        String body = new String(request.body(), StandardCharsets.UTF_8);

        log.info("Запрос: {} {} {} {} body = {}", RequestDirection.IN, method, requestUri, headers, body);
    }

    public void logFeignResponse(Response response, String responseBody) {
        String url = response.request().url();
        String maskedBody = jsonMaskingUtil.maskBody(responseBody);
        String method = response.request().httpMethod().name();
        int status = response.status();

        log.info("Ответ: {} {} {} {} body = {}", RequestDirection.OUT, method, url, status, maskedBody);
    }

    private String inlineHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(it -> it, request::getHeader));

        Map<String, String> maskedHeaders = headerMaskingUtil.getMaskedHeaders(headersMap);
        String inlineHeaders = maskedHeaders.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));

        return "headers={" + inlineHeaders + "}";
    }

    private String inlineHeaders(Map<String, Collection<String>> headersMap) {
        String inlineHeaders = headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = String.join(",", entry.getValue());
                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(","));

        return "headers={" + inlineHeaders + "}";
    }

}
