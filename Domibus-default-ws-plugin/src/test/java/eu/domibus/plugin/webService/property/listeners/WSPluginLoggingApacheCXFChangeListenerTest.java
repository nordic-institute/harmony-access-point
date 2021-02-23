package eu.domibus.plugin.webService.property.listeners;

import eu.domibus.plugin.webService.logging.WSPluginLoggingEventSender;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.plugin.webService.configuration.WSPluginConfiguration.*;

/**
 * @author François Gautier
 * @since 4.2
 */
@RunWith(JMockit.class)
public class WSPluginLoggingApacheCXFChangeListenerTest {

    @Mocked
    private LoggingFeature loggingFeature;

    @Mocked
    private WSPluginLoggingEventSender loggingSender;

    protected WSPluginLoggingApacheCXFChangeListener listener;

    @Before
    public void setUp() {
        listener = new WSPluginLoggingApacheCXFChangeListener(loggingFeature, loggingSender);
    }

    @Test
    public void handlesProperty_true() {
        Assert.assertTrue(listener.handlesProperty(DOMIBUS_LOGGING_METADATA_PRINT));
    }

    @Test
    public void handlesProperty_false() {
        Assert.assertFalse(listener.handlesProperty("I hate pickles"));
    }

    @Test
    public void propertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_METADATA_PRINT, "true");
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_CXF_LIMIT, "20000");
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_PAYLOAD_PRINT, "false");

        new FullVerifications() {{
            loggingSender.setPrintMetadata(true);
            loggingFeature.setLimit(anyInt);
            loggingSender.setPrintPayload(false);
        }};
    }

    /**
     * Should not happen because of property validation. But default behaviour: set false.
     */
    @Test
    public void propertyValueChanged_invalid() {
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_METADATA_PRINT, "nope");

        new FullVerifications() {{
            loggingSender.setPrintMetadata(false);
            times = 1;
        }};
    }
}