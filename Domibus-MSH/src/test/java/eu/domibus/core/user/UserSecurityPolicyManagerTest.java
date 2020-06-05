package eu.domibus.core.user;

import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import mockit.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class UserSecurityPolicyManagerTest {

    private static final String PASSWORD_COMPLEXITY_PATTERN = "^.*(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&+=\\-_<>.,?:;*/()|\\[\\]{}'\"\\\\]).{8,32}$";

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    ConsoleUserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    ConsoleUserAlertsServiceImpl userAlertsService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    UserDao userDao;

    @Injectable
    BCryptPasswordEncoder bCryptEncoder;

    @Injectable
    UserDomainService userDomainService;

    @Injectable
    DomainService domainService;

    @Injectable
    private AlertConfigurationService alertConfigurationService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    UserSessionsService userSessionsService;

    @Tested
    UserSecurityPolicyManager securityPolicyManager;

    @Test
    public void checkPasswordComplexity() throws Exception {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

//        Password should follow all of these rules:
//        - Minimum length: 8 characters
//        - Maximum length: 32 characters
//        - At least one letter in lowercase
//        - At least one letter in uppercase
//        - At least one digit
//        - At least one special character


        // Happy Flow: No error should occur
        try {
            String testPassword1 = "Lalala-5";
            securityPolicyManager.validateComplexity(userName, testPassword1);

            String testPassword2 = "UPPER lower 12345 /`~!@#$%^&*()-"; // 32 characters
            securityPolicyManager.validateComplexity(userName, testPassword2);

            String testPassword3 = "Aa`()-_=+\\|,<.>/?;:'\"|\\[{]}.0";
            securityPolicyManager.validateComplexity(userName, testPassword3);

            char[] specialCharacters = new char[]{'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '+', '=', '-', '_',
                    '|', '(', ')', '[', ']', '{', '}', '?', '/', ';', ':', ',', '.', '<', '>', '\'', '\"', '\\'};
            for (char c : specialCharacters) {
                String testPassword4 = "AlphaNum3ric " + c;
                securityPolicyManager.validateComplexity(userName, testPassword4);
            }

        } catch (DomibusCoreException e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }

        // Minimum length: 8 characters
        try {
            String invalidPassword1 = "Lala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // Maximum length: 32 characters
        try {
            String invalidPassword1 = "UPPER lower 12345 /`~!@#$%^&*()- 12345";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in lowercase
        try {
            String invalidPassword1 = "LALALA-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one letter in uppercase
        try {
            String invalidPassword1 = "lalala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one digit
        try {
            String invalidPassword1 = "lalala-LA";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        // At least one special character
        try {
            String invalidPassword1 = "Lalala55";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
    }

    @Test
    public void testPasswordHistoryDisabled() throws Exception {
        String username = "user1";
        String testPassword = "testPassword123.";
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getPasswordHistoryPolicyProperty());
            result = 0;
        }};

        securityPolicyManager.validateHistory(username, testPassword);

        new VerificationsInOrder() {{
            userPasswordHistoryDao.getPasswordHistory((User) any, anyInt);
            times = 0;
        }};
    }

    @Test(expected = DomibusCoreException.class)
    public void testValidateHistory() throws Exception {
        String username = "anyname";
        String testPassword = "anypassword";
        int oldPasswordsToCheck = 5;
        final User user = new User() {{
            setUserName(username);
            setPassword(testPassword);
        }};
        user.setDefaultPassword(true);
        List<UserPasswordHistory> oldPasswords = Arrays.asList(new UserPasswordHistory(user, testPassword, LocalDateTime.now()));

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getPasswordHistoryPolicyProperty());
            result = oldPasswordsToCheck;
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(username);
            result = user;
            securityPolicyManager.getUserHistoryDao();
            result = userPasswordHistoryDao;
            userPasswordHistoryDao.getPasswordHistory(user, oldPasswordsToCheck);
            result = oldPasswords;
            bCryptEncoder.matches((CharSequence) any, anyString);
            result = true;
        }};

        securityPolicyManager.validateHistory(username, testPassword);
    }

    @Test
    public void testValidateDaysTillExpiration() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate = LocalDateTime.of(2018, 9, 15, 15, 58, 59);
        final Integer remainingDays = 15;

        final LocalDateTime passwordChangeDate2 = LocalDateTime.of(2019, 9, 15, 15, 58, 59);
        final Integer remainingDays2 = null;

        final LocalDateTime passwordChangeDate3 = LocalDateTime.of(2018, 9, 1, 15, 58, 59);
        final Integer remainingDays3 = 1;

        final String username = "user1";
        final String maximumDefaultPasswordAgeProperty = "MaximumDefaultPasswordAgeProperty";

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = "warningDaysBeforeExpirationProperty";
            domibusPropertyProvider.getIntegerProperty("warningDaysBeforeExpirationProperty");
            result = 20;
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = maximumDefaultPasswordAgeProperty;
            domibusPropertyProvider.getIntegerProperty(maximumDefaultPasswordAgeProperty);
            result = maxPasswordAge;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);
        Integer result2 = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate2);
        Integer result3 = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate3);

        assertEquals(remainingDays, result);
        assertEquals(remainingDays2, result2);
        assertEquals(remainingDays3, result3);
    }

    @Test
    public void testValidateDaysTillExpirationDisabled() {
        final String username = "user1";
        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = null;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, LocalDateTime.now());
        assertEquals(null, result);
    }

    @Test(expected = CredentialsExpiredException.class)
    public void testValidatePasswordExpired() {
        final String username = "user1";
        final Integer defaultAge = 5;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge;
        }};

        securityPolicyManager.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge + 1));

    }

    @Test
    public void applyLockingPolicyOnUpdateUnlock() {
        final User userEntity = new User() {{
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};
        eu.domibus.api.user.User user = new eu.domibus.api.user.User() {{
            setActive(true);
        }};
        new Expectations() {{
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(anyString);
            result = userEntity;
            securityPolicyManager.getUserAlertsService();
            result = userAlertsService;
        }};

        securityPolicyManager.applyLockingPolicyOnUpdate(user);

        new Verifications() {{
            userAlertsService.triggerEnabledEvent(user);
            times = 1;
        }};

        assertNull(userEntity.getSuspensionDate());
        assertEquals(0, userEntity.getAttemptCount(), 0d);
    }

    @Test
    public void applyLockingPolicyOnUpdateSendAlert() {
        final User userEntity = new User();
        userEntity.setActive(true);
        eu.domibus.api.user.User user = new eu.domibus.api.user.User();
        user.setActive(false);
        user.setUserName("user");

        new Expectations() {{
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(anyString);
            result = userEntity;
            securityPolicyManager.getUserAlertsService();
            result = userAlertsService;
        }};

        securityPolicyManager.applyLockingPolicyOnUpdate(user);

        new Verifications() {{
            userAlertsService.triggerDisabledEvent(user);
            times = 1;
        }};
    }

    @Test
    public void changePasswordTest() {
        String newPassword = "newPassword";

        final User user = new User() {{
            setActive(true);
            setUserName("user");
            setDefaultPassword(true);
        }};

        new Expectations() {{
            securityPolicyManager.getPasswordComplexityPatternProperty();
            result = "prop2";
            domibusPropertyProvider.getProperty("prop2");
            result = StringUtils.EMPTY;
            securityPolicyManager.getPasswordHistoryPolicyProperty();
            result = "prop3";
            domibusPropertyProvider.getIntegerProperty("prop3");
            result = 0;
            bCryptEncoder.encode(newPassword);
            result = "encoded_password";
        }};

        securityPolicyManager.changePassword(user, newPassword);

        new Verifications() {{
            userPasswordHistoryDao.savePassword(user, user.getPassword(), user.getPasswordChangeDate());
            times = 0;
        }};

        assertEquals(false, user.hasDefaultPassword());
        assertEquals("encoded_password", user.getPassword());
    }

    @Test
    public void handleCorrectAuthenticationTest() {
        String userName = "user1";
        final User userEntity = new User() {{
            setUserName(userName);
            setActive(true);
            setAttemptCount(3);
        }};

        new Expectations() {{
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(anyString);
            result = userEntity;
        }};

        securityPolicyManager.handleCorrectAuthentication(userName);

        assertEquals(0, userEntity.getAttemptCount(), 0d);
    }

    @Test
    public void handleWrongAuthenticationSuspendedTest() {
        String userName = "user1";
        final User user = new User() {{
            setUserName(userName);
            setActive(false);
            setSuspensionDate(new Date());
            setAttemptCount(5);
        }};

        new Expectations() {{
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(anyString);
            result = user;
            securityPolicyManager.getUserAlertsService();
            result = userAlertsService;
        }};

        securityPolicyManager.handleWrongAuthentication(userName);

        new Verifications() {{
            userAlertsService.triggerLoginEvents(userName, UserLoginErrorReason.SUSPENDED);
            times = 1;
        }};
    }

    @Test
    public void handleWrongAuthenticationBadCredentialsTest() {
        String userName = "user1";
        int attemptCount = 5;
        final User user = new User() {{
            setUserName(userName);
            setActive(true);
            setAttemptCount(attemptCount - 1);
        }};

        new Expectations() {{
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.findByUserName(anyString);
            result = user;
            securityPolicyManager.getMaxAttemptAmount(user);
            result = attemptCount;
            securityPolicyManager.getUserAlertsService();
            result = userAlertsService;
        }};

        securityPolicyManager.handleWrongAuthentication(userName);

        new Verifications() {{
            userAlertsService.triggerDisabledEvent(user);
            times = 1;
        }};

        assertEquals(false, user.isActive());
        assert (user.getSuspensionDate() != null);
    }

    @Test
    public void reactivateSuspendedUsersTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setActive(true);
        }};
        final User user2 = new User() {{
            setUserName("user2");
            setActive(false);
            setSuspensionDate(new Date());
        }};
        List<UserEntityBase> users = Arrays.asList(user1);

        new Expectations() {{
            securityPolicyManager.getSuspensionInterval();
            result = 1;
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.getSuspendedUsers((Date) any);
            result = users;
        }};

        securityPolicyManager.reactivateSuspendedUsers();

        assertEquals(true, user1.isActive());
        assertEquals((long) 0, (long) user1.getAttemptCount());
        assertEquals(null, user1.getSuspensionDate());

    }

    @Test(expected = UserManagementException.class)
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists() {
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

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDomainService.getDomainForUser(testUsername);
            result = testDomain;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDomainService.getDomainForUser(testUsername);
            result = null;
            userDomainService.getPreferredDomainForUser(testUsername);
            result = testDomain;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
            securityPolicyManager.getUserDao().existsWithId(testUsername);
            result = true;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);
    }
}
