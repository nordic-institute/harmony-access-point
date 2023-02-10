package eu.domibus.ext.delegate.services.truststore;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.CryptoExtException;
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
    public class TrustStoreServiceInterceptor extends ServiceInterceptor {

        private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustStoreServiceInterceptor.class);

        @Around(value = "execution(public * eu.domibus.ext.delegate.services.truststore.TrustStoreServiceDelegate.*(..))")
        @Override
        public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
            return super.intercept(joinPoint);
        }

        @Override
        public Exception convertCoreException(Exception e) {
            return new CryptoExtException(e);
        }

        @Override
        public DomibusLogger getLogger() {
            return LOG;
        }
    }
