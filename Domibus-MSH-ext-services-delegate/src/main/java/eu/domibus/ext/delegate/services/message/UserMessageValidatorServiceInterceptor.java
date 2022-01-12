package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserMessageValidatorServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageValidatorServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.message.UserMessageValidatorServiceDelegateImpl.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if (e instanceof DomibusServiceExtException) {
            return e;
        }
        return new UserMessageExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}