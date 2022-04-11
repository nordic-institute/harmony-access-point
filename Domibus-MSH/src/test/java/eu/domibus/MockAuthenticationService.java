package eu.domibus;

import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.DomibusUserDetailsImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockAuthenticationService implements AuthenticationService {
    @Override
    public DomibusUserDetailsImpl authenticate(String username, String password, String domain) {
        return null;
    }

    @Override
    public void changeDomain(String domainCode) {
    }

    @Override
    public void changePassword(String currentPassword, String newPassword) {
    }

    @Override
    public void addDomainCode(String domainCode) {
    }

    @Override
    public void removeDomainCode(String domainCode) {
    }

    @Override
    public DomibusUserDetailsImpl getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = (String) authentication.getPrincipal();
            DomibusUserDetailsImpl domibusUserDetails = new DomibusUserDetailsImpl(userName, StringUtils.EMPTY, authentication.getAuthorities());
            return domibusUserDetails;
        }
        return null;
    }
}
