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
import org.junit.Assert;
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
    @Injectable
    WSPluginPropertyManager wsPluginPropertyManager;

    @Mocked
    SchemaValidationEnabledChangeListener schemaValidationEnabledChangeListener;

    @Mocked
    MtomEnabledChangeListener mtomEnabledChangeListener;

    @Injectable
    DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    DomainExtService domainExtService;

    @Test
    public void getKnownProperties() {
        Map<String, DomibusPropertyMetadataDTO> properties = wsPluginPropertyManager.getKnownProperties();
        Assert.assertTrue(properties.containsKey(WSPluginPropertyManager.SCHEMA_VALIDATION_ENABLED_PROPERTY));
        Assert.assertTrue(properties.containsKey(WSPluginPropertyManager.MTOM_ENABLED_PROPERTY));
        Assert.assertTrue(properties.containsKey(WSPluginPropertyManager.PROP_LIST_PENDING_MESSAGES_MAXCOUNT));
        Assert.assertFalse(properties.containsKey("unknown.property"));
    }
}