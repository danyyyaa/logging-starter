package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Optional;

@ControllerAdvice
public class WebLoggingRequestControllerAdvice extends RequestBodyAdviceAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingRequestControllerAdvice.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JsonMaskingUtil jsonMaskingUtil;

    @Autowired(required = false)
    private LoggingExclusionProperties loggingExclusionProperties;

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);

        boolean isExcludedPath = loggingExclusionProperties != null
                && loggingExclusionProperties.getExcludePaths().stream().anyMatch(requestUri::startsWith);
        if (isExcludedPath) {
            return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
        }

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации тела запроса", e);
            throw new RuntimeException("Ошибка сериализации тела запроса", e);
        }

        if (loggingExclusionProperties != null) {
            bodyJson = jsonMaskingUtil.maskFields(bodyJson, loggingExclusionProperties.getMaskFields());
        }
        log.info("Тело запроса: {} {} {}", method, requestUri, bodyJson);

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String requestUri = request.getRequestURI() + formatQueryString(request);
        return !isExcludedPath(requestUri);
    }

    private String formatQueryString(HttpServletRequest request) {
        return Optional.ofNullable(request.getQueryString())
                .map(qs -> "=" + qs)
                .orElse(Strings.EMPTY);
    }

    private boolean isExcludedPath(String requestUri) {
        if (loggingExclusionProperties == null) {
            return false;
        }
        return loggingExclusionProperties.getExcludePaths().stream().anyMatch(requestUri::startsWith);
    }
}
