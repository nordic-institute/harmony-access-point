package eu.domibus.plugin.webService.property.listeners;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
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
    Endpoint backendInterfaceEndpoint;

    @Tested
    SchemaValidationEnabledChangeListener listener = new SchemaValidationEnabledChangeListener(backendInterfaceEndpoint);

    @Test
    public void handlesProperty_true() {
        boolean result = listener.handlesProperty("wsplugin.schema.validation.enabled");
        Assert.assertEquals(true, result);
    }

    @Test
    public void handlesProperty_false() {
        boolean result = listener.handlesProperty("wsplugin.mtom.enabled");
        Assert.assertEquals(false, result);
    }

    @Test
    public void propertyValueChanged() {
        HashMap<String, Object> propBag = new HashMap<>();
        new Expectations() {{
            backendInterfaceEndpoint.getProperties();
            result = propBag;
        }};

        listener.propertyValueChanged("default", "wsplugin.schema.validation.enabled", "true");

        Assert.assertEquals("true", propBag.get("schema-validation-enabled"));
    }
}