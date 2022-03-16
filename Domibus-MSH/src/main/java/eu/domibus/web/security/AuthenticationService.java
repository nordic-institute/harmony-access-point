package eu.domibus.web.security;

/**
 * @author Catalin Enache
 * @since 4.1
 */
public interface AuthenticationService {

    DomibusUserDetailsImpl authenticate(String username, String password, String domain);

    void changeDomain(String domainCode);

    DomibusUserDetailsImpl getLoggedUser();
}
