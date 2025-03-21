package com.danya.loggingstarter.webfilter;

import com.danya.loggingstarter.util.JsonMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

import static com.danya.loggingstarter.util.QueryStringUtil.formatQueryString;

@ControllerAdvice
public class WebLoggingRequestControllerAdvice extends RequestBodyAdviceAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingRequestControllerAdvice.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JsonMaskingUtil jsonMaskingUtil;

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        String method = request.getMethod();
        String requestUri = request.getRequestURI() + formatQueryString(request);

        String maskedJson = jsonMaskingUtil.maskBody(body);
        log.info("Тело запроса: {} {} {}", method, requestUri, maskedJson);

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}
