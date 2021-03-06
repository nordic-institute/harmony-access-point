package eu.domibus.core.security;

import eu.domibus.api.util.AOPUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.exception.CoreServiceExceptionInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Interceptor in charge of converting authentication spi exceptions into crypto exceptions.
 */
@Aspect
@Component
public class AuthorizationServiceInterceptor extends CoreServiceExceptionInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationServiceInterceptor.class);

    public AuthorizationServiceInterceptor(AOPUtil aopUtil) {
        super(aopUtil);
    }

    @Around(value = "execution(public * eu.domibus.core.security.AuthorizationService.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (LOG.isTraceEnabled()) {
            for (Object arg : args) {
                LOG.trace("Method argument:[{}]", arg);
            }
        }
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if (e instanceof AuthorizationException) {
            AuthorizationException a = (AuthorizationException) e;
            LOG.trace("Converting Authorization exception:[{}] into EBMSException", a.getClass(), e);
            if (a.getAuthorizationError() != null) {
                switch (a.getAuthorizationError()) {
                    case INVALID_FORMAT:
                        LOG.error("Invalid incoming message format during authorization:[{}]", a.getMessage());
                        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, a.getMessage(), a.getMessageId(), null);
                    case AUTHORIZATION_REJECTED:
                        LOG.error("Authorization for incoming message was not granted:[{}]", a.getMessage());
                        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "A0001:Authorization to access the targeted application refused to sender.", a.getMessageId(), null);
                    case AUTHORIZATION_MODULE_CONFIGURATION_ISSUE:
                    case AUTHORIZATION_SYSTEM_DOWN:
                        LOG.error("Technical issue with the authorization module:[{}]", a.getMessage());
                        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "A0003:Technical issue.", a.getMessageId(), null);
                    case AUTHORIZATION_CONNECTION_REJECTED:
                        LOG.error("Connection credential to Authorization was rejected:[{}]", a.getMessage());
                        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "A0002:Technical issue.", a.getMessageId(), null);
                    default:
                        LOG.warn("Unknown authorization error:[{}]", a.getAuthorizationError());
                }
            }
        }
        LOG.error("Authorization module Unforeseen error:[{}]", e.getMessage(), e);
        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "A0003:Technical issue.", null, null);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}