package eu.domibus.ext.delegate.services.jms;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.DomainExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class JMSExtServiceDelegateInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSExtServiceDelegateInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.jms.JMSExtServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if (e instanceof eu.domibus.api.messaging.MessageNotFoundException) {
            return new MessageNotFoundException(e);
        }
        return new DomainExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}