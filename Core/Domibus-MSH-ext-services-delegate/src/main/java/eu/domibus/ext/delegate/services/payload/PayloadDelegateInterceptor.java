package eu.domibus.ext.delegate.services.payload;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.exceptions.PayloadExtException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PayloadDelegateInterceptor extends ServiceInterceptor {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(PayloadDelegateInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.payload.PayloadExtDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if(e instanceof DomibusServiceExtException) {
            return e;
        }
        return new PayloadExtException(e);
    }

    @Override
    public IDomibusLogger getLogger() {
        return LOG;
    }
}