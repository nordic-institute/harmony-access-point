package eu.domibus.core.property.listeners;

import eu.domibus.core.logging.cxf.DomibusLoggingEventSender;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_LOGGING_PAYLOAD_PRINT;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class DomibusLoggingPayloadPrintChangeListenerTest {

    @Mocked
    private DomibusLoggingEventSender loggingSender;

    protected DomibusLoggingPayloadPrintChangeListener listener;

    @Before
    public void setUp() {
        listener = new DomibusLoggingPayloadPrintChangeListener(loggingSender);
    }

    @Test
    public void handlesProperty_true() {
        Assert.assertTrue(listener.handlesProperty(DOMIBUS_LOGGING_PAYLOAD_PRINT));
    }

    @Test
    public void handlesProperty_false() {
        Assert.assertFalse(listener.handlesProperty("OTHER"));
    }

    @Test
    public void propertyValueChanged() {
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_PAYLOAD_PRINT, "true");

        new FullVerifications() {{
            loggingSender.setPrintPayload(true);
            times = 1;
        }};
    }

    /**
     * Should not happen because of property validation. But default behaviour: set false.
     */
    @Test
    public void propertyValueChanged_invalid() {
        listener.propertyValueChanged("default", DOMIBUS_LOGGING_PAYLOAD_PRINT, "nope");

        new FullVerifications() {{
            loggingSender.setPrintPayload(false);
            times = 1;
        }};
    }
}