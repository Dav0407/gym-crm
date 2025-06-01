package com.epam.gym_crm.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Before("execution(* com.epam.gym_crm..*Service.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Operation started: {} | Args: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "execution(* com.epam.gym_crm..*Service.*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logger.info("Operation finished: {} | Result: {}", joinPoint.getSignature(), result);
    }
}
