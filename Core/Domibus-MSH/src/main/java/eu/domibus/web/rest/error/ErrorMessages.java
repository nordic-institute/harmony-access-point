package eu.domibus.web.rest.error;

import org.springframework.security.core.SpringSecurityMessageSource;

public interface ErrorMessages {
    String DEFAULT_MESSAGE_FOR_GENERIC_ERRORS = "A server error occurred";
    String DEFAULT_MESSAGE_FOR_AUTHENTICATION_ERRORS = SpringSecurityMessageSource.getAccessor()
            .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials");
}
