package com.example.quitespotfinder.services;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;


@Aspect
@Component
public class ResponseTimeAOP {
    
    @Around("@annotation(com.example.quitespotfinder.annotation.LogExecutionTIme)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
       
        Object proceed = joinPoint.proceed();
       
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        double seconds = duration.toNanos() / 1_000_000_000.0;

        System.out.printf("[AOP Metrics] Method %s executed in %.3f seconds%n",
                joinPoint.getSignature().getName(), seconds);

        return proceed;
    }
    
    
}
