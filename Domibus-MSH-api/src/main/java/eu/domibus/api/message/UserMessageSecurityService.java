package eu.domibus.api.message;

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
    void checkMessageAuthorization(String messageId) throws AuthenticationException;

    void checkMessageAuthorizationWithUnsecureLoginAllowed(UserMessage userMessage) throws AuthenticationException;

    /**
     *
     * @return true if admin or super admin
     */
    boolean isAdminMultiAware();

    /**
     * Returns the original user passed via the security context OR
     * null when the user has the role ROLE_ADMIN / ROLE_AP_ADMIN or unsecured authorizations is allowed
     *
     * @return original user passed via the security context or null
     * @throws AuthenticationException in case the user doesn't have the permission
     */
    String getOriginalUserFromSecurityContext() throws AuthenticationException;

    void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage);

    void validateUserAccessWithUnsecureLoginAllowed(UserMessage userMessage, String authOriginalUser, String propertyName);

    void checkMessageAuthorizationWithUnsecureLoginAllowed(final Long messageEntityId);

}
