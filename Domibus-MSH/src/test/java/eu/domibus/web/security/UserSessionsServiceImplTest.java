package eu.domibus.web.security;

import eu.domibus.api.cluster.SignalService;
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

import java.util.*;

@RunWith(JMockit.class)
public class UserSessionsServiceImplTest {
    @Tested
    UserSessionsServiceImpl userSessionsService;

    @Injectable
    SessionRegistry sessionRegistry;

    @Injectable
    SignalService signalService;

    @Test
    public void invalidateSessions() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        final UserDetail userDetail = new UserDetail("userName", "password", authorities);

        User user = new User("userName", "email", false, new ArrayList<String>(), UserState.UPDATED, null, false);

        SessionInformation sinfo = new SessionInformation(userDetail, "id", new Date());

        new Expectations(sinfo) {{
            sessionRegistry.getAllPrincipals();
            result = Arrays.asList(userDetail);

            sessionRegistry.getAllSessions(userDetail, false);
            result = sinfo;
        }};

        userSessionsService.invalidateSessions(user);

        new Verifications() {{
            sinfo.expireNow();
            times = 1;
        }};
    }
}