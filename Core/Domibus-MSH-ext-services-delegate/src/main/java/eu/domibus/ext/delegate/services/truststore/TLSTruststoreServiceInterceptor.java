package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.TruststoreExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Soumya Chnadran
 * @since 5.1
 */
@Aspect
@Component
public class TLSTruststoreServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSTruststoreServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.truststore.TLSTruststoreServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new TruststoreExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
