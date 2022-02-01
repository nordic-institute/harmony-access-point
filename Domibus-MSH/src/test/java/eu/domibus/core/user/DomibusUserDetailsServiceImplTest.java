package eu.domibus.core.user;

import com.google.common.collect.Sets;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.core.security.UserDetailServiceImpl;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.web.security.DomibusUserDetails;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(JMockit.class)
public class DomibusUserDetailsServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserService userManagementService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Tested
    private UserDetailServiceImpl userDetailService;

    @Test
    public void loadUserByUsernameSuccessfully()  {
        User user = new User() {{
            setUserName("admin");
            setPassword("whateverdifferentthandefaultpasswordhash");
        }};

        new Expectations() {{
            userDao.loadActiveUserByUsername("admin");
            result = user;

            domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD);
            result = "true";

            userManagementService.getDaysTillExpiration("admin");
            result = 90;
        }};


        DomibusUserDetails admin = (DomibusUserDetails) userDetailService.loadUserByUsername("admin");

        assertEquals("whateverdifferentthandefaultpasswordhash", admin.getPassword());
        assertEquals("admin", admin.getUsername());
        assertFalse(admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPassword() {
        User user = new User() {{
            setUserName("user");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
        }};

        new Expectations() {{
            userDao.loadActiveUserByUsername("admin");
            result = user;

            domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD);
            result = "true";

            userManagementService.getDaysTillExpiration("admin");
            result = 90;
        }};

        DomibusUserDetails admin = (DomibusUserDetails) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPasswordWarningDisabled() {
        User user = new User() {{
            setUserName("user");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
        }};

        new Expectations() {{
            userDao.loadActiveUserByUsername("admin");
            result = user;

            domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD);
            result = "false";

            userManagementService.getDaysTillExpiration("admin");
            result = 90;
        }};

        DomibusUserDetails admin = (DomibusUserDetails) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
        assertFalse(admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsername_AllDomainsAvailableToSuperAdminUsers() {
        User superAdmin = new User() {{
            setUserName("super");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
            addRole(new UserRole(AuthRole.ROLE_AP_ADMIN.name()));
        }};

        final List<Domain> domains = Arrays.asList(
                new Domain("red", "Ro"),
                new Domain("yellow", "Ma"),
                new Domain("blue", "Nia"));

        new Expectations() {{
            userDao.loadActiveUserByUsername("super");
            result = superAdmin;

            domainService.getDomains();
            this.result = domains;
        }};

        DomibusUserDetails admin = (DomibusUserDetails) userDetailService.loadUserByUsername("super");

        assertEquals(Sets.newHashSet("red", "yellow", "blue"), admin.getAvailableDomainCodes());
    }

    @Test
    public void loadUserByUsername_DomainsAvailableToNormalUsers() {
        User superAdmin = new User() {{
            setUserName("admin");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
            addRole(new UserRole(AuthRole.ROLE_USER.name()));
        }};

        new Expectations() {{
            userDao.loadActiveUserByUsername("admin");
            result = superAdmin;

            userDomainService.getDomainForUser("admin");
            this.result = "red";
        }};

        DomibusUserDetails admin = (DomibusUserDetails) userDetailService.loadUserByUsername("admin");

        assertEquals(Collections.singleton("red"), admin.getAvailableDomainCodes());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() {
        new Expectations() {{
            userDao.loadActiveUserByUsername("adminNotInThere");
            result = null;
        }};

        userDetailService.loadUserByUsername("adminNotInThere");
    }

}