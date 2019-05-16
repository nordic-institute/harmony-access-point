package eu.domibus.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.model.security.UserDetail;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@RunWith(JMockit.class)
public class AuthenticationServiceBaseTest {

    @Tested
    AuthenticationServiceBase authenticationServiceBase;

    @Injectable
    DomainService domainService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void changeDomain(final @Mocked Authentication authentication, final @Mocked UserDetail userDetail) {
        final String domainCode = "domain1";
        final Domain domain1 = new Domain("domain1", "domain 1");
        final Domain domain2 = new Domain("domain2", "domain 2");
        List<Domain> domains = new ArrayList<>();
        domains.add(domain1);
        domains.add(domain2);

        new Expectations() {{
            domainService.getDomains();
            result = domains;

            SecurityContextHolder.getContext().getAuthentication();
            result = authentication;

            authentication.getPrincipal();
            result = userDetail;

        }};

        //tested method
        authenticationServiceBase.changeDomain(domainCode);

        new Verifications() {{
            String actualDomain;
           userDetail.setDomain(actualDomain = withCapture());
        }};
    }
}