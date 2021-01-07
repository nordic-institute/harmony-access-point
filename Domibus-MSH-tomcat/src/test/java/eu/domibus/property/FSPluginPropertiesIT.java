package eu.domibus.property;

import eu.domibus.AbstractIT;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import eu.domibus.plugin.fs.property.FSPluginPropertiesMetadataManagerImpl;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;
import static org.mockito.Mockito.when;

/**
 * @author Catalin Enache
 * @since 5.0
 */
public class FSPluginPropertiesIT extends AbstractIT {

    private static final String DOMAIN1 = "DOMAIN1";
    private static final String DOMAIN2 = "DOMAIN2";
    private static final String NONEXISTENT_DOMAIN = "NONEXISTENT_DOMAIN";
    private static final String ODR = "ODR";
    private static final String BRIS = "BRIS";
    private static final String UNORDEREDA = "UNORDEREDA";
    private static final String UNORDEREDB = "UNORDEREDB";

    private static final String DEFAULT_LOCATION = "/tmp/fs_plugin_data";
    private static final String DOMAIN1_LOCATION = "/tmp/fs_plugin_data/DOMAIN1";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager;

    @Autowired
    FSPluginProperties fsPluginProperties;

    @Configuration
    @PropertySource(value = "file:${domibus.config.location}/dataset/fsplugin/fs-plugin.properties")
    static class ContextConfiguration {

        @Bean
        @Primary
        public DomibusConfigurationExtService domibusConfigurationExtService() {
            return Mockito.mock(DomibusConfigurationExtService.class);
        }
    }


    @Before
    public void setUp() throws Exception {
        Mockito.reset(domibusConfigurationExtService);
        when(domibusConfigurationExtService.isMultiTenantAware()).thenReturn(false);
    }

