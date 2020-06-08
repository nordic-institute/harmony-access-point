package eu.domibus.core.alerts.configuration.account.enabled;

import eu.domibus.core.alerts.configuration.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class PluginAccountEnabledConfigurationManagerTest extends TestCase {

    @Tested
    PluginAccountEnabledConfigurationManager configurationManager;

    @Injectable
    private PluginAccountEnabledConfigurationReader reader;

    @Injectable
    private ConfigurationLoader<AlertModuleConfigurationBase> loader;

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
    public void getConfiguration(@Mocked AlertModuleConfigurationBase configuration) {
        new Expectations() {{
            loader.getConfiguration((ConfigurationReader<AlertModuleConfigurationBase>) any);
            result = configuration;
        }};
        AlertModuleConfigurationBase res = configurationManager.getConfiguration();
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