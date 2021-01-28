package eu.domibus.plugin.ws.property.listeners;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MtomEnabledChangeListenerTest {

    @Mocked
    private Endpoint backendInterfaceEndpoint;

    private MtomEnabledChangeListener listener;

    @Before
    public void setUp() {
        listener = new MtomEnabledChangeListener(backendInterfaceEndpoint);
    }

    @Test
    public void handlesProperty_false() {
        boolean result = listener.handlesProperty("wsplugin.schema.validation.enabled");
        Assert.assertFalse(result);
    }

    @Test
    public void handlesProperty_true() {
        boolean result = listener.handlesProperty("wsplugin.mtom.enabled");
        Assert.assertTrue(result);
    }

    @Test
    public void propertyValueChanged(@Mocked SOAPBinding soapBinding) {
        new Expectations() {{
            backendInterfaceEndpoint.getBinding();
            result = soapBinding;
        }};

        listener.propertyValueChanged("default", "wsplugin.mtom.enabled", "true");
        new FullVerifications() {{
            soapBinding.setMTOMEnabled(true);
        }};

    }
}