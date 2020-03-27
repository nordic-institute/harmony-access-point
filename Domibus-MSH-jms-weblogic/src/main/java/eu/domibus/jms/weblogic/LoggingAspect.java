package eu.domibus.jms.weblogic;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private DomibusLogger LOG = DomibusLoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* eu.domibus.jms.weblogic.*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try{
            return joinPoint.proceed();
        } finally {
            LOG.info("[{}] took [{}] milliseconds", joinPoint.getSignature().toLongString(), System.currentTimeMillis() - start);
        }
    }
}
