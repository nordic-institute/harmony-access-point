package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.disabled.console.ConsoleAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.enabled.console.ConsoleAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.login.LoginFailureModuleConfiguration;
import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;
import eu.domibus.core.user.*;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.ui.converters.UserConverter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserAlertsServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserPersistenceService userPersistenceService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UserConverter userConverter;

    @Injectable
    private ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

    @Injectable
    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

    @Injectable
    private EventService eventService;

    @Injectable
    protected UserDomainService userDomainService;

    @Injectable
    protected DomainService domainService;

    @Tested
    private UserAlertsServiceImpl userAlertsService;

    @Test
    public void testSendPasswordExpiredAlerts(@Mocked UserDaoBase<UserEntityBase> dao,
                                              @Mocked PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 10;
        final Integer howManyDaysToGenerateAlertsAfterExpiration = 3;
        final LocalDate from = LocalDate.of(2018, 10, 2);
        final LocalDate to = LocalDate.of(2018, 10, 5);
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        final User user2 = new User() {{
            setUserName("user2");
            setPassword("anypassword");
        }};
        final List<User> users = Arrays.asList(user1, user2);

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations(userAlertsService) {{
            userAlertsService.getExpiredAlertConfiguration();
            result = alertConfiguration;
            alertConfiguration.isActive();
            result = true;
            alertConfiguration.getEventDelay();
            result = howManyDaysToGenerateAlertsAfterExpiration;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE;
            domibusPropertyProvider.getIntegerProperty(ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE);
            result = maxPasswordAge;
            userAlertsService.getUserDao();
            result = dao;
            dao.findWithPasswordChangedBetween(from, to, false);
            result = users;
            userAlertsService.getEventTypeForPasswordExpired();
            result = EventType.PASSWORD_EXPIRED;
        }};

        userAlertsService.triggerExpiredEvents(false);

        new VerificationsInOrder() {{
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_EXPIRED, (User) any, maxPasswordAge, alertConfiguration);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordImminentExpirationAlerts(@Mocked UserDaoBase<UserEntityBase> dao,
                                                         @Mocked PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 10;
        final Integer howManyDaysBeforeExpirationToGenerateAlerts = 4;
        final LocalDate from = LocalDate.of(2018, 10, 5);
        final LocalDate to = LocalDate.of(2018, 10, 9);
        final UserEntityBase user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        final UserEntityBase user2 = new User() {{
            setUserName("user2");
            setPassword("anypassword");
        }};
        final List<UserEntityBase> users = Arrays.asList(user1, user2);

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations(userAlertsService) {{
            userAlertsService.getImminentExpirationAlertConfiguration();
            result = alertConfiguration;
            alertConfiguration.isActive();
            result = true;
            alertConfiguration.getEventDelay();
            result = howManyDaysBeforeExpirationToGenerateAlerts;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE;
            domibusPropertyProvider.getIntegerProperty(ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE);
            result = maxPasswordAge;
            userAlertsService.getEventTypeForPasswordImminentExpiration();
            result = EventType.PASSWORD_IMMINENT_EXPIRATION;
            userAlertsService.getUserDao();
            result = dao;
            dao.findWithPasswordChangedBetween(from, to, false);
            result = users;
        }};

        userAlertsService.triggerImminentExpirationEvents(false);

        new VerificationsInOrder() {{
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_IMMINENT_EXPIRATION, (UserEntityBase) any,
                    maxPasswordAge, alertConfiguration);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordImminentExpirationAlerts_inactive(
            @Mocked PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        new Expectations(userAlertsService) {{
            userAlertsService.getEventTypeForPasswordImminentExpiration();
            result = EventType.PASSWORD_IMMINENT_EXPIRATION;

            userAlertsService.getImminentExpirationAlertConfiguration();
            result = alertConfiguration;

            alertConfiguration.isActive();
            result = false;
        }};

        userAlertsService.triggerImminentExpirationEvents(false);

        new FullVerifications() {
        };
    }

    @Test
    public void testSendPasswordAlerts() {
        new Expectations(userAlertsService) {{
            userAlertsService.triggerExpiredEvents(true);
            userAlertsService.triggerExpiredEvents(false);
            userAlertsService.triggerImminentExpirationEvents(true);
            userAlertsService.triggerImminentExpirationEvents(false);
        }};

        userAlertsService.triggerPasswordExpirationEvents();

        new FullVerifications() {
        };
    }

    @Test
    public void testSendPasswordAlerts_exception() {
        new Expectations(userAlertsService) {{
            userAlertsService.triggerExpiredEvents(true);
            result = new Exception("ERROR");
            userAlertsService.triggerImminentExpirationEvents(true);
            result = new Exception("ERROR");
        }};

        userAlertsService.triggerPasswordExpirationEvents();

        new FullVerifications() {
        };
    }

    @Test
    public void doNotSendPasswordExpiredEventsIfPasswordExpirationIsDisabled(@Mocked PasswordExpirationAlertModuleConfiguration alertConfiguration) {
        new Expectations() {{
            userAlertsService.getExpiredAlertConfiguration();
            result = alertConfiguration;
            alertConfiguration.isActive();
            result = true;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = "propertyNameToCheck";
            domibusPropertyProvider.getIntegerProperty("propertyNameToCheck");
            result = 0;
        }};

        userAlertsService.triggerExpiredEvents(false);

        new VerificationsInOrder() {{
            userAlertsService.getUserDao();
            times = 0;
        }};
    }

    @Test
    public void triggerDisabledEventTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        AccountDisabledModuleConfiguration conf = new AccountDisabledModuleConfiguration(AlertType.USER_ACCOUNT_DISABLED,
                AlertLevel.MEDIUM, AccountDisabledMoment.AT_LOGON, "");
        new Expectations(userAlertsService) {{
            userAlertsService.getAccountDisabledConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerDisabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueAccountDisabledEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date) any);
            times = 1;
        }};
    }

    @Test
    public void triggerEnabledEventTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        AlertModuleConfigurationBase conf = new AlertModuleConfigurationBase(AlertType.USER_ACCOUNT_ENABLED,
                AlertLevel.MEDIUM, "");
        new Expectations(userAlertsService) {{
            userAlertsService.getAccountEnabledConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerEnabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueAccountEnabledEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date) any);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest_BAD_CREDENTIALS(
            @Mocked LoginFailureModuleConfiguration LoginFailureModuleConfiguration) {

        new Expectations(userAlertsService) {{
            LoginFailureModuleConfiguration.isActive();
            result = true;

            userAlertsService.getLoginFailureConfiguration();
            result = LoginFailureModuleConfiguration;

            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.BAD_CREDENTIALS);

        new VerificationsInOrder() {{
            eventService.enqueueLoginFailureEvent(UserEntityBase.Type.CONSOLE, "user1", (Date) any, false);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_inactive(
            @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Mocked LoginFailureModuleConfiguration LoginFailureModuleConfiguration) {

        new Expectations(userAlertsService) {{
            LoginFailureModuleConfiguration.isActive();
            result = true;
            userAlertsService.getLoginFailureConfiguration();
            result = LoginFailureModuleConfiguration;

            userAlertsService.getAccountDisabledConfiguration();
            result = accountDisabledConfiguration;

            accountDisabledConfiguration.isActive();
            result = false;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.SUSPENDED);

        new FullVerifications() {
        };
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_active_eachLogin(
            @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Mocked LoginFailureModuleConfiguration LoginFailureModuleConfiguration) {

        new Expectations(userAlertsService) {{
            LoginFailureModuleConfiguration.isActive();
            result = true;

            userAlertsService.getLoginFailureConfiguration();
            result = LoginFailureModuleConfiguration;

            userAlertsService.getAccountDisabledConfiguration();
            result = accountDisabledConfiguration;

            accountDisabledConfiguration.isActive();
            result = true;

            accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin();
            result = true;

            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.SUSPENDED);

        new FullVerifications() {{
            eventService.enqueueAccountDisabledEvent(UserEntityBase.Type.CONSOLE, "user1", withAny(new Date()));
        }};
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_active_notEachLogin(
            @Mocked AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Mocked LoginFailureModuleConfiguration LoginFailureModuleConfiguration) {

        new Expectations(userAlertsService) {{
            LoginFailureModuleConfiguration.isActive();
            result = true;

            userAlertsService.getLoginFailureConfiguration();
            result = LoginFailureModuleConfiguration;

            userAlertsService.getAccountDisabledConfiguration();
            result = accountDisabledConfiguration;

            accountDisabledConfiguration.isActive();
            result = true;

            accountDisabledConfiguration.shouldTriggerAccountDisabledAtEachLogin();
            result = false;

            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.SUSPENDED);

        new FullVerifications() {{
            eventService.enqueueLoginFailureEvent(UserEntityBase.Type.CONSOLE, "user1", withAny(new Date()), true);
        }};
    }

}
