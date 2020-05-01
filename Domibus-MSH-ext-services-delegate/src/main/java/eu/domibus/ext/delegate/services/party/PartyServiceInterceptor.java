package eu.domibus.ext.delegate.services.party;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class PartyServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.party.PartyServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new PartyExtServiceException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
