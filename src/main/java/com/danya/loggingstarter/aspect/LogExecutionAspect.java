package com.danya.loggingstarter.aspect;

import com.danya.loggingstarter.annotation.LogExecutionTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
public class LogExecutionAspect {

    private static final Logger log = LoggerFactory.getLogger(LogExecutionAspect.class);

    @Around("@annotation(com.danya.loggingstarter.annotation.LogExecutionTime)")
    public Object aroundExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogExecutionTime originalAnnotation = method.getAnnotation(LogExecutionTime.class);
        String methodName = Optional.ofNullable(originalAnnotation)
                .map(LogExecutionTime::methodName)
                .filter(StringUtils::hasText)
                .orElse(method.getName());

        try {
            return joinPoint.proceed();
        } finally {
            log.info("Время выполнения метода '{}': {} мс", methodName, System.currentTimeMillis() - start);
        }
    }
}