    @Test
    public void testGetLocation() {
        Assert.assertEquals(DEFAULT_LOCATION, fsPluginProperties.getLocation(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetLocation_Domain1() {
        Assert.assertEquals(DOMAIN1_LOCATION, fsPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void testGetLocation_NonExistentDomain() {
        try {
            String value = fsPluginProperties.getLocation(NONEXISTENT_DOMAIN);
            Assert.fail("Exception expected");
        } catch (DomibusPropertyExtException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown property"));
        }
    }

    @Test
    public void testGetSentAction() {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fsPluginProperties.getSentAction(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetSentPurgeWorkerCronExpression() {
        Assert.assertEquals("0 0/1 * * * ?", fsPluginProperties.getSentPurgeWorkerCronExpression(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetSentPurgeExpired() {
        Assert.assertEquals(Integer.valueOf(600), fsPluginProperties.getSentPurgeExpired(FSSendMessagesService.DEFAULT_DOMAIN));
    }

    @Test
    public void testGetFailedAction() {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fsPluginProperties.getFailedAction(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetFailedPurgeWorkerCronExpression() {
        Assert.assertEquals("0 0/1 * * * ?", fsPluginProperties.getFailedPurgeWorkerCronExpression(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetFailedPurgeExpired() {
        Assert.assertEquals(Integer.valueOf(600), fsPluginProperties.getFailedPurgeExpired(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetReceivedPurgeExpired() {
        Assert.assertEquals(Integer.valueOf(600), fsPluginProperties.getReceivedPurgeExpired(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetUser() {
        Assert.assertEquals("user1", fsPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void testGetPassword() {
        Assert.assertEquals("pass1", fsPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void testGetUser_NotSecured() {
        Assert.assertEquals("", fsPluginProperties.getUser(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_Domain() {
        Assert.assertEquals("cid:attachment", fsPluginProperties.getPayloadId(DOMAIN1));
    }

    @Test
    public void testGetPayloadId_DomainMissing() {
        Assert.assertEquals("cid:message", fsPluginProperties.getPayloadId(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_NullDomain() {
        Assert.assertEquals("cid:message", fsPluginProperties.getPayloadId(DEFAULT_DOMAIN));
    }

    @Test
    public void testGetPassword_NotSecured() {
        Assert.assertEquals("", fsPluginProperties.getPassword(DOMAIN2));
    }

    @Test
    public void testGetExpression_Domain1() {
        Assert.assertEquals("bdx:noprocess#TC1Leg1", fsPluginProperties.getExpression(DOMAIN1));
    }

    @Test
    public void testGetExpression_Domain2() {
        Assert.assertEquals("bdx:noprocess#TC2Leg1", fsPluginProperties.getExpression(DOMAIN2));
    }

    @Test
    public void testGetDomains_Ordered() {
        Assert.assertEquals(DOMAIN1, fsPluginProperties.getDomainsOrdered().get(0));
        Assert.assertEquals(DOMAIN2, fsPluginProperties.getDomainsOrdered().get(1));
        Assert.assertEquals(ODR, fsPluginProperties.getDomainsOrdered().get(2));
        Assert.assertEquals(BRIS, fsPluginProperties.getDomainsOrdered().get(3));
    }

    @Test
    public void testGetDomains_UnOrdered() {
        int unorderedA = fsPluginProperties.getDomainsOrdered().indexOf(UNORDEREDA);
        int unorderedB = fsPluginProperties.getDomainsOrdered().indexOf(UNORDEREDB);

        Assert.assertTrue(unorderedA == 4 || unorderedA == 5);
        Assert.assertTrue(unorderedB == 4 || unorderedB == 5);
    }

    @Test
    public void testKnownPropertyValue_singleTenancy() {
        final String domainDefault = "default";
        final String domain1 = "DOMAIN1";
        final String domain2 = "UNORDEREDA";
        final String propertyName1 = "fsplugin.messages.location";
        final String propertyName2 = "fsplugin.messages.location";
        final String oldPropertyValue1 = "/tmp/fs_plugin_data/DOMAIN1";
        final String oldPropertyValue2 = null;
        final String newPropertyValue1 = "new-property-value1";
        final String newPropertyValue2 = "new-property-value2";
        Mockito.reset(domibusConfigurationExtService);
        when(domibusConfigurationExtService.isMultiTenantAware()).thenReturn(false);

        // test get value
        String value1 = fsPluginProperties.getKnownPropertyValue(domainDefault, domain1 + "." + propertyName1);
        String value2 = fsPluginProperties.getKnownPropertyValue(domainDefault, domain2 + "." + propertyName2);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fsPluginProperties.setKnownPropertyValue(domainDefault, domain1 + "." + propertyName1, newPropertyValue1);
        fsPluginProperties.setKnownPropertyValue(domainDefault, domain2 + "." + propertyName2, newPropertyValue2, true);

        value1 = fsPluginProperties.getKnownPropertyValue(domainDefault, domain1 + "." + propertyName2);
        value2 = fsPluginProperties.getKnownPropertyValue(domainDefault, domain2 + "." + propertyName2);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fsPluginProperties.setKnownPropertyValue(domainDefault, domain1 + "." + propertyName1, oldPropertyValue1);

        // the map that holds property values cannot accept null
        try {
            fsPluginProperties.setKnownPropertyValue(domainDefault, domain2 + "." + propertyName2, null, true);
            Assert.fail();
        } catch (DomibusPropertyException ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot set a null value for a property"));
        }
    }

    @Test
    @Ignore //TODO EDELIVERY-7553
    public void testKnownPropertyValue_multiTenancy() {
        final String propertyName = "fsplugin.messages.location";
        final String domain1 = "DOMAIN1";
        final String domain2 = "UNORDEREDA";
        final String oldPropertyValue1 = "/tmp/fs_plugin_data/DOMAIN1";
        final String oldPropertyValue2 = "/tmp/fs_plugin_data";
        final String newPropertyValue1 = "new-property-value1";
        final String newPropertyValue2 = "new-property-value2";
        Mockito.reset(domibusConfigurationExtService);
        when(domibusConfigurationExtService.isMultiTenantAware()).thenReturn(true);

        // test get value
        String value1 = fsPluginProperties.getKnownPropertyValue(domain1, propertyName);
        String value2 = fsPluginProperties.getKnownPropertyValue(domain2, propertyName);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fsPluginProperties.setKnownPropertyValue(domain1, propertyName, newPropertyValue1);
        fsPluginProperties.setKnownPropertyValue(domain2, propertyName, newPropertyValue2, true);

        value1 = fsPluginProperties.getKnownPropertyValue(domain1, propertyName);
        value2 = fsPluginProperties.getKnownPropertyValue(domain2, propertyName);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fsPluginProperties.setKnownPropertyValue(domain1, propertyName, oldPropertyValue1);
        fsPluginProperties.setKnownPropertyValue(domain2, propertyName, oldPropertyValue2, true);
    }

}
