package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.account.disabled.plugin.PluginAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.enabled.plugin.PluginAccountEnabledConfigurationManager;
import eu.domibus.core.alerts.configuration.login.plugin.PluginLoginFailConfigurationManager;
import eu.domibus.core.alerts.configuration.password.expired.PluginPasswordExpiredAlertConfigurationManager;
import eu.domibus.core.alerts.configuration.password.imminent.PluginPasswordImminentExpirationAlertConfigurationManager;
import eu.domibus.core.user.UserEntityBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.user.plugin.AuthenticationDAO;
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
public class PluginUserAlertsServiceImplTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AuthenticationDAO userDao;

    @Injectable
    private PluginAccountDisabledConfigurationManager pluginAccountDisabledConfigurationManager;

    @Injectable
    private EventService eventService;

    @Injectable
    PluginPasswordExpiredAlertConfigurationManager pluginPasswordExpiredAlertConfigurationManager;

    @Injectable
    PluginAccountEnabledConfigurationManager pluginAccountEnabledConfigurationManager;

    @Injectable
    PluginLoginFailConfigurationManager pluginLoginFailConfigurationManager;

    @Injectable
    PluginPasswordImminentExpirationAlertConfigurationManager pluginPasswordImminentExpirationAlertConfigurationManager;

    @Tested
    private PluginUserAlertsServiceImpl userAlertsService;

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumDefaultPasswordAgeProperty();

        Assert.assertEquals(PluginUserAlertsServiceImpl.MAXIMUM_DEFAULT_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumPasswordAgeProperty();

        Assert.assertEquals(PluginUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetAlertTypeForPasswordImminentExpiration() {
        AlertType val = userAlertsService.getAlertTypeForPasswordImminentExpiration();

        Assert.assertEquals(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, val);
    }

    @Test
    public void testGetAlertTypeForPasswordExpired() {
        AlertType val = userAlertsService.getAlertTypeForPasswordExpired();

        Assert.assertEquals(AlertType.PLUGIN_PASSWORD_EXPIRED, val);
    }

    @Test
    public void testGetEventTypeForPasswordExpired() {
        EventType val = userAlertsService.getEventTypeForPasswordExpired();

        Assert.assertEquals(EventType.PLUGIN_PASSWORD_EXPIRED, val);
    }

    @Test
    public void testGetUserType() {
        UserEntityBase.Type val = userAlertsService.getUserType();

        Assert.assertEquals(UserEntityBase.Type.PLUGIN, val);
    }

    @Test
    public void testGetAccountDisabledConfiguration() {
        AccountDisabledModuleConfiguration val = userAlertsService.getAccountDisabledConfiguration();

        new VerificationsInOrder() {{
            pluginAccountDisabledConfigurationManager.getConfiguration();
            times = 1;
        }};
    }
}
