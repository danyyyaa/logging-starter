package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.util.HeaderMaskingUtil;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.danya.loggingstarter.util.QueryStringUtil.formatQueryString;

public class WebLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Autowired
    private JsonMaskingUtil jsonMaskingUtil;

    @Autowired
    private HeaderMaskingUtil headerMaskingUtil;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        logRequest(request.getMethod(), request.getRequestURI() + formatQueryString(request), request);
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            super.doFilter(request, wrapper, chain);
            logResponse(
                    request.getMethod(),
                    request.getRequestURI() + formatQueryString(request),
                    wrapper,
                    new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8)
            );
        } finally {
            wrapper.copyBodyToResponse();
        }
    }

    private String inlineHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(it -> it, request::getHeader));

        headerMaskingUtil.maskHeaders(headersMap);
        String inlineHeaders = headersMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));

        return "headers={" + inlineHeaders + "}";
    }

    private void logRequest(String method, String requestUri, HttpServletRequest request) {
        String headers = inlineHeaders(request);
        log.info("Запрос: {} {} {}", method, requestUri, headers);
    }

    private void logResponse(String method, String requestUri,
                             HttpServletResponse response,
                             String responseBody) {
        responseBody = jsonMaskingUtil.maskBody(responseBody);
        log.info("Ответ: {} {} {} {}", method, requestUri, response.getStatus(), responseBody);
    }
}
