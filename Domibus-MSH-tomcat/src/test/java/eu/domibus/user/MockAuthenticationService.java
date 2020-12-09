package eu.domibus.user;

import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.UserDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MockAuthenticationService implements AuthenticationService {
    @Override
    public UserDetail authenticate(String username, String password, String domain) {
        return null;
    }

    @Override
    public void changeDomain(String domainCode) {
    }

    @Override
    public UserDetail getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userName = (String) authentication.getPrincipal();
            UserDetail userDetail = new UserDetail(userName, StringUtils.EMPTY, authentication.getAuthorities());
            return userDetail;
        }
        return null;
    }
}
