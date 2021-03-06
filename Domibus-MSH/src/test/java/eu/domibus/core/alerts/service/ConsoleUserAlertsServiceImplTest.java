package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.account.disabled.console.ConsoleAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.enabled.console.ConsoleAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.login.console.ConsoleLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.password.expired.console.ConsolePasswordExpiredAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.imminent.console.ConsolePasswordImminentExpirationAlertConfigurationManager;
import eu.domibus.core.user.ui.UserDao;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import mockit.Injectable;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class ConsoleUserAlertsServiceImplTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserDao userDao;

    @Injectable
    ConsoleAccountDisabledConfigurationManager consoleAccountDisabledConfigurationManager;

    @Injectable
    private EventService eventService;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Injectable
    ConsoleLoginFailConfigurationManager consoleLoginFailConfigurationManager;

    @Injectable
    ConsoleAccountEnabledConfigurationManager consoleAccountEnabledConfigurationManager;

    @Injectable
    ConsolePasswordExpiredAlertConfigurationManager consolePasswordExpiredAlertConfigurationManager;

    @Injectable
    ConsolePasswordImminentExpirationAlertConfigurationManager consolePasswordImminentExpirationAlertConfigurationManager;

    @Tested
    private ConsoleUserAlertsServiceImpl userAlertsService;

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumDefaultPasswordAgeProperty();

        Assert.assertEquals(ConsoleUserAlertsServiceImpl.MAXIMUM_DEFAULT_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumPasswordAgeProperty();

        Assert.assertEquals(ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetAlertTypeForPasswordImminentExpiration() {
        AlertType val = userAlertsService.getAlertTypeForPasswordImminentExpiration();

        Assert.assertEquals(AlertType.PASSWORD_IMMINENT_EXPIRATION, val);
    }

    @Test
    public void testGetAlertTypeForPasswordExpired() {
        AlertType val = userAlertsService.getAlertTypeForPasswordExpired();

        Assert.assertEquals(AlertType.PASSWORD_EXPIRED, val);
    }

    @Test
    public void testGetEventTypeForPasswordExpired() {
        EventType val = userAlertsService.getEventTypeForPasswordExpired();

        Assert.assertEquals(EventType.PASSWORD_EXPIRED, val);
    }

    @Test
    public void testGetUserType() {
        UserEntityBase.Type val = userAlertsService.getUserType();

        Assert.assertEquals( UserEntityBase.Type.CONSOLE, val);
    }

    @Test
    public void testGetAccountDisabledConfiguration() {
        AccountDisabledModuleConfiguration val = userAlertsService.getAccountDisabledConfiguration();

        new VerificationsInOrder() {{
            consoleAccountDisabledConfigurationManager.getConfiguration();
            times = 1;
        }};
    }
}
