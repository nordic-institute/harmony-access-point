package eu.domibus.core.user;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomain;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.converter.AuthCoreMapper;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.UserRoleDao;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import eu.domibus.web.security.AuthenticationService;
import eu.domibus.web.security.DomibusUserDetails;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Injectable
    private AuthCoreMapper authCoreMapper;

    @Injectable
    AuthenticationService authenticationService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

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
            authCoreMapper.userApiToUserSecurity(addedUser);
            result = addedUserUntity;
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
            authCoreMapper.userApiToUserSecurity(addedUser);
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
    public void insertNewUsersMultiTenantTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("addedUserName");
            setActive(true);
            setStatus(UserState.NEW.name());
            setDomain("Domain2");
        }};
        User addedUserEntity = new User() {{
            setPassword("password1");
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authCoreMapper.userApiToUserSecurity(addedUser);
            result = addedUserEntity;
        }};

        userPersistenceService.insertNewUsers(addedUsers);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(addedUser.getDomain());
            times = 1;
            userDao.create(addedUserEntity);
            times = 1;
            userDomainService.setDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 1;
            userDomainService.setPreferredDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 0;
        }};
    }

    @Test
    public void insertNewSuperUsersMultiTenantTest() {
        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName("super1");
            setActive(true);
            setStatus(UserState.NEW.name());
            setDomain("default");
            setAuthorities(Arrays.asList("ROLE_AP_ADMIN"));
        }};
        User addedUserEntity = new User() {{
            setPassword("password1");
        }};
        List<eu.domibus.api.user.User> addedUsers = Arrays.asList(addedUser);

        new Expectations(userPersistenceService) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            domainContextProvider.getCurrentDomainSafely();
            result = null;
            authCoreMapper.userApiToUserSecurity(addedUser);
            result = addedUserEntity;
            userPersistenceService.addRoleToUser(addedUser.getAuthorities(), addedUserEntity);
        }};

        userPersistenceService.insertNewUsers(addedUsers);

        new Verifications() {{
            userDao.create(addedUserEntity);
            times = 1;
            userDomainService.setDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 0;
            userDomainService.setPreferredDomainForUser(addedUser.getUserName(), addedUser.getDomain());
            times = 1;
        }};
    }

    @Test
    public void deleteUsersMultiTenantTest() {
        eu.domibus.api.user.User deletedUser = new eu.domibus.api.user.User() {{
            setUserName("deletedUserName");
            setActive(true);
            setStatus(UserState.REMOVED.name());
            setDomain("domain2");
        }};
        List<eu.domibus.api.user.User> deletedUsers = Arrays.asList(deletedUser);
        User deletedUserEntity = new User() {{
            setPassword("password1");
        }};

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDao.loadUserByUsername(deletedUser.getUserName());
            result = deletedUserEntity;
        }};

        userPersistenceService.deleteUsers(deletedUsers);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(deletedUser.getDomain());
            times = 1;
            userDao.delete(deletedUserEntity);
            times = 1;
            userSessionsService.invalidateSessions(deletedUserEntity);
            times = 1;
        }};
    }

    @Test
    public void updatedUsersMultiTenantTest() {
        eu.domibus.api.user.User modifiedUser = new eu.domibus.api.user.User() {{
            setUserName("modifiedUserName");
            setActive(true);
            setStatus(UserState.UPDATED.name());
            setDomain("domain2");
        }};
        List<eu.domibus.api.user.User> modifiedUsers = Arrays.asList(modifiedUser);
        User updatedUserEntity = new User() {{
            setPassword("password1");
        }};

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDao.loadUserByUsername(modifiedUser.getUserName());
            result = updatedUserEntity;
        }};

        userPersistenceService.updateUsers(modifiedUsers, false);

        new Verifications() {{
            domainContextProvider.setCurrentDomain(modifiedUser.getDomain());
            times = 1;
            securityPolicyManager.applyLockingPolicyOnUpdate(modifiedUser, updatedUserEntity);
            times=1;
            userDao.update(updatedUserEntity);
            times = 1;
        }};
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

        new Expectations(userPersistenceService) {{
            userDao.loadUserByUsername(anyString);
            result = userEntity;
            userPersistenceService.checkCanUpdateIfCurrentUser(user, userEntity);
        }};

        userPersistenceService.updateUsers(users, true);

        new Verifications() {{
            securityPolicyManager.applyLockingPolicyOnUpdate(user, userEntity);
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
    public void filterModifiedUsersTest(@Injectable eu.domibus.api.user.User user1) {

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
    public void isPasswordChangedTest(@Injectable eu.domibus.api.user.User user1) {

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
    public void isUpdatedTest(@Injectable eu.domibus.api.user.User user1) {

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

    @Test
    public void checkCanUpdateIfCurrentUser(@Injectable eu.domibus.api.user.User user, @Injectable User existing, @Injectable DomibusUserDetails loggedUser) {
        String userName1 = "userName1", userName2 = "userName2";
        new Expectations(userPersistenceService) {{
            authenticationService.getLoggedUser();
            result = loggedUser;
            loggedUser.getUsername();
            result = userName1;
            user.getUserName();
            result = userName2;
        }};

        userPersistenceService.checkCanUpdateIfCurrentUser(user, existing);

        new Verifications() {{
            existing.isActive();
            times = 0;
        }};
    }

    @Test(expected = UserManagementException.class)
    public void checkCanUpdateIfCurrentUser_ChangeActive(@Injectable eu.domibus.api.user.User user, @Injectable User existing, @Injectable DomibusUserDetails loggedUser) {
        String userName = "userName";
        new Expectations() {{
            authenticationService.getLoggedUser();
            result = loggedUser;
            loggedUser.getUsername();
            result = userName;
            user.getUserName();
            result = userName;
            existing.isActive();
            result = true;
            user.isActive();
            result = false;
        }};

        userPersistenceService.checkCanUpdateIfCurrentUser(user, existing);
    }

    @Test(expected = UserManagementException.class)
    public void checkCanUpdateIfCurrentUser_ChangeRole(@Injectable eu.domibus.api.user.User user, @Injectable User existing, @Injectable DomibusUserDetails loggedUser) {
        String userName = "userName";
        new Expectations(userPersistenceService) {{
            authenticationService.getLoggedUser();
            result = loggedUser;
            loggedUser.getUsername();
            result = userName;
            user.getUserName();
            result = userName;
            existing.isActive();
            result = true;
            user.isActive();
            result = true;
            userPersistenceService.sameRoles(user, existing);
            result = false;
        }};

        userPersistenceService.checkCanUpdateIfCurrentUser(user, existing);

        new Verifications() {{
            existing.isActive();
        }};
    }
}
