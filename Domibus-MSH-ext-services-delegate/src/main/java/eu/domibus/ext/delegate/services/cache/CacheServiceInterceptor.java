package eu.domibus.ext.delegate.services.cache;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.CacheExtServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
@Aspect
@Component
public class CacheServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CacheServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.cache.CacheServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new CacheExtServiceException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
