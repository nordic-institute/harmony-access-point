package eu.domibus.user;

import eu.domibus.AbstractIT;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.user.ui.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class UserManagementServiceTestIT extends AbstractIT {

    private static final String LOGGED_USER = "test_user";

    @Autowired
    UserManagementServiceImpl userManagementService;

    @Autowired
    protected UserDao userDao;

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
    @Rollback
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void updateUsers_loggedIn_changeActive() {
        eu.domibus.api.user.User apiUser = initTestUser(LOGGED_USER);
        apiUser.setActive(false);
        try {
            userManagementService.updateUsers(Arrays.asList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot change the active status of the logged-in user"));
        }
    }

    @Test
    @Transactional
    @Rollback
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void updateUsers_loggedIn_changeRole() {
        eu.domibus.api.user.User apiUser = initTestUser(LOGGED_USER);
        apiUser.setAuthorities(Arrays.asList("ROLE_USER"));
        try {
            userManagementService.updateUsers(Arrays.asList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot change the role of the logged-in user"));
        }
    }

    @Test
    @Transactional
    public void updateUsers_notLoggedIn_atLeastOneAdmin() {
        final User userEntity = createUser("baciuco", "Password-0", "test@domibus.eu", AuthRole.ROLE_USER);
        final eu.domibus.api.user.User apiUser = convert(userEntity);
        apiUser.setActive(false);
        try {
            userManagementService.updateUsers(Arrays.asList(apiUser));
            Assert.fail();
        } catch (UserManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("There must always be at least one active Domain Admin for each Domain"));
        }
    }

    @Test
    @Transactional
    @Rollback
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void updateUsers_notLoggedIn_OK() {
        eu.domibus.api.user.User apiUser1 = initTestUser("admin1");
        eu.domibus.api.user.User apiUser = initTestUser("admin2");
        apiUser.setAuthorities(Arrays.asList("ROLE_USER"));
        apiUser.setActive(false);
        apiUser.setEmail("other.email@google.com");
        userManagementService.updateUsers(Arrays.asList(apiUser));
    }

    private eu.domibus.api.user.User initTestUser(String userName) {
        String password = "Password-0";
        String email = "test@mailinator.com";

//        UserRole userRole = userRoleDao.findByName(AuthRole.ROLE_ADMIN.name());

        final User user = createUser(userName, password, email, AuthRole.ROLE_ADMIN);
        return convert(user);
    }

    eu.domibus.api.user.User convert(User userEntity) {
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName(userEntity.getUserName());
        user.setPassword(userEntity.getPassword());

        final Collection<UserRole> roles = userEntity.getRoles();
        List<String> authorities = new ArrayList<>();
        roles.stream().forEach(userRole -> authorities.add(userRole.getName()));


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