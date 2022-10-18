package eu.domibus.plugin.fs.property;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.property.DefaultDomibusConfigurationService;
import eu.domibus.core.property.PropertyProviderHelper;
import eu.domibus.core.property.PropertyRetrieveManager;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.test.AbstractIT;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl.LOCATION;
import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * @author Ion Perpegel
 * @since 5.1
 */
public class FSPluginPropertiesWriteIT extends AbstractIT {

    private static final String NONEXISTENT_DOMAIN = "NONEXISTENT_DOMAIN";

    private static final String DEFAULT_LOCATION = "/tmp/fs_plugin_data";

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
    @PropertySource(value = "file:${domibus.config.location}/plugins/config/fs-plugin.properties")
    static class ContextConfiguration {
    }


    @Test
    public void testKnownPropertyValue_singleTenancy() {
        final String domainDefault = "default";
        final String propertyName1 = LOCATION;
        final String propertyName2 = FSPluginPropertiesMetadataManagerImpl.SENT_ACTION;
        final String oldPropertyValue1 = "/tmp/fs_plugin_data";
        final String oldPropertyValue2 = "delete";
        final String newPropertyValue1 = "new-property-value1";
        final String newPropertyValue2 = "new-property-value2";

        // test get value
        String value1 = fsPluginProperties.getKnownPropertyValue(domainDefault, propertyName1);
        String value2 = fsPluginProperties.getKnownPropertyValue(domainDefault, propertyName2);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fsPluginProperties.setKnownPropertyValue(domainDefault, propertyName1, newPropertyValue1, false);
        fsPluginProperties.setKnownPropertyValue(domainDefault, propertyName2, newPropertyValue2, true);

        value1 = fsPluginProperties.getKnownPropertyValue(domainDefault, propertyName1);
        value2 = fsPluginProperties.getKnownPropertyValue(domainDefault, propertyName2);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fsPluginProperties.setKnownPropertyValue(domainDefault, propertyName1, oldPropertyValue1, false);
        fsPluginProperties.setKnownPropertyValue(domainDefault, propertyName2, oldPropertyValue2, true);
    }


}
