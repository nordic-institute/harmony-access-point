package eu.domibus.ext.delegate.services.party;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptorHelper;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public class PartyServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceInterceptor.class);

    @Autowired
    ServiceInterceptorHelper serviceInterceptorHelper;

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.party.PartyServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        PartyExtServiceException partyExtServiceException = new PartyExtServiceException(e);
        serviceInterceptorHelper.handlePModeValidationException(e, partyExtServiceException);
        return new PartyExtServiceException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
