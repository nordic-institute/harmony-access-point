package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.util.AOPUtil;
import eu.domibus.core.crypto.spi.CryptoSpiException;
import eu.domibus.core.crypto.spi.DomibusCertificateSpiException;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.exception.CoreServiceExceptionInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.security.KeyStoreException;

/**
 * Interceptor in charge of converting authentication spi exceptions into crypto exceptions.
 */
@Aspect
@Component
public class DomainCryptoServiceInterceptor extends CoreServiceExceptionInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceInterceptor.class);

    public DomainCryptoServiceInterceptor(AOPUtil aopUtil) {
        super(aopUtil);
    }

    @Around(value = "execution(public * eu.domibus.core.crypto.DomainCryptoServiceImpl.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if (e instanceof CryptoSpiException) {
            LOG.trace("Converting CryptoSpiException:[{}] into CryptoException", e);
            return new CryptoException(e.getMessage(), e);
        } else if (e instanceof DomibusCertificateSpiException) {
            LOG.trace("Converting DomibusCertificateSpiException:[{}] into DomibusCertificateException", e);
            return new DomibusCertificateException(e.getMessage(), e);
        } else if (e instanceof WSSecurityException ||
                e instanceof KeyStoreException ||
                e instanceof CryptoException ||
                e instanceof ConfigurationException ||
                e instanceof DomibusCertificateException) {
            LOG.trace("No need to convert exception:[{}]", e.getClass());
            return e;
        } else {
            LOG.trace("Unknown exception:[{}] converted to CryptoException", e);
            return new CryptoException(e);
        }
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}