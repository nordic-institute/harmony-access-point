package eu.domibus.ext.delegate.services.pmode;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.PModeExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@Aspect
@Component
public class PModeServiceServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeServiceServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.pmode.PModeServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new PModeExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
