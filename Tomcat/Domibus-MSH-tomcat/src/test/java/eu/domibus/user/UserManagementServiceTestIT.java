package eu.domibus.user;

import eu.domibus.AbstractIT;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.security.UserDetailServiceImpl;
import eu.domibus.core.user.ui.*;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.AuthenticationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class UserManagementServiceTestIT extends AbstractIT {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(UserManagementServiceTestIT.class);

    private static final String LOGGED_USER = "test_user";

    @Autowired
    UserManagementServiceImpl userManagementService;
    @Autowired
    UserDetailServiceImpl userDetailService;

    @Autowired
    protected UserDao userDao;
    @Autowired
    protected AuthenticationService authenticationService;

    @Autowired
    protected UserRoleDao userRoleDao;

    @PersistenceContext(unitName = JPAConstants.PERSISTENCE_UNIT_NAME)
    protected EntityManager entityManager;

    @Before
    public void before() {
        userDao.delete(userDao.listUsers());
    }

    @Test
    @Transactional
    @WithUserDetails(value = LOGGED_USER, userDetailsServiceBeanName = "testUserDetailService")
    public void updateUsers_loggedIn_changeActive() {
        LOG.info("LOGGED: [{}]", authenticationService.getLoggedUser().getUsername());

        eu.domibus.api.user.User admin = initTestUser("admin_test", AuthRole.ROLE_ADMIN);
        eu.domibus.api.user.User apiUser = initTestUser(LOGGED_USER, AuthRole.ROLE_ADMIN);
        apiUser.setActive(false);
        try {
            userManagementService.updateUsers(Collections.singletonList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            LOG.info(ex.getMessage(), ex);
            Assert.assertTrue(ex.getMessage().contains("Cannot change the active status of the logged-in user"));
        }
    }

    @Test
    @Transactional
    @WithUserDetails(value = LOGGED_USER, userDetailsServiceBeanName = "testUserDetailService")
    public void updateUsers_loggedIn_changeRole() {
        LOG.info("LOGGED: [{}]", authenticationService.getLoggedUser().getUsername());

        eu.domibus.api.user.User admin = initTestUser("admin_test", AuthRole.ROLE_ADMIN);
        eu.domibus.api.user.User apiUser = initTestUser(LOGGED_USER, AuthRole.ROLE_USER);
        apiUser.setAuthorities(Collections.singletonList("ROLE_ADMIN"));
        try {
            userManagementService.updateUsers(Collections.singletonList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            LOG.info(ex.getMessage(), ex);
            Assert.assertTrue(ex.getMessage().contains("Cannot change the role of the logged-in user"));
        }
    }

    @Test
    @Transactional
    @WithUserDetails(value = "customUsername", userDetailsServiceBeanName = "testUserDetailService")
    public void updateUsers_notLoggedIn_atLeastOneAdmin() {
        LOG.info("LOGGED: [{}]", authenticationService.getLoggedUser().getUsername());

        final User userEntity = createUser("baciuco", "Password-0123456", "test@domibus.eu", AuthRole.ROLE_USER);
        final eu.domibus.api.user.User apiUser = convert(userEntity);
        apiUser.setActive(false);
        try {
            userManagementService.updateUsers(Collections.singletonList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            LOG.info(ex.getMessage(), ex);
            Assert.assertTrue(ex.getMessage().contains("There must always be at least one active Domain Admin for each Domain"));
        }
    }

    @Test
    @Transactional
    @WithUserDetails(value = "customUsername", userDetailsServiceBeanName = "testUserDetailService")
    public void updateUsers_notLoggedIn_OK() {
        LOG.info("LOGGED: [{}]", authenticationService.getLoggedUser().getUsername());

        initTestUser("admin1", AuthRole.ROLE_ADMIN);
        eu.domibus.api.user.User apiUser = initTestUser("admin2", AuthRole.ROLE_ADMIN);
        apiUser.setAuthorities(Collections.singletonList("ROLE_USER"));
        apiUser.setActive(false);
        apiUser.setEmail("other.email@google.com");
        userManagementService.updateUsers(Collections.singletonList(apiUser));
    }

    private eu.domibus.api.user.User initTestUser(String userName, AuthRole roleAdmin) {
        String password = "Password-0123456";
        String email = "test@mailinator.com";

        final User user = createUser(userName, password, email, roleAdmin);
        return convert(user);
    }

    eu.domibus.api.user.User convert(User userEntity) {
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName(userEntity.getUserName());
        user.setPassword(userEntity.getPassword());

        final Collection<UserRole> roles = userEntity.getRoles();
        List<String> authorities = new ArrayList<>();
        roles.forEach(userRole -> authorities.add(userRole.getName()));


        user.setAuthorities(authorities);
        user.setEmail(userEntity.getEmail());
        user.setActive(userEntity.isActive());
        user.setStatus(UserState.UPDATED.name());

        return user;
    }

    private User createUser(String userName, String password, String email, AuthRole userRole) {
        User userEntity = new User();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);

        UserRole userRoleEntity = userRoleDao.findByName(userRole.name());
        userEntity.addRole(userRoleEntity);
        userEntity.setEmail(email);
        userEntity.setActive(true);
        userDao.create(userEntity);

        return userEntity;
    }
}
