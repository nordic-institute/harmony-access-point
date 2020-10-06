package eu.domibus.core.user.ui;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.user.ui.converters.UserConverter;
import eu.domibus.core.user.ui.security.ConsoleUserSecurityPolicyManager;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserManagementServiceImplTest {

    @Tested
    private UserManagementServiceImpl userManagementService;

    @Injectable
    ConsoleUserSecurityPolicyManager userPasswordManager;

    @Injectable
    protected UserDao userDao;

    @Injectable
    UserRoleDao userRoleDao;

    @Injectable
    ConsoleUserPasswordHistoryDao consoleUserPasswordHistoryDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    UserConverter userConverter;

    @Injectable
    UserPersistenceService userPersistenceService;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Injectable
    ConsoleUserAlertsServiceImpl consoleUserAlertsService;

    @Test
    public void findUsersTest() {
        User userEntity = new User();
        userEntity.setPassword("user1");
        List<User> userEntities = Collections.singletonList(userEntity);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName("user1");
        List<eu.domibus.api.user.User> users = Collections.singletonList(user);
        String domainCode = "default";

        new Expectations() {{
            userDao.listUsers();
            result = userEntities;
            userConverter.convert(userEntity);
            result = user;
            userDomainService.getDomainForUser(user.getUserName());
            result = domainCode;
        }};

        List<eu.domibus.api.user.User> result = userManagementService.findUsers();

        Assert.assertEquals(users, result);
        Assert.assertEquals(domainCode, result.get(0).getDomain());
    }

    @Test
    public void findUserRoles() {
        UserRole userEntity = new UserRole("userRole1");
        List<UserRole> userEntities = Collections.singletonList(userEntity);

        new Expectations() {{
            userRoleDao.listRoles();
            result = userEntities;
        }};

        List<eu.domibus.api.user.UserRole> result = userManagementService.findUserRoles();

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("userRole1", result.get(0).getRole());
    }

    @Test
    public void handleAuthenticationPolicyNoAttemptUpgrade(final @Mocked User user) {
        String userName = "user1";
        userManagementService.handleWrongAuthentication(userName);
        new Verifications() {{
            userPasswordManager.handleWrongAuthentication(userName);
            times = 1;
        }};
    }

    @Test
    public void undoUserSuspension(@Mocked final System system) {
        userManagementService.reactivateSuspendedUsers();

        new Verifications() {{
            userPasswordManager.reactivateSuspendedUsers();
            times = 1;
        }};
    }

    @Test
    public void handleCorrectAuthenticationWithSomeFaileAttempts() {
        String userName = "user1";
        userManagementService.handleCorrectAuthentication(userName);
        new Verifications() {{
            userPasswordManager.handleCorrectAuthentication(userName);
            times = 1;
        }};
    }

    @Test
    public void handleCorrectAuthenticationTest() {
        String userName = "user1";
        userManagementService.handleCorrectAuthentication(userName);
        new Verifications() {{
            userPasswordManager.handleCorrectAuthentication(userName);
            times = 1;
        }};
    }

    @Test(expected = UserManagementException.class)
    public void getUserWithName_null() {
        new Expectations() {{
            userDao.findByUserName("username");
            result = null;
        }};
        userManagementService.getUserWithName("username");
    }

    @Test(expected = UserManagementException.class)
    public void getUserWithName_ok() {
        new Expectations() {{
            userDao.findByUserName("username");
            result = null;
        }};
        userManagementService.getUserWithName("username");
    }

    @Test
    public void validateExpiredPasswordTest() {
        final LocalDateTime passwordChangeDate = LocalDateTime.of(2018, 9, 15, 15, 58, 59);
        final String username = "user1";
        final User user = new User();
        user.setUserName(username);
        user.setPasswordChangeDate(passwordChangeDate);
        user.setDefaultPassword(true);

        new Expectations() {{
            userDao.findByUserName(username);
            result = user;
        }};

        userManagementService.validateExpiredPassword(username);

        new Verifications() {{
            userPasswordManager.validatePasswordExpired(username, true, passwordChangeDate);
            times = 1;
        }};
    }

    @Test
    public void testValidateDaysTillExpiration() {
        final LocalDateTime passwordChangeDate = LocalDateTime.of(2018, 9, 15, 15, 58, 59);

        final String username = "user1";
        final User user = new User();
        user.setUserName(username);
        user.setPasswordChangeDate(passwordChangeDate);
        user.setDefaultPassword(true);

        new Expectations() {{
            userDao.findByUserName(username);
            result = user;
        }};

        Integer result = userManagementService.getDaysTillExpiration(username);

        Assert.assertEquals(0, result.longValue());
        new Verifications() {{
            userPasswordManager.getDaysTillExpiration(username, true, passwordChangeDate);
            times = 1;
        }};
    }

    @Test
    public void validateAtLeastOneOfRoleTest_nok(@Injectable AuthRole role) {

        User deletedUser = new User() {{
            setDeleted(true);
            setActive(true);
        }};
        User inactiveUser = new User() {{
            setDeleted(true);
            setActive(true);
        }};
        List<User> users = Arrays.asList(
                deletedUser,
                inactiveUser);
        new Expectations() {{
            userDao.findByRole(role.toString());
            result = users;
        }};
        try {
            userManagementService.validateAtLeastOneOfRole(role);
            Assert.fail();
        } catch (UserManagementException ex) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, ex.getError());
        }
    }

    @Test
    public void validateAtLeastOneOfRoleTest_ok(@Injectable AuthRole role) {
        User validUser = new User() {{
            setDeleted(false);
            setActive(true);
        }};
        User deletedUser = new User() {{
            setDeleted(true);
            setActive(true);
        }};
        User inactiveUser = new User() {{
            setDeleted(true);
            setActive(true);
        }};
        List<User> users = Arrays.asList(validUser,
                deletedUser,
                inactiveUser);

        new Expectations() {{
            userDao.findByRole(role.toString());
            result = users;
        }};
        userManagementService.validateAtLeastOneOfRole(role);

        new FullVerifications(){};
    }

    @Test
    public void prepareUsers(@Mocked Function<eu.domibus.api.user.User, String> getDomainForUserFn) {
        User userEntity = new User();
        userEntity.setPassword("user1");
        List<User> userEntities = Collections.singletonList(userEntity);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName("user1");
        List<eu.domibus.api.user.User> users = Collections.singletonList(user);

        new Expectations(userManagementService) {{
            userManagementService.convertAndPrepareUser(getDomainForUserFn, userEntity);
            result = user;
        }};

        List<eu.domibus.api.user.User> result = userManagementService.prepareUsers(getDomainForUserFn, userEntities);

        Assert.assertEquals(users, result);
    }

    @Test
    public void convertAndPrepareUser(@Mocked Function<eu.domibus.api.user.User, String> getDomainForUserFn) {
        User userEntity = new User();
        userEntity.setPassword("user1");
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setUserName("user1");
        String domainCode = "default";
        LocalDateTime expDate = LocalDateTime.now().plusDays(30);

        new Expectations() {{
            userConverter.convert(userEntity);
            result = user;
            getDomainForUserFn.apply(user);
            result = domainCode;
            userPasswordManager.getExpirationDate(userEntity);
            result = expDate;
        }};

        eu.domibus.api.user.User result = userManagementService.convertAndPrepareUser(getDomainForUserFn, userEntity);

        Assert.assertEquals(user, result);
        Assert.assertEquals(domainCode, result.getDomain());
        Assert.assertEquals(expDate, result.getExpirationDate());
    }

    @Test
    public void updateUsers() {
        ArrayList<eu.domibus.api.user.User> users = new ArrayList<>();

        userManagementService.updateUsers(users);

        new FullVerifications(){{
            userPersistenceService.updateUsers(users);
            times = 1;
        }};

    }
    @Test
    public void triggerPasswordAlerts() {
        userManagementService.triggerPasswordAlerts();

        new FullVerifications(){{
            consoleUserAlertsService.triggerPasswordExpirationEvents();
            times = 1;
        }};
    }

    @Test
    public void changePassword() {
        String username = "username";
        String         currentPassword = "currentPassword";
        String newPassword = "newPassword";
        userManagementService.changePassword(username,
                currentPassword,
                newPassword);

        new FullVerifications(){{
            userPersistenceService.changePassword(username, currentPassword, newPassword);
            times = 1;
        }};
    }
}

