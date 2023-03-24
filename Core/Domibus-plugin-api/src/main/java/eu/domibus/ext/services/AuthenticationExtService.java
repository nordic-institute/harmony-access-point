package eu.domibus.ext.services;

import eu.domibus.common.AuthRole;
import eu.domibus.ext.exceptions.AuthenticationExtException;

import javax.servlet.http.HttpServletRequest;

/**
 * Responsible for operations related to the plugin authentication
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface AuthenticationExtService {

    /**
     * Authenticates the caller using one of the following authentication methods in this specific order: basic authentication, https or blue coat
     * Note: it may bypass authentication if specified in domibus configuration.
     *
     * @param httpRequest the HttpServletRequest request
     * @throws AuthenticationExtException in case an error occurs while authenticating the caller
     */
    void authenticate(HttpServletRequest httpRequest) throws AuthenticationExtException;

    /**
     * Authenticates the caller using one of the following authentication methods in this specific order: basic authentication, https or blue coat
     *
     * @param httpRequest the HttpServletRequest request
     * @throws AuthenticationExtException in case an error occurs while authenticating the caller
     */
    void enforceAuthentication(HttpServletRequest httpRequest) throws AuthenticationExtException;

    /**
     * Authenticates the caller using basic authentication method
     *
     * @param username The username used for authentication
     * @param password The user password
     * @throws AuthenticationExtException
     */
    void basicAuthenticate(String username, String password) throws AuthenticationExtException;

    /**
     * Checks whether unsecure login is allowed
     *
     * @return true in case unsecure loggin is allowed
     */
    boolean isUnsecureLoginAllowed();

    /**
     * Verifies if the user is authenticated
     * @throws org.springframework.security.access.AccessDeniedException if the user is not authenticated
     */
    void hasUserOrAdminRole();

    /**
     * Get authenticated user
     *
     * @return the authenticated user
     */
    String getAuthenticatedUser();

    /**
     * Get original user of the authenticated user
     *
     * @return the original user
     */
    String getOriginalUser();

    /**
     * Method execute function given in function parameter.
     * If method isUnsecureLoginAllowed returns false, then
     * the spring security context with user Authentication and role 'authRole; is set before invoking the function.
     * After the method is executed the security context is removed.
     *
     * @param runnable - method to wrap
     * @param user     - Authentication: username
     * @param password - Authentication: password
     * @param authRole - Authentication: role
     */
    void runWithSecurityContext(Runnable runnable, String user, String password, AuthRole authRole);

}
