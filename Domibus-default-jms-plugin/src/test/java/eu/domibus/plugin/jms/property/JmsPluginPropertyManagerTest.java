package eu.domibus.plugin.jms.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class JmsPluginPropertyManagerTest {

    @Tested
    JmsPluginPropertyManager jmsPluginPropertyManager;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    protected DomainExtService domainExtService;


    private final String jmsProperty = "jmsplugin.fromPartyId";
    private final String testValue = "new-value";
    private final DomainDTO testDomain = new DomainDTO("default", "default");

    @Test
    public void setKnownPropertyValue() {
        new Expectations() {{
            domibusPropertyExtService.getProperty(jmsProperty);
            returns("old-value", testValue);
        }};

        final String oldValue = jmsPluginPropertyManager.getKnownPropertyValue(jmsProperty);
        jmsPluginPropertyManager.setKnownPropertyValue(jmsProperty, testValue);
        final String newValue = jmsPluginPropertyManager.getKnownPropertyValue(jmsProperty);

        Assert.assertTrue(oldValue != newValue);
        Assert.assertEquals(testValue, newValue);
    }

    @Test
    public void getKnownProperties() {
        Map<String, DomibusPropertyMetadataDTO> properties = jmsPluginPropertyManager.getKnownProperties();
        Assert.assertTrue(properties.containsKey(jmsProperty));
    }

    @Test
    public void hasKnownProperty() {
        boolean hasProperty = jmsPluginPropertyManager.hasKnownProperty(jmsProperty);
        Assert.assertTrue(hasProperty);
    }

    @Test
    public void testUnknownProperty() {
        String unknownPropertyName = "jmsplugin.unknown.property";

        try {
            jmsPluginPropertyManager.getKnownPropertyValue(unknownPropertyName);
            Assert.fail("Expected exception not thrown");
        } catch (DomibusPropertyExtException ex) {
            Assert.assertTrue(ex.getMessage().contains(unknownPropertyName));
        }

        try {
            jmsPluginPropertyManager.setKnownPropertyValue(unknownPropertyName, testValue);
            Assert.fail("Expected exception not thrown");
        } catch (DomibusPropertyExtException ex) {
            Assert.assertTrue(ex.getMessage().contains(unknownPropertyName));
        }
    }
}