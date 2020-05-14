package eu.domibus.core.user;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.user.ui.*;
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
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class UserManagementServiceImplTest {

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


    @Tested
    private UserManagementServiceImpl userManagementService;


    @Test
    public void findUsersTest() throws Exception {
        User userEntity = new User();
        List<User> userEntities = Arrays.asList(userEntity);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        List<eu.domibus.api.user.User> users = Arrays.asList(user);
        String domainCode = "default";

        new Expectations() {{
            userDao.listUsers();
            result = userEntities;
            userConverter.convert(userEntities);
            result = users;
            userDomainService.getDomainForUser(user.getUserName());
            result = domainCode;
        }};

        List<eu.domibus.api.user.User> result = userManagementService.findUsers();

        Assert.assertEquals(users, result);
        Assert.assertEquals(domainCode, result.get(0).getDomain());
    }

    @Test
    public void findUserRoles() throws Exception {
        UserRole userEntity = new UserRole("userRole1");
        List<UserRole> userEntities = Arrays.asList(userEntity);

        new Expectations() {{
            userRoleDao.listRoles();
            result = userEntities;
        }};

        List<eu.domibus.api.user.UserRole> result = userManagementService.findUserRoles();

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("userRole1", result.get(0).getRole());
    }

    @Test
    public void handleAuthenticationPolicyNoAttemptUpgrade(final @Mocked User user) throws Exception {
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
        final Integer maxPasswordAge = 45;

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

        new Verifications() {{
            userPasswordManager.getDaysTillExpiration(username, true, passwordChangeDate);
            times = 1;
        }};
    }

    @Test
    public void validateAtLeastOneOfRoleTest(@Injectable AuthRole role,
                                             @Injectable User user) {
        List<User> users = new ArrayList<>();
        long count = 0;

        new Expectations() {{
            userDao.findByRole(role.toString());
            result = users;
            users.stream().filter(u -> !u.isDeleted() && u.isActive()).count();
            result = count;
        }};
        try {
            userManagementService.validateAtLeastOneOfRole(role);
        } catch (UserManagementException ex) {
            Assert.assertEquals(DomibusCoreErrorCode.DOM_001, ex.getError());
            return;
        }
        Assert.fail();
    }
}

