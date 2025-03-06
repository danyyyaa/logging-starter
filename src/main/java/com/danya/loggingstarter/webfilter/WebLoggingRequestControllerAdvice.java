package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private HttpServletRequest request;

    @Autowired(required = false)
    private LoggingExclusionProperties loggingExclusionProperties;

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);

        boolean condition = loggingExclusionProperties != null
                && loggingExclusionProperties.getExcludePaths().stream().anyMatch(requestUri::startsWith);
        if (condition) {
            return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
        }

        try {
            String bodyJson = objectMapper.writeValueAsString(body);
            if (loggingExclusionProperties != null) {
                bodyJson = JsonMaskingUtil.maskFields(bodyJson, loggingExclusionProperties.getMaskFields());
            }
            log.info("Тело запроса: {} {} {}", method, requestUri, bodyJson);
        } catch (Exception e) {
            log.error("Ошибка маскирования полей в json", e);
        }

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    private String formatQueryString(HttpServletRequest request) {
        return Optional.ofNullable(request.getQueryString())
                .map(qs -> "=" + qs)
                .orElse(Strings.EMPTY);
    }
}
