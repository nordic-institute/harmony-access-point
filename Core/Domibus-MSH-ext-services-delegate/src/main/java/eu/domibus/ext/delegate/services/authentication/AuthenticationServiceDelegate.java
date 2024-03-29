package eu.domibus.ext.delegate.services.authentication;

import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.AuthRole;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class AuthenticationServiceDelegate implements AuthenticationExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceDelegate.class);

    @Autowired
    protected eu.domibus.api.security.AuthenticationService authenticationService;

    @Autowired
    protected AuthUtils authUtils;

    @Override
    public void authenticate(HttpServletRequest httpRequest) throws AuthenticationExtException {
        try {
            authenticationService.authenticate(httpRequest);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationExtException(e);
        }
    }

    @Override
    public void enforceAuthentication(HttpServletRequest httpRequest) throws AuthenticationExtException {
        try {
            authenticationService.enforceAuthentication(httpRequest);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationExtException(e);
        }
    }

    @Override
    public void basicAuthenticate(String user, String password) throws AuthenticationExtException {
        try {
            authenticationService.basicAuthenticate(user, password);
        } catch (Exception e) {
            //already logged by the authentication service
            throw new AuthenticationExtException(e);
        }
    }

    @Override
    public boolean isUnsecureLoginAllowed() {
        return authUtils.isUnsecureLoginAllowed();
    }

    @Override
    public void hasUserOrAdminRole() {
        authUtils.hasUserOrAdminRole();
    }

    @Override
    public String getAuthenticatedUser() {
        return authUtils.getAuthenticatedUser();
    }

    @Override
    public String getOriginalUser() {
        return authUtils.getOriginalUserWithUnsecureLoginAllowed();
    }

    @Override
    public void runWithSecurityContext(Runnable runnable, String user, String password, AuthRole authRole) {
        authUtils.runWithSecurityContext(runnable::run, user, password, eu.domibus.api.security.AuthRole.valueOf(authRole.name()));
    }
}
