package eu.domibus.user;

import eu.domibus.AbstractIT;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserManagementServiceImpl;
import eu.domibus.core.user.ui.UserRole;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;

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

    @PersistenceContext(unitName = "domibusEM")
    protected EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
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
    @Rollback
    public void updateUsers_notLoggedIn_atLeastOneAdmin() {
        eu.domibus.api.user.User apiUser = initTestUser("otherUser");
        apiUser.setAuthorities(Arrays.asList("ROLE_USER"));
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

        UserRole userRole = entityManager.find(UserRole.class, 1L);
        if (userRole == null) {
            userRole = new UserRole("ROLE_USER");
            entityManager.persist(userRole);
            userRole = new UserRole("ROLE_ADMIN");
            entityManager.persist(userRole);
        }
        User userEntity = new User();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);
        userEntity.addRole(userRole);
        userEntity.setEmail(email);
        userEntity.setActive(true);
        userDao.create(userEntity);

        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName(userName);
        user.setPassword(password);
        user.setAuthorities(Arrays.asList(userRole.getName()));
        user.setEmail(email);
        user.setActive(true);
        user.setStatus(UserState.UPDATED.name());
        return user;
    }
}