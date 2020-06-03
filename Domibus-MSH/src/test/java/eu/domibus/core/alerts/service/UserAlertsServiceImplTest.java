package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.*;
import eu.domibus.core.user.ui.converters.UserConverter;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserPasswordHistoryDao;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
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
@RunWith(JMockit.class)
public class UserAlertsServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected UserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    private UserPersistenceService userPersistenceService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UserConverter userConverter;

    @Injectable
    private MultiDomainAlertConfigurationService alertsConfiguration;

    @Injectable
    private EventService eventService;

    @Injectable
    protected UserDomainService userDomainService;

    @Injectable
    protected DomainService domainService;

    @Tested
    private UserAlertsServiceImpl userAlertsService;

    @Test
    public void testSendPasswordExpiredAlerts(@Mocked UserDaoBase dao) {
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
        new Expectations() {{
            userAlertsService.getAlertTypeForPasswordExpired();
            result = AlertType.PASSWORD_EXPIRED;
            alertsConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_EXPIRED).isActive();
            result = true;
            alertsConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_EXPIRED).getEventDelay();
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
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_EXPIRED, (User) any, maxPasswordAge);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordImminentExpirationAlerts(@Mocked UserDaoBase dao) {
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
        new Expectations() {{
            userAlertsService.getAlertTypeForPasswordImminentExpiration();
            result = AlertType.PASSWORD_IMMINENT_EXPIRATION;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE;
            alertsConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION).isActive();
            result = true;
            alertsConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION).getEventDelay();
            result = howManyDaysBeforeExpirationToGenerateAlerts;
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
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_IMMINENT_EXPIRATION, (UserEntityBase) any, maxPasswordAge);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordAlerts() {

        userAlertsService.triggerPasswordExpirationEvents();

        new VerificationsInOrder() {{
            userAlertsService.triggerImminentExpirationEvents(false);
            times = 1;
        }};
        new VerificationsInOrder() {{
            userAlertsService.triggerExpiredEvents(false);
            times = 1;
        }};
    }


    @Test
    public void doNotSendPasswordExpiredEventsIfPasswordExpirationIsDisabled() {
        new Expectations() {{
            alertsConfiguration.getRepetitiveAlertConfiguration((AlertType) any);
            result = new RepetitiveAlertModuleConfiguration(AlertType.PLUGIN_PASSWORD_EXPIRED, 100, 20,  AlertLevel.LOW, "alert subject");

            userAlertsService.getMaximumDefaultPasswordAgeProperty();
            result = "propertyNameToCheck";

            domibusPropertyProvider.getIntegerProperty("propertyNameToCheck");
            result = 0;
        }};

        userAlertsService.triggerExpiredEvents(true);

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
        new Expectations() {{
            alertsConfiguration.getAccountDisabledConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerDisabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueAccountDisabledEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date)any);
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
        new Expectations() {{
            alertsConfiguration.getAccountEnabledConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerEnabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueAccountEnabledEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date)any);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        LoginFailureModuleConfiguration conf = new LoginFailureModuleConfiguration(AlertType.USER_LOGIN_FAILURE, AlertLevel.MEDIUM, "");
        new Expectations() {{
            userAlertsService.getLoginFailureConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.BAD_CREDENTIALS);

        new VerificationsInOrder() {{
            eventService.enqueueLoginFailureEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date)any, false);
            times = 1;
        }};
    }

}
