package eu.domibus.core.user;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.multitenancy.UserSessionsService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.core.alerts.service.ConsoleUserAlertsServiceImpl;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.security.password.ConsoleUserPasswordHistoryDao;
import mockit.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.time.LocalDateTime.of;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
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

    @Tested
    UserSecurityPolicyManager<User> securityPolicyManager;

    @Test
    public void checkPasswordComplexity_blank() {
        String userName = "user1";

        String testPassword = "";
        try {
            securityPolicyManager.validateComplexity(userName, testPassword);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_ok1() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        String testPassword1 = "Lalala-5";
        securityPolicyManager.validateComplexity(userName, testPassword1);
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_ok2() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        String testPassword2 = "UPPER lower 12345 /`~!@#$%^&*()-"; // 32 characters
        securityPolicyManager.validateComplexity(userName, testPassword2);
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_ok3() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        String testPassword3 = "Aa`()-_=+\\|,<.>/?;:'\"|\\[{]}.0";
        securityPolicyManager.validateComplexity(userName, testPassword3);
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_ok4() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        char[] specialCharacters = new char[]{'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '+', '=', '-', '_',
                '|', '(', ')', '[', ']', '{', '}', '?', '/', ';', ':', ',', '.', '<', '>', '\'', '\"', '\\'};
        for (char c : specialCharacters) {
            String testPassword4 = "AlphaNum3ric " + c;
            securityPolicyManager.validateComplexity(userName, testPassword4);
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_min8() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // Minimum length: 8 characters
        try {
            String invalidPassword1 = "Lala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_max32() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // Maximum length: 32 characters
        try {
            String invalidPassword1 = "UPPER lower 12345 /`~!@#$%^&*()- 12345";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_lowerCase() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // At least one letter in lowercase
        try {
            String invalidPassword1 = "LALALA-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_uppercase() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // At least one letter in uppercase
        try {
            String invalidPassword1 = "lalala-5";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_digit() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // At least one digit
        try {
            String invalidPassword1 = "lalala-LA";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void checkPasswordComplexity_nok_specialChar() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(securityPolicyManager.getPasswordComplexityPatternProperty());
            result = PASSWORD_COMPLEXITY_PATTERN;
        }};

        String userName = "user1";

        // At least one special character
        try {
            String invalidPassword1 = "Lalala55";
            securityPolicyManager.validateComplexity(userName, invalidPassword1);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusCoreException e2) {
            assertEquals(DomibusCoreErrorCode.DOM_001, e2.getError());
        }
        new FullVerifications() {
        };
    }

    @Test
    public void testPasswordHistoryDisabled() {
        String username = "user1";
        String testPassword = "testPassword123.";
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getPasswordHistoryPolicyProperty());
            result = 0;
        }};

        securityPolicyManager.validateHistory(username, testPassword);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateHistory_ok() {
        String username = "anyname";
        String testPassword = "anypassword";
        int oldPasswordsToCheck = 5;
        final User user = new User() {{
            setUserName(username);
            setPassword(testPassword);
        }};
        user.setDefaultPassword(true);
        List<UserPasswordHistory<User>> oldPasswords = Collections.singletonList(new UserPasswordHistory<>(user, testPassword, LocalDateTime.now()));

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
            result = false;
        }};

        securityPolicyManager.validateHistory(username, testPassword);
        new FullVerifications() {
        };
    }

    @Test(expected = DomibusCoreException.class)
    public void testValidateHistory_exception() {
        String username = "anyname";
        String testPassword = "anypassword";
        int oldPasswordsToCheck = 5;
        final User user = new User() {{
            setUserName(username);
            setPassword(testPassword);
        }};
        user.setDefaultPassword(true);
        List<UserPasswordHistory<User>> oldPasswords = Collections.singletonList(new UserPasswordHistory<>(user, testPassword, LocalDateTime.now()));

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
        new FullVerifications() {
        };

    }

    @Test
    public void testValidateDaysTillExpiration_warningDaysBeforeExpiration_0() {
        final LocalDateTime passwordChangeDate = of(2018, 9, 15, 15, 58, 59);

        final String username = "user1";

        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = "warningDaysBeforeExpirationProperty";
            domibusPropertyProvider.getIntegerProperty("warningDaysBeforeExpirationProperty");
            result = 0;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);

        assertNull(result);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpiration_maxPasswordAgeInDays_0() {
        final LocalDateTime passwordChangeDate = of(2018, 9, 15, 15, 58, 59);
        final Integer remainingDays = 15;


        final String username = "user1";
        final String maximumDefaultPasswordAgeProperty = "MaximumDefaultPasswordAgeProperty";

        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = "warningDaysBeforeExpirationProperty";
            domibusPropertyProvider.getIntegerProperty("warningDaysBeforeExpirationProperty");
            result = 20;
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = maximumDefaultPasswordAgeProperty;
            domibusPropertyProvider.getIntegerProperty(maximumDefaultPasswordAgeProperty);
            result = 0;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);

        assertNull(result);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpiration_LocalDateTimeIsNull() {
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate = of(2018, 9, 15, 15, 58, 59);
        ReflectionTestUtils.setField(passwordChangeDate, "date", null);

        final String username = "user1";
        final String maximumDefaultPasswordAgeProperty = "MaximumDefaultPasswordAgeProperty";

        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = "warningDaysBeforeExpirationProperty";
            domibusPropertyProvider.getIntegerProperty("warningDaysBeforeExpirationProperty");
            result = 40;
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = maximumDefaultPasswordAgeProperty;
            domibusPropertyProvider.getIntegerProperty(maximumDefaultPasswordAgeProperty);
            result = maxPasswordAge;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);

        assertNull(result);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpiration_warningDaysBeforeExpiration_sup_maxPasswordAgeInDays() {
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate = of(2018, 9, 15, 15, 58, 59);

        final String username = "user1";
        final String maximumDefaultPasswordAgeProperty = "MaximumDefaultPasswordAgeProperty";

        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = "warningDaysBeforeExpirationProperty";
            domibusPropertyProvider.getIntegerProperty("warningDaysBeforeExpirationProperty");
            result = 50;
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = maximumDefaultPasswordAgeProperty;
            domibusPropertyProvider.getIntegerProperty(maximumDefaultPasswordAgeProperty);
            result = maxPasswordAge;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate);

        assertNull(result);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpiration_15() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate = of(2018, 9, 15, 15, 58, 59);
        final Integer remainingDays = 15;


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

        assertEquals(remainingDays, result);

        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpiration_null() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate2 = of(2019, 9, 15, 15, 58, 59);
        final Integer remainingDays2 = null;

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

        Integer result2 = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate2);

        assertEquals(remainingDays2, result2);
        new FullVerifications() {
        };

    }

    @Test
    public void testValidateDaysTillExpiration_1() {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 45;

        final LocalDateTime passwordChangeDate3 = of(2018, 9, 1, 15, 58, 59);
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

        Integer result3 = securityPolicyManager.getDaysTillExpiration(username, true, passwordChangeDate3);

        assertEquals(remainingDays3, result3);
        new FullVerifications() {
        };
    }

    @Test
    public void testValidateDaysTillExpirationDisabled() {
        final String username = "user1";
        new Expectations() {{
            securityPolicyManager.getWarningDaysBeforeExpirationProperty();
            result = null;
        }};

        Integer result = securityPolicyManager.getDaysTillExpiration(username, true, LocalDateTime.now());
        assertNull(result);
        new FullVerifications() {
        };
    }

    @Test
    public void testValidatePasswordExpired_ok() {
        final String username = "user1";
        final int defaultAge = 15;

        new Expectations(LocalDate.class) {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge;
        }};

        securityPolicyManager.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge - 1));

        new FullVerifications() {
        };

    }

    @Test
    public void testValidatePasswordExpired_pwdAge_0() {
        final String username = "user1";
        final int defaultAge = 0;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge;
        }};

        securityPolicyManager.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge + 1));

        new FullVerifications() {
        };

    }

    @Test(expected = CredentialsExpiredException.class)
    public void testValidatePasswordExpired() {
        final String username = "user1";
        final int defaultAge = 5;

        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(securityPolicyManager.getMaximumDefaultPasswordAgeProperty());
            result = defaultAge;
        }};

        securityPolicyManager.validatePasswordExpired(username, true, LocalDateTime.now().minusDays(defaultAge + 1));

        new FullVerifications() {
        };

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
    public void handleWrongAuthenticationSuspendedTest_noUser() {
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

        assertFalse(user.isActive());
        assertNotNull(user.getSuspensionDate());
    }

    @Test
    public void reactivateSuspendedUsersTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setAttemptCount(3);
            setActive(true);
            setSuspensionDate(new Date());
        }};

        List<UserEntityBase> users = Collections.singletonList(user1);

        new Expectations() {{
            securityPolicyManager.getSuspensionInterval();
            result = 1;
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.getSuspendedUsers((Date) any);
            result = users;
        }};

        securityPolicyManager.reactivateSuspendedUsers();

        assertTrue(user1.isActive());
        assertEquals(0, (long) user1.getAttemptCount());
        assertNull(user1.getSuspensionDate());

    }

    @Test
    public void reactivateSuspendedUsersTest_0() {
        final User user1 = new User() {{
            setUserName("user1");
            setAttemptCount(3);
            setActive(true);
            setSuspensionDate(new Date());
        }};

        new Expectations() {{
            securityPolicyManager.getSuspensionInterval();
            result = 0;

            securityPolicyManager.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        securityPolicyManager.reactivateSuspendedUsers();

        assertTrue(user1.isActive());
        assertEquals(3, (long) user1.getAttemptCount());
        assertNotNull(user1.getSuspensionDate());
    }

    @Test(expected = UserManagementException.class)
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists_domain() {
        String testUsername = "testUsername";
        String testDomain = "testDomain";

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

        new FullVerifications() {
        };
    }

    @Test(expected = UserManagementException.class)
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists_NoDomain() {
        String testUsername = "testUsername";

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDomainService.getDomainForUser(testUsername);
            result = null;
            userDomainService.getPreferredDomainForUser(testUsername);
            result = "preferredDomain";
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new FullVerifications() {
        };
    }

    @Test
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists_multiAware_ok() {
        String testUsername = "testUsername";

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            userDomainService.getDomainForUser(testUsername);
            result = null;
            userDomainService.getPreferredDomainForUser(testUsername);
            result = null;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new FullVerifications() {
        };
    }

    @Test
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists_noMultiAware_ok(@Mocked UserDaoBase<User> userDao) {
        String testUsername = "testUsername";

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};

        new Expectations(securityPolicyManager) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
            securityPolicyManager.getUserDao();
            result = userDao;
            userDao.existsWithId(testUsername);
            result = false;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new FullVerifications() {
        };
    }

    @Test(expected = UserManagementException.class)
    public void validateUniqueUserShouldFailIfUsernameAlreadyExists_noMultiAware_nok(@Mocked UserDaoBase<User> userDao) {
        String testUsername = "testUsername";

        eu.domibus.api.user.User addedUser = new eu.domibus.api.user.User() {{
            setUserName(testUsername);
            setActive(true);
            setStatus(UserState.NEW.name());
        }};

        new Expectations(securityPolicyManager) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            securityPolicyManager.getUserDao();
            result = userDao;

            userDao.existsWithId(testUsername);
            result = true;
        }};

        securityPolicyManager.validateUniqueUser(addedUser);

        new FullVerifications() {
        };
    }

    @Test
    public void getExpirationDate_noExpiration(@Mocked User userEntity) {

        new Expectations(securityPolicyManager) {{
            userEntity.hasDefaultPassword();
            result = false;
            securityPolicyManager.getMaximumPasswordAgeProperty();
            result = "propNme";
            domibusPropertyProvider.getIntegerProperty("propNme");
            result = 0;
        }};

        LocalDateTime res = securityPolicyManager.getExpirationDate(userEntity);
        assertNull(res);
    }

    @Test
    public void getExpirationDate_nullChangeDate(@Mocked User userEntity) {
        new Expectations(securityPolicyManager) {{
            userEntity.hasDefaultPassword();
            result = false;
            securityPolicyManager.getMaximumPasswordAgeProperty();
            result = "propNme";
            domibusPropertyProvider.getIntegerProperty("propNme");
            result = 30;
            userEntity.getPasswordChangeDate();
            result = null;
        }};

        LocalDateTime res = securityPolicyManager.getExpirationDate(userEntity);
        assertNull(res);
    }

    @Test
    public void getExpirationDate(@Mocked User userEntity) {
        LocalDateTime passChangeDate = LocalDateTime.now();

        new Expectations(securityPolicyManager) {{
            userEntity.hasDefaultPassword();
            result = true;
            securityPolicyManager.getMaximumDefaultPasswordAgeProperty();
            result = "propNme";
            domibusPropertyProvider.getIntegerProperty("propNme");
            result = 100;
            userEntity.getPasswordChangeDate();
            result = passChangeDate;
        }};

        LocalDateTime res = securityPolicyManager.getExpirationDate(userEntity);
        assertEquals(passChangeDate.plusDays(100), res);
    }

    @Test
    public void getLoginFailureReason_null() {
        String userName = "userName";
        UserLoginErrorReason loginFailureReason = securityPolicyManager.getLoginFailureReason(userName, null);
        assertEquals(UserLoginErrorReason.UNKNOWN, loginFailureReason);
        new FullVerifications() {
        };
    }

    @Test
    public void getLoginFailureReason_activeUser(@Mocked User user) {
        String userName = "userName";

        new Expectations() {{
            user.isActive();
            result = true;
        }};
        UserLoginErrorReason loginFailureReason = securityPolicyManager.getLoginFailureReason(userName, user);

        assertEquals(UserLoginErrorReason.BAD_CREDENTIALS, loginFailureReason);

        new FullVerifications() {
        };
    }

    @Test
    public void getLoginFailureReason_inactiveUser(@Mocked User user) {
        String userName = "userName";

        new Expectations() {{
            user.isActive();
            result = false;
            user.getSuspensionDate();
            result = null;
        }};
        UserLoginErrorReason loginFailureReason = securityPolicyManager.getLoginFailureReason(userName, user);

        assertEquals(UserLoginErrorReason.INACTIVE, loginFailureReason);

        new FullVerifications() {
        };
    }

    @Test
    public void getLoginFailureReason_inactiveUser_suspended(@Mocked User user) {
        String userName = "userName";

        new Expectations() {{
            user.isActive();
            result = false;
            user.getSuspensionDate();
            result = new Date();
        }};
        UserLoginErrorReason loginFailureReason = securityPolicyManager.getLoginFailureReason(userName, user);

        assertEquals(UserLoginErrorReason.SUSPENDED, loginFailureReason);

        new FullVerifications() {
        };
    }

    @Test
    public void savePasswordHistory_noSave(@Mocked User user) {
        new Expectations(securityPolicyManager) {{
            securityPolicyManager.getPasswordHistoryPolicyProperty();
            result = "passwordHistoryPolicyProperty";
            domibusPropertyProvider.getIntegerProperty("passwordHistoryPolicyProperty");
            result = 0;
        }};
        securityPolicyManager.savePasswordHistory(user);
    }

    @Test
    public void savePasswordHistory(@Mocked User user, @Mocked UserPasswordHistoryDao dao) {
        new Expectations(securityPolicyManager) {{
            securityPolicyManager.getPasswordHistoryPolicyProperty();
            result = "passwordHistoryPolicyProperty";

            domibusPropertyProvider.getIntegerProperty("passwordHistoryPolicyProperty");
            result = 10;

            securityPolicyManager.getUserHistoryDao();
            result = dao;

            user.getPassword();
            result = "pwd";

            user.getPasswordChangeDate();
            result = LocalDateTime.now();
        }};
        securityPolicyManager.savePasswordHistory(user);

        new FullVerifications() {{
            dao.savePassword(user, "pwd", withAny(LocalDateTime.now()));
            times = 1;
            dao.removePasswords(user, 10);
            times = 1;
        }};
    }
}
