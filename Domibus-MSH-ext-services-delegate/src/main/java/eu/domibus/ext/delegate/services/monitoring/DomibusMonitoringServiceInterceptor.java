package eu.domibus.ext.delegate.services.monitoring;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.DomibusMonitoringExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Soumya Chandran (azhikso)
 * @since 4.2
 */
@Aspect
@Component
public class DomibusMonitoringServiceInterceptor extends ServiceInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusMonitoringServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.monitoring.DomibusMonitoringServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new DomibusMonitoringExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
