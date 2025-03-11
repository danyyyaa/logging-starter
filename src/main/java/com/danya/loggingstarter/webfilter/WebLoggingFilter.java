package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.util.HeaderMaskingUtil;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WebLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Autowired(required = false)
    private LoggingExclusionProperties loggingExclusionProperties;

    @Autowired
    private AntPathMatcher matcher;

    @Autowired
    private JsonMaskingUtil jsonMaskingUtil;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isExcludedRequest(request.getRequestURI() + formatQueryString(request))) {
            chain.doFilter(request, response);
            return;
        }

        logRequest(request.getMethod(), request.getRequestURI() + formatQueryString(request), request);
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            super.doFilter(request, wrapper, chain);
            logResponse(
                    request.getMethod(),
                    request.getRequestURI() + formatQueryString(request),
                    wrapper,
                    new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8),
                    (loggingExclusionProperties != null) ? loggingExclusionProperties.getMaskFields() : null
            );
        } finally {
            wrapper.copyBodyToResponse();
        }
    }

    private boolean isExcludedRequest(String requestUri) {
        return loggingExclusionProperties != null
                && loggingExclusionProperties.getExcludePaths().stream()
                .anyMatch(pattern -> matcher.match(pattern, requestUri));
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

    private void logRequest(String method, String requestUri, HttpServletRequest request) {
        List<String> maskHeaders = (loggingExclusionProperties != null)
                ? loggingExclusionProperties.getMaskHeaders()
                : Collections.emptyList();

        String headers = inlineHeaders(request, maskHeaders);
        log.info("Запрос: {} {} {}", method, requestUri, headers);
    }

    private void logResponse(String method, String requestUri,
                             HttpServletResponse response,
                             String responseBody, List<String> maskFields) {
        if (loggingExclusionProperties != null && maskFields != null) {
            responseBody = jsonMaskingUtil.maskFields(responseBody, maskFields);
        }
        log.info("Ответ: {} {} {} {}", method, requestUri, response.getStatus(), responseBody);
    }
}
