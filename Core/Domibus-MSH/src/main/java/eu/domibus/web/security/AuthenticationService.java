package eu.domibus.web.security;

import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.security.DomibusUserDetails;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface AuthenticationService extends DomainsAware {

    DomibusUserDetails authenticate(String username, String password, String domain);

    void changeDomain(String domainCode);

    void changePassword(String currentPassword, String newPassword);

    DomibusUserDetails getLoggedUser();
}
