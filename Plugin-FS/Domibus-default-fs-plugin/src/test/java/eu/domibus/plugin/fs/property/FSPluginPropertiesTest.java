package eu.domibus.plugin.fs.property;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.*;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSPluginPropertiesTest {

    @Injectable
    protected PasswordEncryptionExtService pluginPasswordEncryptionService;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    DomainContextExtService domainContextExtService;

    @Tested
    @Injectable
    protected FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Injectable
    DomibusPropertyExtService domibusPropertyExtService;

    @Tested
    FSPluginProperties fsPluginProperties;


    @Test
    public void testHasKnownProperty() {
        final String propertyName = "fsplugin.messages.location";

        final Boolean isKnownProperty = fsPluginPropertiesMetadataManager.hasKnownProperty(propertyName);
        Assert.assertEquals(true, isKnownProperty);

        final Boolean isKnownFSProperty = fsPluginProperties.hasKnownProperty(propertyName);
        Assert.assertEquals(true, isKnownFSProperty);
    }


    @Test
    public void testUnknownProperty() {
        final String propertyName = "fsplugin.messages.location.unknown";


        final Boolean isKnownFSProperty = fsPluginProperties.hasKnownProperty(propertyName);
        Assert.assertEquals(false, isKnownFSProperty);
 
        String value = fsPluginProperties.getKnownPropertyValue("default", propertyName);
        Assert.assertTrue(StringUtils.isBlank(value));
    }

}
