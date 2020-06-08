package eu.domibus.core.alerts.configuration.login;

import eu.domibus.core.alerts.configuration.account.disabled.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.configuration.account.disabled.ConsoleAccountDisabledConfigurationManager;
import eu.domibus.core.alerts.configuration.account.disabled.ConsoleAccountDisabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class ConsoleLoginFailConfigurationManagerTest {

    @Tested
    ConsoleLoginFailConfigurationManager configurationManager;

    @Injectable
    private ConsoleLoginFailConfigurationReader reader;

    @Injectable
    private ConfigurationLoader<LoginFailureModuleConfiguration> loader;

    @Test
    public void getAlertType(@Mocked AlertType alertType) {
        new Expectations() {{
            reader.getAlertType();
            result = alertType;
        }};

        AlertType res = configurationManager.getAlertType();
        assertEquals(res, alertType);
    }

    @Test
    public void getConfiguration(@Mocked LoginFailureModuleConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<LoginFailureModuleConfiguration>) any);
            result = configuration;
        }};
        LoginFailureModuleConfiguration res = configurationManager.getConfiguration();
        assertEquals(res, configuration);
    }

    @Test
    public void reset() {
        configurationManager.reset();
        new Verifications() {{
            loader.resetConfiguration();
        }};
    }
}