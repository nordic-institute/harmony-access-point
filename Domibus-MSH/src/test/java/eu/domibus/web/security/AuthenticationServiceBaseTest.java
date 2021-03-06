package eu.domibus.web.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class AuthenticationServiceBaseTest {

    @Tested
    AuthenticationServiceBase authenticationServiceBase;

    @Injectable
    DomainService domainService;

    private List<Domain> domains = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        final Domain domain1 = new Domain("domain1", "domain 1");
        final Domain domain2 = new Domain("domain2", "domain 2");

        domains.add(domain1);
        domains.add(domain2);
    }

    @Test
    public void test_changeDomain_DomainExists(final @Mocked SecurityContext securityContext, final @Mocked Authentication authentication, final @Mocked UserDetail userDetail) {
        final String domainCode = "domain1";

        new Expectations() {{
            domainService.getDomains();
            result = domains;

            new MockUp<SecurityContextHolder>() {
                @Mock
                SecurityContext getContext() {
                    return securityContext;
                }
            };

            securityContext.getAuthentication();
            result = authentication;

            authentication.getPrincipal();
            result = userDetail;

        }};

        //tested method
        authenticationServiceBase.changeDomain(domainCode);

        new Verifications() {{
            String actualDomain;
            userDetail.setDomain(actualDomain = withCapture());
            Assert.assertEquals(domainCode, actualDomain);

            SecurityContextHolder.getContext().setAuthentication((Authentication) any);
        }};
    }

    @Test
    public void test_changeDomain_DomainDoesntExists() {
        final String domainCode = "domain3";


        new Expectations() {{
            domainService.getDomains();
            result = domains;

        }};

        try {
            //tested method
            authenticationServiceBase.changeDomain(domainCode);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(DomainTaskException.class, e.getClass());
        }

        new FullVerifications() {{
        }};
    }

    @Test
    public void test_changeDomain_DomainEmpty() {
        final String domainCode = StringUtils.EMPTY;

        new Expectations() {{
        }};

        try {
            //tested method
            authenticationServiceBase.changeDomain(domainCode);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(DomainTaskException.class, e.getClass());
        }

        new FullVerifications() {{
        }};
    }

    @Test
    public void testGetLoggedUser_PrincipalExists(final @Mocked SecurityContext securityContext, final @Mocked Authentication authentication) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        final UserDetail userDetail = new UserDetail("username", "password", authorities);

        new Expectations() {{
            new MockUp<SecurityContextHolder>() {
                @Mock
                SecurityContext getContext() {
                    return securityContext;
                }
            };

            securityContext.getAuthentication();
            result = authentication;

            authentication.getPrincipal();
            result = userDetail;

        }};

        //tested method
        UserDetail userDetai1Actual = authenticationServiceBase.getLoggedUser();
        Assert.assertEquals(userDetail, userDetai1Actual);
    }

    @Test
    public void testGetLoggedUser_PrincipalDoesntExists(final @Mocked SecurityContext securityContext) {

        new Expectations(authenticationServiceBase) {{
            new MockUp<SecurityContextHolder>() {
                @Mock
                SecurityContext getContext() {
                    return securityContext;
                }
            };

            securityContext.getAuthentication();
            result = null;

        }};

        //tested method
        UserDetail userDetai1Actual = authenticationServiceBase.getLoggedUser();
        Assert.assertNull(userDetai1Actual);
    }
}