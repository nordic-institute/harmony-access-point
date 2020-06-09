package eu.domibus.core.alerts.configuration.account.disabled;

import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class ConsoleAccountDisabledConfigurationManagerTest  {

    @Tested
    ConsoleAccountDisabledConfigurationManager configurationManager;

    @Injectable
    private ConsoleAccountDisabledConfigurationReader reader;

    @Injectable
    private ConfigurationLoader<AccountDisabledModuleConfiguration> loader;

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
    public void getConfiguration(@Mocked AccountDisabledModuleConfiguration configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<AccountDisabledModuleConfiguration>) any);
            result = configuration;
        }};
        AccountDisabledModuleConfiguration res = configurationManager.getConfiguration();
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