package eu.domibus.core.alerts.configuration.password.imminent;

import eu.domibus.core.alerts.configuration.password.PasswordExpirationAlertModuleConfiguration;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class ConsolePasswordImminentExpirationAlertConfigurationManagerTest {
    @Tested
    ConsolePasswordImminentExpirationAlertConfigurationManager configurationManager;

    @Injectable
    private ConsolePasswordImminentExpirationAlertConfigurationReader reader;

    @Injectable
    private ConfigurationLoader<PasswordExpirationAlertModuleConfiguration> loader;

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
    public void getConfiguration(@Mocked PasswordExpirationAlertModuleConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<PasswordExpirationAlertModuleConfiguration>) any);
            result = configuration;
        }};
        PasswordExpirationAlertModuleConfiguration res = configurationManager.getConfiguration();
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