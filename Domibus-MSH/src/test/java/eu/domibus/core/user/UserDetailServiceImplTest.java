package eu.domibus.core.user;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.security.UserDetailServiceImpl;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.web.security.UserDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDetailServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private DomibusPropertyProvider domibusPropertyProvider;
    @Mock
    private UserService userService;
    @Spy
    private BCryptPasswordEncoder bcryptEncoder;
    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    public void loadUserByUsernameSuccessfully() throws Exception {
        User user = new User() {{
            setUserName("admin");
            setPassword("whateverdifferentthandefaultpasswordhash");
        }};

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getProperty(eq(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD))).thenReturn("true");
        when(userService.getDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("whateverdifferentthandefaultpasswordhash", admin.getPassword());
        assertEquals("admin", admin.getUsername());
        assertEquals(false, admin.isDefaultPasswordUsed());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPassword() throws Exception {
        User user = new User() {{
            setUserName("user");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
        }};

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getProperty(eq(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD))).thenReturn("true");
        when(userService.getDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
    }

    @Test
    public void loadUserByUsernameSuccessfullyUsingDefaultPasswordWarningDisabled() throws Exception {
        User user = new User() {{
            setUserName("user");
            setPassword("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36");
        }};

        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(user);
        when(domibusPropertyProvider.getProperty(eq(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD))).thenReturn("false");
        when(userService.getDaysTillExpiration(eq("admin"))).thenReturn(90);

        UserDetail admin = (UserDetail) userDetailService.loadUserByUsername("admin");

        assertEquals("$2a$10$5uKS72xK2ArGDgb2CwjYnOzQcOmB7CPxK6fz2MGcDBM9vJ4rUql36", admin.getPassword());
        assertEquals("user", admin.getUsername());
        assertEquals(false, admin.isDefaultPasswordUsed());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testUserNotFound() throws Exception {
        when(userDao.loadActiveUserByUsername(eq("admin"))).thenReturn(null);
        userDetailService.loadUserByUsername("adminNotInThere");
    }

}