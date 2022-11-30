package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.user.UserEntityBase;
import eu.domibus.core.alerts.configuration.account.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.configuration.generic.RepetitiveAlertConfiguration;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.user.UserDaoBase;
import eu.domibus.core.user.UserLoginErrorReason;
import eu.domibus.core.user.UserPersistenceService;
import eu.domibus.core.user.ui.User;
import eu.domibus.core.user.ui.UserDao;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
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
    private EventService eventService;

    @Injectable
    protected UserDomainService userDomainService;

    @Injectable
    protected DomainService domainService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Tested
    private UserAlertsServiceImpl userAlertsService;

    @Test
    public void testSendPasswordExpiredAlerts(@Injectable UserDaoBase<UserEntityBase> dao,
                                              @Injectable RepetitiveAlertConfiguration alertConfiguration) {
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
            alertConfiguration.getDelay();
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
    public void testSendPasswordImminentExpirationAlerts(@Injectable UserDaoBase<UserEntityBase> dao,
                                                         @Injectable RepetitiveAlertConfiguration alertConfiguration) {
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
            alertConfiguration.getDelay();
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
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_IMMINENT_EXPIRATION, (UserEntityBase) any, maxPasswordAge);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordImminentExpirationAlerts_inactive(
            @Injectable RepetitiveAlertConfiguration alertConfiguration) {
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
    public void doNotSendPasswordExpiredEventsIfPasswordExpirationIsDisabled(@Injectable RepetitiveAlertConfiguration alertConfiguration) {
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
    public void triggerDisabledEventTest(@Injectable AccountDisabledModuleConfiguration configuration) {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        new Expectations(userAlertsService) {{
            configuration.isActive();
            result = true;
            userAlertsService.getAccountDisabledConfiguration();
            result = configuration;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerDisabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueEvent(EventType.USER_ACCOUNT_DISABLED, UserEntityBase.Type.CONSOLE.getCode() + "/" + user1.getUserName(), (EventProperties) any);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest_BAD_CREDENTIALS(
            @Injectable AlertModuleConfigurationBase LoginFailureModuleConfiguration) {

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
            eventService.enqueueEvent(EventType.USER_LOGIN_FAILURE, UserEntityBase.Type.CONSOLE.getCode() + "/" + "user1", (EventProperties) any);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_inactive(
            @Injectable AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Injectable AlertModuleConfigurationBase LoginFailureModuleConfiguration) {

        new Expectations(userAlertsService) {{
            LoginFailureModuleConfiguration.isActive();
            result = true;

            userAlertsService.getLoginFailureConfiguration();
            result = LoginFailureModuleConfiguration;

            userAlertsService.getAccountDisabledConfiguration();
            result = accountDisabledConfiguration;

            accountDisabledConfiguration.isActive();
            result = false;

            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents(UserEntityBase.Type.CONSOLE.getCode() + "/" + "user1", UserLoginErrorReason.SUSPENDED);

        new FullVerifications() {
        };
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_active_eachLogin(
            @Injectable AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Injectable AlertModuleConfigurationBase LoginFailureModuleConfiguration) {

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
            eventService.enqueueEvent(EventType.USER_ACCOUNT_DISABLED, UserEntityBase.Type.CONSOLE.getCode() + "/" + "user1", (EventProperties) any);
        }};
    }

    @Test
    public void triggerLoginEventsTest_SUSPENDED_active_notEachLogin(
            @Injectable AccountDisabledModuleConfiguration accountDisabledConfiguration,
            @Injectable AlertModuleConfigurationBase LoginFailureModuleConfiguration) {

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
            eventService.enqueueEvent(EventType.USER_LOGIN_FAILURE, UserEntityBase.Type.CONSOLE.getCode() + "/" + "user1", (EventProperties) any);
        }};
    }

}
