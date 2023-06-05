package eu.domibus.web.security;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.core.security.UserSessionsServiceImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(JMockit.class)
public class UserSessionsServiceImplTest {
    @Tested
    UserSessionsServiceImpl userSessionsService;

    @Injectable
    SessionRegistry sessionRegistry;

    @Injectable
    SignalService signalService;

    @Test
    public void invalidateUserSessions(@Injectable User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        String userName = "userName";
        final DomibusUserDetailsImpl domibusUserDetails = new DomibusUserDetailsImpl(userName, "password", authorities);

        SessionInformation sinfo = new SessionInformation(domibusUserDetails, "id", new Date());

        new Expectations(sinfo) {{
            user.getUserName();
            result = userName;

            sessionRegistry.getAllPrincipals();
            result = Arrays.asList(domibusUserDetails);

            sessionRegistry.getAllSessions(domibusUserDetails, false);
            result = sinfo;
        }};

        userSessionsService.invalidateSessions(user);

        new Verifications() {{
            sinfo.expireNow();
            times = 1;
        }};
    }

    @Test
    public void invalidateDomainSessions(@Injectable User user, @Injectable DomibusUserDetailsImpl domibusUserDetails) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        String userName = "userName";

        String domainCode = "default";
        Domain domain = new Domain(domainCode, "Default");

        SessionInformation sinfo = new SessionInformation(domibusUserDetails, "id", new Date());

        new Expectations(sinfo) {{
            domibusUserDetails.getDomain();
            result = domainCode;

            domibusUserDetails.getUsername();
            result = userName;

            sessionRegistry.getAllPrincipals();
            result = Arrays.asList(domibusUserDetails);

            sessionRegistry.getAllSessions(domibusUserDetails, false);
            result = sinfo;
        }};

        userSessionsService.invalidateSessions(domain);

        new Verifications() {{
            userSessionsService.invalidateSessions(userName);
        }};
    }

}
