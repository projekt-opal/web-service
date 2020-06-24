package org.dice_research.opal.webservice.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogMonitor {

    private static final Logger logger = LoggerFactory.getLogger(LogMonitor.class);

    @Before(value = "execution(* org.dice_research.opal.webservice.control..*(..)) ||" +
            " execution(* org.dice_research.opal.webservice.services..*(..))")
    public void beforeLogger(JoinPoint joinPoint) {
        logger.debug("Before Call: {}.{}({})", joinPoint.getThis().getClass().getName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    @After(value = "execution(* org.dice_research.opal.webservice.control..*(..)) ||" +
            " execution(* org.dice_research.opal.webservice.services..*(..))")
    public void afterLogger(JoinPoint joinPoint) {
        logger.debug("After Call: {}.{}({})", joinPoint.getThis().getClass().getName(),
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }
}
