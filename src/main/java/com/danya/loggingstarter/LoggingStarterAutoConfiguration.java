package com.danya.loggingstarter;

import com.danya.loggingstarter.aspect.LogExecutionAspect;
import com.danya.loggingstarter.property.LoggingExclusionProperties;
import com.danya.loggingstarter.util.JsonMaskingUtil;
import com.danya.loggingstarter.webfilter.WebLoggingFilter;
import com.danya.loggingstarter.webfilter.WebLoggingRequestControllerAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.AntPathMatcher;

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
    @ConditionalOnProperty(prefix = "logging.exclude-paths", value = "enabled", havingValue = "true")
    public LoggingExclusionProperties loggingExclusionProperties() {
        return new LoggingExclusionProperties();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .simpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss")
                .build();
    }

    @Bean
    public JsonMaskingUtil jsonMaskingUtil() {
        return new JsonMaskingUtil();
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
