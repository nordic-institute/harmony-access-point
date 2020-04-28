package eu.domibus.ext.delegate.services.pmode;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptorHelper;
import eu.domibus.ext.exceptions.PModeExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@Aspect
@Component
public class PModeServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeServiceInterceptor.class);

    @Autowired
    ServiceInterceptorHelper serviceInterceptorHelper;

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.pmode.PModeServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        PModeExtException pModeExtException = new PModeExtException(e);
        serviceInterceptorHelper.handlePModeValidationException(e, pModeExtException);
        return pModeExtException;
    }



    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
