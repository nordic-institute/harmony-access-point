package eu.domibus.core.security;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.AuthenticationException;
import eu.domibus.api.security.functions.AuthenticatedFunction;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED;

@Component(value = "authUtils")
public class AuthUtilsImpl implements AuthUtils {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthUtilsImpl.class);
    private static final String DOMIBUS_USER = "domibus";
    private static final String DOMIBUS_PASSWORD = "domibus"; //NOSONAR

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final DomibusConfigurationService domibusConfigurationService;

    public AuthUtilsImpl(
            DomibusPropertyProvider domibusPropertyProvider,
            DomibusConfigurationService domibusConfigurationService) {
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    /**
     * Returns the original user passed via the security context OR
     * null value when the user has the role ROLE_ADMIN or unsecured authorization is allowed
     */
    @Override
    public String getOriginalUserFromSecurityContext() throws AuthenticationException {
        /* unsecured login allowed */
        if (isUnsecureLoginAllowed()) {
            LOG.debug("Unsecured login is allowed");
            return null;
        }

        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.error("Authentication is missing from the security context. Unsecured login is not allowed");
            throw new AuthenticationException("Authentication is missing from the security context. Unsecured login is not allowed");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String originalUser = null;
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (!authorities.contains(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()))
                && !authorities.contains(new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name()))) {
            originalUser = (String) authentication.getPrincipal();
            LOG.debug("User [{}] has user role and finalRecipient [{}]", getAuthenticatedUser(), originalUser);
        } else {
            LOG.debug("User [{}] has admin role", getAuthenticatedUser());
        }

        return originalUser;
    }

    @Override
    public String getAuthenticatedUser() {
        if (SecurityContextHolder.getContext() == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.debug("Authentication is missing from the security context");
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @Override
    public boolean isUnsecureLoginAllowed() {
        if (domibusConfigurationService.isMultiTenantAware()) {
            LOG.trace("Unsecured login not allowed: Domibus is running in multi-tenancy mode");
            return false;
        }
        /* unsecured login allowed */
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED);
    }

    @Override
    public boolean isSuperAdmin() {
        return checkAdminRights(AuthRole.ROLE_AP_ADMIN);
    }

    @Override
    public boolean isAdmin() {
        return checkAdminRights(AuthRole.ROLE_ADMIN);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    public void hasUserOrAdminRole() {
        if (isAdmin() || isSuperAdmin()) {
            return;
        }
        // USER_ROLE - verify the ORIGINAL_USER is configured
        String originalUser = getOriginalUserFromSecurityContext();
        if (StringUtils.isEmpty(originalUser)) {
            throw new AuthenticationException("User " + getAuthenticatedUser() + " has USER_ROLE but is missing the ORIGINAL_USER in the db");
        }
        LOG.debug("Logged with USER_ROLE, ORIGINAL_USER is {}", originalUser);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_AP_ADMIN')")
    public void hasAdminRole() {
        // PreAuthorize
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public void hasUserRole() {
        // PreAuthorize
    }

    @Override
    public void setAuthenticationToSecurityContext(String user, String password) {
        setAuthenticationToSecurityContext(user, password, AuthRole.ROLE_ADMIN);
    }

    @Override
    public void setAuthenticationToSecurityContext(String user, String password, AuthRole authRole) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        user,
                        password,
                        Collections.singleton(new SimpleGrantedAuthority(authRole.name()))));
    }

    protected boolean checkAdminRights(AuthRole authRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            LOG.debug("No authentication found or the authenticated user has no authorities");
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.contains(new SimpleGrantedAuthority(authRole.name()))) {
            LOG.debug("User=[{}] has Admin or Super Admin rights ([{}])", authentication.getName(), authRole.name());
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdminMultiAware() {
        if (domibusConfigurationService.isMultiTenantAware()) {
            return isSuperAdmin();
        }
        return isAdmin();
    }

    @Override
    public void runWithSecurityContext(AuthenticatedProcedure runnable, String user, String password) {
        runWithSecurityContext(runnable, user, password, AuthRole.ROLE_ADMIN);
    }

    @Override
    public void runWithSecurityContext(AuthenticatedProcedure method, String user, String password, AuthRole authRole) {
        runWithSecurityContext(method, user, password, authRole, false);
    }

    @Override
    public void runWithSecurityContext(AuthenticatedProcedure method, String user, String password, AuthRole authRole, boolean forceSecurityContext) {
        if (isUnsecureLoginAllowed() && !forceSecurityContext) {
            LOG.debug("Run method without spring security context.");
            method.invoke();
            return;
        }
        try {
            setAuthenticationToSecurityContext(user, password, authRole);
            method.invoke();
        } finally {
            clearSecurityContext();
        }
    }

    @Override
    public <R> R runFunctionWithSecurityContext(AuthenticatedFunction function, String user, String password, AuthRole authRole) {
        return runFunctionWithSecurityContext(function, user, password, authRole, false);
    }

    @Override
    public <R> R runFunctionWithSecurityContext(AuthenticatedFunction function, String user, String password, AuthRole authRole, boolean forceSecurityContext) {
        if (isUnsecureLoginAllowed() && !forceSecurityContext) {
            LOG.debug("Unsecure login is allowed: not Spring security is set before executing the method.");
            return (R) function.invoke();
        }

        try {
            setAuthenticationToSecurityContext(user, password, authRole);
            return (R) function.invoke();
        } finally {
            clearSecurityContext();
        }
    }

    @Override
    public void runWithDomibusSecurityContext(AuthenticatedProcedure method, AuthRole authRole) {
        runWithDomibusSecurityContext(method, authRole, false);
    }

    @Override
    public void runWithDomibusSecurityContext(AuthenticatedProcedure method, AuthRole authRole, boolean forceSecurityContext) {
        runWithSecurityContext(method, DOMIBUS_USER, DOMIBUS_PASSWORD, authRole, forceSecurityContext);
    }
    
    @Override
    public <R> R runFunctionWithDomibusSecurityContext(AuthenticatedFunction function, AuthRole authRole, boolean forceSecurityContext) {
        return runFunctionWithSecurityContext(function, DOMIBUS_USER, DOMIBUS_PASSWORD, authRole, forceSecurityContext);
    }

    @Override
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
