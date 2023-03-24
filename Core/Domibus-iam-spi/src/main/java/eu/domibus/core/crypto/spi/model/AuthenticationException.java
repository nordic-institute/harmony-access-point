package eu.domibus.core.crypto.spi.model;

import org.apache.wss4j.common.ext.WSSecurityException;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Exception thrown by the new Authentication module.
 */
public class AuthenticationException extends RuntimeException {

    private AuthenticationError authenticationError;

    public AuthenticationException(WSSecurityException e) {
        super(e);
    }

    public AuthenticationException(final AuthenticationError authenticationError, String message) {
        super(message);
        this.authenticationError = authenticationError;
    }

    public AuthenticationError getAuthenticationError() {
        return authenticationError;
    }
}
