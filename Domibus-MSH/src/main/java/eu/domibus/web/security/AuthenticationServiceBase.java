package eu.domibus.web.security;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public abstract class AuthenticationServiceBase {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceBase.class);

    @Autowired
    protected DomainService domainService;

    /**
     * Set the domain in the current security context
     *
     * @param domainCode the code of the new current domain
     */
    public void changeDomain(String domainCode) {

        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainTaskException("Could not set current domain: domain is empty");
        }
        if (!domainService.getDomains().stream().anyMatch(d -> domainCode.equalsIgnoreCase(d.getCode()))) {
            throw new DomainTaskException("Could not set current domain: unknown domain (" + domainCode + ")");
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetail securityUser = (UserDetail) authentication.getPrincipal();
        securityUser.setDomain(domainCode);
        refreshSecurityContext(authentication);
    }

    /**
     * It will return the Principal from {@link SecurityContextHolder}
     * if different from {@link AnonymousAuthenticationToken}
     *
     * @return logged in user info
     */
    public UserDetail getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && (authentication.getPrincipal() instanceof UserDetail)) {
            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            LOG.debug("Principal found on SecurityContextHolder: {}", userDetail);
            return userDetail;
        }
        LOG.warn("Authentication is missing from the security context or it is of wrong type. Could not return the logged user.");
        return null;
    }

    /**
     * Refreshes the SecurityContext by first clearing any existing security context (this is only
     * needed when the user details have changed and need to be updated in the user session).
     *
     * @param authentication the immutable authentication object
     */
    // EDELIVERY-7611 - do this inside setDomain method of UserDetails
    protected void refreshSecurityContext(Authentication authentication) {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
