package com.example.Commerce.Aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    private final Map<String, Long> dbFetchTimes = new ConcurrentHashMap<>();

    @Around("execution(* com.example.Commerce.Repositories..*(..))")
    public Object monitorDatabaseFetch(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            dbFetchTimes.put(methodName, executionTime);
            log.info("DB Query: {} took {}ms", methodName, executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            dbFetchTimes.put(methodName + "_ERROR", executionTime);
            throw e;
        }
    }

    public Map<String, Long> getDbFetchTimes() {
        return new ConcurrentHashMap<>(dbFetchTimes);
    }

    public void clearMetrics() {
        dbFetchTimes.clear();
    }
}