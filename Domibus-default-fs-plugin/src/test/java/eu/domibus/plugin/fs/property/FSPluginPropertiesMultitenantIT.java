package eu.domibus.plugin.fs.property;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.property.DefaultDomibusConfigurationService;
import eu.domibus.core.property.PropertyProviderHelper;
import eu.domibus.core.property.PropertyRetrieveManager;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.test.AbstractIT;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.LOCATION;

/**
 * @author Catalin Enache
 * @since 5.0
 */
public class FSPluginPropertiesMultitenantIT extends AbstractIT {

    private static final String DOMAIN1 = "domain1";
    private static final String DOMAIN2 = "domain2";
    private static final String NONEXISTENT_DOMAIN = "NONEXISTENT_DOMAIN";

    private static final String DEFAULT_LOCATION = "/tmp/fs_plugin_data";
    private static final String DOMAIN1_LOCATION = "/tmp/fs_plugin_data/domain1";

    @Autowired
    FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Autowired
    FSPluginProperties fsPluginProperties;
    @Autowired
    PropertyProviderHelper propertyProviderHelper;

    @Autowired
    DomainExtService domainExtService;
    @Autowired
    DomainService domainService;
    @Autowired
    PropertyRetrieveManager propertyRetrieveManager;
    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;
    @Autowired
    DomibusCacheService domibusCacheService;
    @Autowired
    DefaultDomibusConfigurationService defaultDomibusConfigurationService;

    @Configuration
    @PropertySource(value = "file:${domibus.config.location}/dataset/fsplugin/fs-plugin.properties")
    // as this test file is added last to property sources and
    // contrary to implemented properties mechanism which allow to override values from the default file
    // fs-plugin-default.properties takes precedence here so we will test both files values
    static class ContextConfiguration {
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testGetLocation_Domain1() {
        Assert.assertEquals(DOMAIN1_LOCATION, fsPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testGetLocation_NonExistentDomain() {
        try {
            fsPluginProperties.getLocation(NONEXISTENT_DOMAIN);
            Assert.fail("Exception expected");
        } catch (DomibusPropertyException e) {
            Assert.assertTrue(e.getMessage().contains("cannot be retrieved without a domain"));
        }
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testGetUser() {
        Assert.assertEquals("user1", fsPluginProperties.getUser(DOMAIN1));
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testGetPassword() {
        Assert.assertEquals("pass1", fsPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void testGetUser_NotSecured() {
        Assert.assertEquals("", fsPluginProperties.getUser(DOMAIN2));
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testGetPayloadId_Domain() {
        Assert.assertEquals("cid:attachment", fsPluginProperties.getPayloadId(DOMAIN1));
    }

    @Test
    public void testGetPayloadId_DomainMissing() {
        Assert.assertEquals("cid:message", fsPluginProperties.getPayloadId(DOMAIN2));
    }


    @Test
    public void testGetPassword_NotSecured() {
        Assert.assertEquals("", fsPluginProperties.getPassword(DOMAIN2));
    }

    @Test
    @Ignore("EDELIVERY-8812: FSPluginPropertiesMultitenantIT - fix tests")
    public void testKnownPropertyValue_multiTenancy() {
        final String oldPropertyValue1 = "/tmp/fs_plugin_data/DOMAIN1";
        final String oldPropertyValue2 = "/tmp/fs_plugin_data";
        final String newPropertyValue1 = "new-property-value1";
        final String newPropertyValue2 = "new-property-value2";

        // test get value
        String value1 = fsPluginProperties.getKnownPropertyValue(DOMAIN1, LOCATION);
        String value2 = fsPluginProperties.getKnownPropertyValue(DOMAIN2, LOCATION);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fsPluginProperties.setKnownPropertyValue(DOMAIN1, LOCATION, newPropertyValue1, true);
        fsPluginProperties.setKnownPropertyValue(DOMAIN2, LOCATION, newPropertyValue2, true);

        value1 = fsPluginProperties.getKnownPropertyValue(DOMAIN1, LOCATION);
        value2 = fsPluginProperties.getKnownPropertyValue(DOMAIN2, LOCATION);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fsPluginProperties.setKnownPropertyValue(DOMAIN1, LOCATION, oldPropertyValue1, true);
        fsPluginProperties.setKnownPropertyValue(DOMAIN2, LOCATION, oldPropertyValue2, true);
    }

}
