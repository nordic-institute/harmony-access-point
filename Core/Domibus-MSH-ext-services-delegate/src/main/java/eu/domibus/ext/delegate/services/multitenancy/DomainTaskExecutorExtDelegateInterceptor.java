package eu.domibus.ext.delegate.services.multitenancy;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.DomainTaskExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DomainTaskExecutorExtDelegateInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainTaskExecutorExtDelegateInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.multitenancy.DomainTaskExecutorExtDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new DomainTaskExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}