package eu.domibus.plugin.fs.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSPluginPropertiesTest {

    @Injectable("fsPluginProperties")
    Properties properties;

    @Injectable
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    PluginPropertyChangeNotifier pluginPropertyChangeNotifier;

    @Injectable
    DomainContextExtService domainContextExtService;

    @Tested
    @Injectable
    protected FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Tested
    FSPluginProperties fsPluginProperties;

    @Test
    public void testReadDomains() {
        Set<String> domains = new HashSet<>();
        domains.add("fsplugin.domains.domain1");
        domains.add("fsplugin.domains.domain2");
        domains.add("fsplugin.domains.domain3");

        new Expectations(Collections.class) {{
            properties.stringPropertyNames();
            result = domains;

            Collections.sort((List) any, (Comparator) any);
        }};

        final List<String> domainsList = fsPluginProperties.readDomains();
        Assert.assertEquals(4, domainsList.size());
        Assert.assertTrue(domainsList.contains(FSSendMessagesService.DEFAULT_DOMAIN));
    }

    @Test
    public void testHasKnownProperty() {
        final String propertyName = "fsplugin.messages.location";

        new Expectations(Collections.class) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
        }};

        final Boolean isKnownProperty = fsPluginPropertiesMetadataManager.hasKnownProperty(propertyName);
        Assert.assertEquals(false, isKnownProperty);

        final Boolean isKnownFSProperty = fsPluginProperties.hasKnownProperty(propertyName);
        Assert.assertEquals(true, isKnownFSProperty);
    }


    @Test
    public void testUnknownProperty() {
        final String propertyName = "fsplugin.messages.location.unknown";

        new Expectations(Collections.class) {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
        }};

        final Boolean isKnownFSProperty = fsPluginProperties.hasKnownProperty(propertyName);
        Assert.assertEquals(false, isKnownFSProperty);

        try {
            fsPluginProperties.getKnownPropertyValue("default", propertyName);
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusPropertyExtException e) {
            assertEquals(true, e.getMessage().contains(propertyName));
        }

        try {
            fsPluginProperties.setKnownPropertyValue("default", propertyName, "testValue");
            Assert.fail("Expected exception was not raised!");
        } catch (DomibusPropertyExtException e) {
            assertEquals(true, e.getMessage().contains(propertyName));
        }

    }

}
