package eu.domibus.core.user;

import eu.domibus.api.multitenancy.UserDomain;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

/**
 * @author Thomas Dussart, Ion Perpegel
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserPersistenceServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    private BCryptPasswordEncoder bCryptEncoder;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private ConsoleUserSecurityPolicyManager securityPolicyManager;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AlertConfigurationService alertConfigurationService;

    @Injectable
    private EventService eventService;

    @Injectable
    UserSessionsService userSessionsService;

    @Autowired
    private DomainCoreConverter domainConverter2;

    @Tested
    private UserPersistenceServiceImpl userPersistenceService;

    @Test
    public void updateUsersTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("addedUserName");
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);
        User addedUserUntity = new User() {{
            setPassword("password1");
        }};

        eu.domibus.api.user.User modifiedUser = new eu.domibus.api.user.User() {{
            setUserName("modifiedUserName");
            setActive(true);
            setStatus(UserState.UPDATED.name());
        }};
        List<eu.domibus.api.user.User> modifiedUsers = Arrays.asList(modifiedUser);

        eu.domibus.api.user.User deletedUser = new eu.domibus.api.user.User() {{
            setUserName("deletedUserName");
            setActive(true);
            setStatus(UserState.REMOVED.name());
        }};
        List<eu.domibus.api.user.User> deletedUsers = Arrays.asList(deletedUser);

        List<eu.domibus.api.user.User> users = Arrays.asList(addedUser, modifiedUser, deletedUser);

        new Expectations() {{
            domainConverter.convert(addedUser, User.class);
            returns(addedUserUntity);
        }};

        userPersistenceService.updateUsers(users);

        new Verifications() {{
            userPersistenceService.updateUsers(modifiedUsers, false);
            times = 1;
            userPersistenceService.deleteUsers(deletedUsers);
            times = 1;
        }};
    }

    @Test
    public void insertNewUsersTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("addedUserName");
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        User addedUserUntity = new User() {{
            setPassword("password1");
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations() {{
            domainConverter.convert(addedUser, User.class);
            result = addedUserUntity;
        }};

        userPersistenceService.insertNewUsers(addedUsers);

        new Verifications() {{
            userDao.create(addedUserUntity);
            times = 1;
            userDomainService.setDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 1;
            userDomainService.setPreferredDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 0;
        }};
    }


    @Test(expected = UserManagementException.class)
    public void insertNewUsersShouldFailIfUsernameAlreadyExists() {
        String testUsername = "testUsername";
        String testDomain = "testDomain";

        UserDomain existingUser = new UserDomain();
        existingUser.setUserName(testUsername);
        existingUser.setDomain(testDomain);

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations() {{
            securityPolicyManager.validateUniqueUser(addedUser);
            result = new UserManagementException("");
        }};

        userPersistenceService.insertNewUsers(addedUsers);
    }

    @Test
    public void internalUpdateUsersTest() {
        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};
        eu.domibus.api.user.User user = new eu.domibus.api.user.User() {{
            setActive(true);
            setAuthorities(Arrays.asList(AuthRole.ROLE_AP_ADMIN.name()));
        }};
        Collection<eu.domibus.api.user.User> users = Arrays.asList(user);

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};

        userPersistenceService.updateUsers(users, true);

        new Verifications() {{
            securityPolicyManager.applyLockingPolicyOnUpdate(user);
            times = 1;
            securityPolicyManager.changePassword(userEntity, user.getPassword());
            times = 1;
            userEntity.setEmail(user.getEmail());
            times = 1;
            userPersistenceService.addRoleToUser(user.getAuthorities(), userEntity);
            times = 1;
            userDao.update(userEntity);
            times = 1;
            userDomainService.setPreferredDomainForUser(user.getUserName(), user.getDomain());
            times = 1;
        }};

    }

    @Test(expected = UserManagementException.class)
    public void changePasswordPasswordsNotMatchTest() {
        String userName = "user1";
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";

        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
        }};

        userPersistenceService.changePassword(userName, currentPassword, newPassword);

        new Verifications() {{
            userDao.update(userEntity);
            times = 1;
        }};

    }

    @Test
    public void changePasswordTest() {
        String userName = "user1";
        String currentPassword = "currentPassword";
        String newPassword = "newPassword";

        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};

        new Expectations() {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
            bCryptEncoder.matches(currentPassword, userEntity.getPassword());
            result = true;
        }};

        userPersistenceService.changePassword(userName, currentPassword, newPassword);

        new VerificationsInOrder() {{
            securityPolicyManager.changePassword(userEntity, newPassword);
            times = 1;
            userDao.update(userEntity);
            times = 1;
        }};

    }

    @Test
    public void updateRolesIfNecessaryTest() {
        final User existing = new User() {{
            setUserName("user1");
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
            addRole(new UserRole(AuthRole.ROLE_ADMIN.name()));
        }};

        eu.domibus.api.user.User user = new eu.domibus.api.user.User() {{
            setUserName("user1");
            setActive(true);
            setAuthorities(Arrays.asList(AuthRole.ROLE_USER.name()));
        }};

        new Expectations(existing) {{
            existing.clearRoles();
        }};

        userPersistenceService.updateRolesIfNecessary(user, existing);

        new Verifications() {{
            userSessionsService.invalidateSessions(existing);
            times = 1;
        }};

    }

    @Test
    public void filterModifiedUsersTest(@Mocked eu.domibus.api.user.User user1) {

        List<eu.domibus.api.user.User> users = new ArrayList<>();
        users.add(user1);

        new Expectations(userPersistenceService) {{
            userPersistenceService.isUpdated(user1);
            result = true;

            userPersistenceService.isPasswordChanged(user1);
            returns(false, true, false, true);
        }};

        Collection<eu.domibus.api.user.User> result1 = userPersistenceService.filterModifiedUserWithoutPasswordChange(users);
        Assert.assertTrue(result1.size() == 1);
        Collection<eu.domibus.api.user.User> result2 = userPersistenceService.filterModifiedUserWithoutPasswordChange(users);
        Assert.assertTrue(result2.isEmpty());

        Collection<eu.domibus.api.user.User> result3 = userPersistenceService.filterModifiedUserWithPasswordChange(users);
        Assert.assertTrue(result3.isEmpty());
        Collection<eu.domibus.api.user.User> result4 = userPersistenceService.filterModifiedUserWithPasswordChange(users);
        Assert.assertTrue(result4.size() == 1);

    }

    @Test
    public void isPasswordChangedTest(@Mocked eu.domibus.api.user.User user1) {

        new Expectations(userPersistenceService) {{
            user1.getPassword();
            returns(StringUtils.EMPTY, "newPass", null);
        }};

        boolean res1 = userPersistenceService.isPasswordChanged(user1);
        Assert.assertFalse(res1);
        boolean res2 = userPersistenceService.isPasswordChanged(user1);
        Assert.assertTrue(res2);
        boolean res3 = userPersistenceService.isPasswordChanged(user1);
        Assert.assertFalse(res3);
    }

    @Test
    public void isUpdatedTest(@Mocked eu.domibus.api.user.User user1) {

        new Expectations(userPersistenceService) {{
            user1.getStatus();
            returns(UserState.UPDATED.name(), UserState.REMOVED.name(), UserState.PERSISTED.name());
        }};

        boolean res1 = userPersistenceService.isUpdated(user1);
        Assert.assertTrue(res1);
        boolean res2 = userPersistenceService.isUpdated(user1);
        Assert.assertFalse(res2);
        boolean res3 = userPersistenceService.isUpdated(user1);
        Assert.assertFalse(res3);
    }
}
