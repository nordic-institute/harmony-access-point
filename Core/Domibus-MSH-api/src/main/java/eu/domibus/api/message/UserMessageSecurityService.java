package eu.domibus.api.message;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.security.AuthenticationException;

/**
 * <p>Service used internally in the delegate module to check user permissions on messages</p>
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageSecurityService {

    /**
     * Checks it the current user has the permission to access the message
     *
     * @param messageId
     * @throws AuthenticationException in case the user doesn't have the permission
     */
    void checkMessageAuthorizationWithUnsecureLoginAllowed(String messageId) throws AuthenticationException;

    void checkMessageAuthorizationWithUnsecureLoginAllowed(String messageId, MSHRole mshRole) throws AuthenticationException;

    void checkMessageAuthorization(String messageId) throws AuthenticationException;

    void checkMessageAuthorizationWithUnsecureLoginAllowed(UserMessage userMessage) throws AuthenticationException;

    void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage);

    void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage, String authOriginalUser, String propertyName);

    void checkMessageAuthorizationWithUnsecureLoginAllowed(final Long messageEntityId);

    void checkMessageAuthorization(String messageId, MSHRole mshRole);
}
