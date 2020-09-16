package eu.domibus.core.alerts.configuration;

import eu.domibus.core.alerts.configuration.account.enabled.console.ConsoleAccountEnabledConfigurationReader;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.ConfigurationLoader;
import eu.domibus.core.alerts.service.ConfigurationReader;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ReaderAlertConfigurationManagerTest extends TestCase {

    @Tested
    ReaderAlertConfigurationManager<AlertModuleConfigurationBase, ConsoleAccountEnabledConfigurationReader> readerAlertConfigurationManager;

    @Injectable
    private ConfigurationLoader<AlertModuleConfigurationBase> loader;

    @Test
    public void testGetAlertType(@Mocked ConsoleAccountEnabledConfigurationReader alertConfigurationReader) {
        new Expectations(readerAlertConfigurationManager) {{
            readerAlertConfigurationManager.getReader();
            result = alertConfigurationReader;
            alertConfigurationReader.getAlertType();
            result = AlertType.CERT_EXPIRED;
        }};

        AlertType res = readerAlertConfigurationManager.getAlertType();

        assertEquals(AlertType.CERT_EXPIRED, res);
    }

    @Test
    public void testGetConfiguration(@Mocked ConsoleAccountEnabledConfigurationReader alertConfigurationReader,
                                     @Mocked AlertModuleConfigurationBase alertModuleConfigurationBase) {
        new Expectations(readerAlertConfigurationManager) {{
            readerAlertConfigurationManager.getReader();
            result = alertConfigurationReader;
            loader.getConfiguration((ConfigurationReader<AlertModuleConfigurationBase>) any);
            result = alertModuleConfigurationBase;
        }};

        AlertModuleConfigurationBase res = readerAlertConfigurationManager.getConfiguration();

        assertEquals(alertModuleConfigurationBase, res);
    }

    @Test
    public void testReset(@Mocked ConsoleAccountEnabledConfigurationReader alertConfigurationReader) {
        new Expectations() {{
        }};

        readerAlertConfigurationManager.reset();

        new FullVerifications() {{
            loader.resetConfiguration();
        }};
    }
}