package com.hz.api.admin.web.config.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAop {

    @Pointcut("execution( * com.hz.api.admin.web.app.*.*(..))")
    public void log(){};

    @Around("log()")
    public Object pushLog(ProceedingJoinPoint point) throws Throwable {
        log.info("请求参数：{}", point.getArgs());
        return point.proceed();
    }
}
