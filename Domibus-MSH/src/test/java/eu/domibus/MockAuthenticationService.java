package eu.domibus;

import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.DomibusUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockAuthenticationService implements AuthenticationService {
    @Override
    public DomibusUserDetails authenticate(String username, String password, String domain) {
        return null;
    }

    @Override
    public void changeDomain(String domainCode) {
    }

    @Override
    public DomibusUserDetails getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = (String) authentication.getPrincipal();
            DomibusUserDetails domibusUserDetails = new DomibusUserDetails(userName, StringUtils.EMPTY, authentication.getAuthorities());
            return domibusUserDetails;
        }
        return null;
    }
}
