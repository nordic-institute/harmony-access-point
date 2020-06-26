package eu.domibus.plugin.webService.property;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import eu.domibus.plugin.webService.property.listeners.MtomEnabledChangeListener;
import eu.domibus.plugin.webService.property.listeners.SchemaValidationEnabledChangeListener;
import junit.framework.TestCase;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.Endpoint;
import javax.xml.ws.soap.SOAPBinding;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class WSPluginPropertyManagerTest extends TestCase {

    @Tested
    SchemaValidationEnabledChangeListener schemaValidationEnabledChangeListener;

    @Tested
    MtomEnabledChangeListener mtomEnabledChangeListener;

    @Tested
    @Injectable
    WSPluginPropertyManager wsPluginPropertyManager;

    @Injectable
    private Endpoint backendInterfaceEndpoint;

    @Injectable
    DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    DomainExtService domainExtService;

    @Test
    public void testPropertyChangeListeners(@Mocked HashMap<String, Object> propBag, @Mocked SOAPBinding soapBinding) {
        PluginPropertyChangeListener[] listeners = new PluginPropertyChangeListener[]{
                schemaValidationEnabledChangeListener,
                mtomEnabledChangeListener,
        };

        new Expectations() {{
            backendInterfaceEndpoint.getProperties();
            result = propBag;
            backendInterfaceEndpoint.getBinding();
            result = soapBinding;
        }};

        Map<String, DomibusPropertyMetadataDTO> properties = wsPluginPropertyManager.getKnownProperties();

        for (String propertyName : properties.keySet()) {
            if (wsPluginPropertyManager.hasKnownProperty(propertyName)) {
                for (PluginPropertyChangeListener listener : listeners) {
                    if (listener.handlesProperty(propertyName)) {
                        String testValue = testPropertyValue(propertyName);
                        listener.propertyValueChanged("default", propertyName, testValue);
                    }
                }
            }
        }

        assertTrue(propBag.get("schema-validation-enabled").equals("true"));

        new Verifications() {{
            ((SOAPBinding) backendInterfaceEndpoint.getBinding()).setMTOMEnabled(true);
        }};
    }

    private String testPropertyValue(String propertyName) {
        return "true";
    }
}