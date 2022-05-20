package eu.domibus.web.security;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.core.user.UserService;
import eu.domibus.core.user.multitenancy.AllUsersManagementServiceImpl;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Consumer;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public abstract class AuthenticationServiceBase implements AuthenticationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticationServiceBase.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    @Qualifier(AllUsersManagementServiceImpl.BEAN_NAME)
    private UserService allUserManagementService;

    @Autowired
    @Qualifier(UserManagementServiceImpl.BEAN_NAME)
    private UserService userManagementService;

    @Autowired
    private AuthUtils authUtils;

    /**
     * Set the domain in the current security context
     *
     * @param domainCode the code of the new current domain
     */
    @Override
    public void changeDomain(String domainCode) {
        if (StringUtils.isEmpty(domainCode)) {
            throw new DomainTaskException("Could not set current domain: domain is empty");
        }
        if (!domainService.getDomains().stream().anyMatch(d -> domainCode.equalsIgnoreCase(d.getCode()))) {
            throw new DomainTaskException("Could not set current domain: unknown domain (" + domainCode + ")");
        }

        executeOnLoggedUser(userDetails -> userDetails.setDomain(domainCode));
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        DomibusUserDetails loggedUser = getLoggedUser();
        LOG.debug("Changing password for user [{}]", loggedUser.getUsername());
        getUserService().changePassword(loggedUser.getUsername(), currentPassword, newPassword);
        executeOnLoggedUser(userDetails -> userDetails.setDefaultPasswordUsed(false));
    }

    @Override
    public void addDomainCode(String domainCode) {
        executeOnLoggedUser(userDetails -> userDetails.addDomainCode(domainCode));
    }

    @Override
    public void removeDomainCode(String domainCode) {
        executeOnLoggedUser(userDetails -> userDetails.removeDomainCode(domainCode));
    }

    /**
     * It will return the Principal from {@link SecurityContextHolder}
     * if different from {@link AnonymousAuthenticationToken}
     *
     * @return logged in user info
     */
    @Override
    public DomibusUserDetails getLoggedUser() {
        return authUtils.getUserDetails();
    }

    protected void executeOnLoggedUser(Consumer<DomibusUserDetails> consumer) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        executeOnLoggedUser(consumer, authentication);
    }

    protected void executeOnLoggedUser(Consumer<DomibusUserDetails> consumer, Authentication authentication) {
        if (authentication == null) {
            LOG.debug("Authentication is missing from the security context");
            return;
        }
        DomibusUserDetails securityUser = (DomibusUserDetails) authentication.getPrincipal();
        if (securityUser == null) {
            LOG.debug("User details are missing from the authentication");
            return;
        }

        consumer.accept(securityUser);

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return allUserManagementService;
        } else {
            return userManagementService;
        }
    }
}
