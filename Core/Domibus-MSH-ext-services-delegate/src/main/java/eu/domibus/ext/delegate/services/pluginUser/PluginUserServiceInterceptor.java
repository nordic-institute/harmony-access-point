package eu.domibus.ext.delegate.services.pluginUser;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.PluginUserExtServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Arun Raj
 * @since 5.0
 */
@Aspect
@Component
public class PluginUserServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginUserServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.pluginUser.PluginUserServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new PluginUserExtServiceException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
