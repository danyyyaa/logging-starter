package com.danya.loggingstarter;

import com.danya.loggingstarter.aspect.LogExecutionAspect;
import com.danya.loggingstarter.feign.FeignRequestLogger;
import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.service.LoggingService;
import com.danya.loggingstarter.util.HeaderMaskingUtil;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import com.danya.loggingstarter.webfilter.WebLoggingFilter;
import com.danya.loggingstarter.webfilter.WebLoggingRequestControllerAdvice;
import feign.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "logging", value = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingStarterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "logging", value = "log-exec-time", havingValue = "true")
    public LogExecutionAspect logExecutionAspect() {
        return new LogExecutionAspect();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.web-logging", value = "enabled", havingValue = "true", matchIfMissing = true)
    public WebLoggingFilter webLoggingFilter() {
        return new WebLoggingFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.web-logging", value = {"enabled", "log-body"}, havingValue = "true")
    public WebLoggingRequestControllerAdvice webLoggingRequestControllerAdvice() {
        return new WebLoggingRequestControllerAdvice();
    }

    @Bean
    @ConfigurationProperties(prefix = "logging.exclude-paths")
    public LoggingExclusionProperties loggingExclusionProperties() {
        return new LoggingExclusionProperties();
    }

    @Bean
    public JsonMaskingUtil jsonMaskingUtil() {
        return new JsonMaskingUtil();
    }

    @Bean
    public HeaderMaskingUtil headerMaskingUtil() {
        return new HeaderMaskingUtil();
    }

    @Bean
    public LoggingService loggingService() {
        return new LoggingService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.web-logging", value = "log-feign-requests", havingValue = "true")
    public FeignRequestLogger feignRequestLogger() {
        return new FeignRequestLogger();
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.web-logging", value = "log-feign-requests", havingValue = "true")
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}

