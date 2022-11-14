package eu.domibus.web.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.api.multitenancy.DomainsAware;
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
public abstract class AuthenticationServiceBase implements AuthenticationService, DomainsAware {

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
    protected AuthUtils authUtils;

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

        authUtils.executeOnLoggedUser(userDetails -> userDetails.setDomain(domainCode));
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        DomibusUserDetails loggedUser = getLoggedUser();
        LOG.debug("Changing password for user [{}]", loggedUser.getUsername());
        getUserService().changePassword(loggedUser.getUsername(), currentPassword, newPassword);
        authUtils.executeOnLoggedUser(userDetails -> userDetails.setDefaultPasswordUsed(false));
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

    @Override
    public void onDomainAdded(Domain domain) {
        authUtils.executeOnLoggedUser(userDetails -> userDetails.addDomainCode(domain.getCode()));
    }

    @Override
    public void onDomainRemoved(Domain domain) {
        authUtils.executeOnLoggedUser(userDetails -> userDetails.removeDomainCode(domain.getCode()));
    }

    UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return allUserManagementService;
        } else {
            return userManagementService;
        }
    }
}
