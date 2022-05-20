package eu.domibus.plugin.ws.property.listeners;

import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.Endpoint;
import java.util.HashMap;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class SchemaValidationEnabledChangeListenerTest {

    @Injectable
    private Endpoint backendInterfaceEndpoint;

    @Injectable
    private Endpoint backendInterfaceEndpointDeprecated;

    private SchemaValidationEnabledChangeListener listener;

    @Before
    public void setUp()  {
        listener = new SchemaValidationEnabledChangeListener(backendInterfaceEndpoint, backendInterfaceEndpointDeprecated);
    }

    @Test
    public void handlesProperty_true() {
        boolean result = listener.handlesProperty("wsplugin.schema.validation.enabled");
        Assert.assertTrue(result);
    }

    @Test
    public void handlesProperty_false() {
        boolean result = listener.handlesProperty("wsplugin.mtom.enabled");
        Assert.assertFalse(result);
    }

    @Test
    public void propertyValueChanged() {
        HashMap<String, Object> propBag = new HashMap<>();
        HashMap<String, Object> propBagDeprecated = new HashMap<>();
        new Expectations() {{
            backendInterfaceEndpoint.getProperties();
            result = propBag;
            backendInterfaceEndpointDeprecated.getProperties();
            result = propBagDeprecated;
        }};

        listener.propertyValueChanged("default", "wsplugin.schema.validation.enabled", "true");

        Assert.assertEquals("true", propBag.get("schema-validation-enabled"));
        Assert.assertEquals("true", propBagDeprecated.get("schema-validation-enabled"));

        new FullVerifications(){};
    }
}