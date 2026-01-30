package com.example.Commerce.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(com.example.Commerce.controllers..*)")
    public void controllerLayer() {}

    @Around("controllerLayer()")
    public Object logAroundControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String clientIp = request.getRemoteAddr();
        
        Object[] args = joinPoint.getArgs();
        String params = args.length > 0 ? Arrays.toString(args) : "none";
        
        log.info("→ {} {} | Controller: {} | Params: {} | IP: {}", 
            method, path, joinPoint.getSignature().getName(), params, clientIp);
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("← {} {} | Controller: {} | Status: SUCCESS | Time: {}ms", 
                method, path, joinPoint.getSignature().getName(), executionTime);
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.error("← {} {} | Controller: {} | Status: FAILED | Time: {}ms | Error: {} - {}", 
                method, path, joinPoint.getSignature().getName(), executionTime, 
                e.getClass().getSimpleName(), e.getMessage());
            
            throw e;
        }
    }
}
