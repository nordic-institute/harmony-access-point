package eu.domibus.security;

import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.security.AuthUtilsImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class tests the PreAuthorize annotation.
 * In order to trigger the @PreAuthorize, we use Spring to create the bean with
 * '@EnableGlobalMethodSecurity(prePostEnabled = true)'
 *
 * '@WithMockUser' is then use to set up the security context with a mock user easily
 * @author François Gautier
 * @since 4.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthUtilsImplIT {

    @Autowired
    private AuthUtils authUtils;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {
        @Bean
        public AuthUtils authUtils() {
            return new AuthUtilsImpl(null, null);
        }
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void hasAdminRole_noUser() {
        authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "ecas", roles={"ECAS"})
    public void hasAdminRole_user() {
        authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
    }

    @Test
    @WithMockUser(username = "admin", roles={"AP_ADMIN"})
    public void hasAdminRole_apAdmin() {
        authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
    }

    @Test
    @WithMockUser(username = "admin", roles={"ADMIN"})
    public void hasAdminRole_admin() {
        authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
    }
    
    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void hasUserRole_noUser() {
        authUtils.checkHasAdminRoleOrUserRoleWithOriginalUser();
    }

}
