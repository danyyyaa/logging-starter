package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.service.LoggingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.danya.loggingstarter.util.QueryStringUtil.formatQueryString;

public class WebLoggingFilter extends HttpFilter {

    @Autowired
    private LoggingService loggingService;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        loggingService.logRequest(request.getMethod(), request.getRequestURI() + formatQueryString(request), request);
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            super.doFilter(request, wrapper, chain);
            loggingService.logResponse(
                    request.getMethod(),
                    request.getRequestURI() + formatQueryString(request),
                    wrapper,
                    new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8)
            );
        } finally {
            wrapper.copyBodyToResponse();
        }
    }
}
