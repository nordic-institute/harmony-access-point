package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.PasswordEncryptionExtService;
import eu.domibus.plugin.property.PluginPropertyChangeNotifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.when;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno, Cosmin Baciu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class FSPluginPropertiesTestIT {

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
    private FSPluginProperties fSPluginProperties;

    @Autowired
    DomibusConfigurationExtService domibusConfigurationExtService;

    @Configuration
    static class ContextConfiguration {

        @Bean
        public FSPluginProperties pluginProperties() {
            FSPluginProperties fsPluginProperties = new FSPluginProperties();
            return fsPluginProperties;
        }

        @Bean
        public Properties fsPluginProperties() throws IOException {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("FSPluginPropertiesTest_fs-plugin.properties"));
            return properties;
        }

        @Bean
        public PasswordEncryptionExtService passwordEncryptionExtService() {
            return Mockito.mock(PasswordEncryptionExtService.class);
        }

        @Bean
        public DomainExtService domainExtService() {
            return Mockito.mock(DomainExtService.class);
        }

        @Bean
        public DomibusConfigurationExtService domibusConfigurationExtService() {
            return Mockito.mock(DomibusConfigurationExtService.class);
        }

        @Bean
        public PluginPropertyChangeNotifier propertyChangeNotifier() {
            return Mockito.mock(PluginPropertyChangeNotifier.class);
        }

        @Bean
        public FSPluginPropertiesMetadataManagerImpl fsPluginPropertiesMetadataManager() {
            return new FSPluginPropertiesMetadataManagerImpl();
        }

        @Bean
        public DomainContextExtService domainContextExtService() {
            return new DomainContextExtService() {
                @Override
                public DomainDTO getCurrentDomain() {
                    return null;
                }

                @Override
                public DomainDTO getCurrentDomainSafely() {
                    return null;
                }

                @Override
                public void setCurrentDomain(DomainDTO domain) {

                }

                @Override
                public void clearCurrentDomain() {

                }
            };
        }

    }

    @Test
    public void testGetLocation() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(null));
    }

    @Test
    public void testGetLocation_Domain1() throws Exception {
        Assert.assertEquals(DOMAIN1_LOCATION, fSPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void testGetLocation_NonExistentDomain() throws Exception {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(NONEXISTENT_DOMAIN));
    }

    @Test
    public void testGetSentAction() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fSPluginProperties.getSentAction(null));
    }

    @Test
    public void testGetSentPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0 0/1 * * * ?", fSPluginProperties.getSentPurgeWorkerCronExpression(null));
    }

    @Test
    public void testGetSentPurgeExpired() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getSentPurgeExpired(null));
    }

    @Test
    public void testGetFailedAction() throws Exception {
        Assert.assertEquals(FSPluginProperties.ACTION_ARCHIVE, fSPluginProperties.getFailedAction(null));
    }

    @Test
    public void testGetFailedPurgeWorkerCronExpression() throws Exception {
        Assert.assertEquals("0 0/1 * * * ?", fSPluginProperties.getFailedPurgeWorkerCronExpression(null));
    }

    @Test
    public void testGetFailedPurgeExpired() throws Exception {
        Assert.assertEquals(null, fSPluginProperties.getFailedPurgeExpired(null));
    }

    @Test
    public void testGetReceivedPurgeExpired() throws Exception {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getReceivedPurgeExpired(null));
    }

    @Test
    public void testGetUser() throws Exception {
        Assert.assertEquals("user1", fSPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void testGetPassword() throws Exception {
        Assert.assertEquals("pass1", fSPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void testGetUser_NotSecured() throws Exception {
        Assert.assertEquals("", fSPluginProperties.getUser(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_Domain() throws Exception {
        Assert.assertEquals("cid:attachment", fSPluginProperties.getPayloadId(DOMAIN1));
    }

    @Test
    public void testGetPayloadId_DomainMissing() throws Exception {
        Assert.assertEquals("cid:message", fSPluginProperties.getPayloadId(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_NullDomain() throws Exception {
        Assert.assertEquals("cid:message", fSPluginProperties.getPayloadId(null));
    }

    @Test
    public void testGetPassword_NotSecured() throws Exception {
        Assert.assertEquals("", fSPluginProperties.getPassword(DOMAIN2));
    }

    @Test
    public void testGetExpression_Domain1() throws Exception {
        Assert.assertEquals("bdx:noprocess#TC1Leg1", fSPluginProperties.getExpression(DOMAIN1));
    }

    @Test
    public void testGetExpression_Domain2() throws Exception {
        Assert.assertEquals("bdx:noprocess#TC2Leg1", fSPluginProperties.getExpression(DOMAIN2));
    }

    @Test
    public void testGetDomains_Ordered() throws Exception {
        Assert.assertEquals(DOMAIN1, fSPluginProperties.getDomains().get(0));
        Assert.assertEquals(DOMAIN2, fSPluginProperties.getDomains().get(1));
        Assert.assertEquals(ODR, fSPluginProperties.getDomains().get(2));
        Assert.assertEquals(BRIS, fSPluginProperties.getDomains().get(3));
    }

    @Test
    public void testGetDomains_UnOrdered() throws Exception {
        int unorderedA = fSPluginProperties.getDomains().indexOf(UNORDEREDA);
        int unorderedB = fSPluginProperties.getDomains().indexOf(UNORDEREDB);

        Assert.assertTrue(unorderedA == 4 || unorderedA == 5);
        Assert.assertTrue(unorderedB == 4 || unorderedB == 5);
    }

    @Test
    public void testKnownPropertyValue_singleTenancy() {
        final String domain = "default";
        final String propertyName1 = "fsplugin.domains.DOMAIN1.messages.location";
        final String propertyName2 = "fsplugin.domains.UNORDEREDA.messages.location";
        final String oldPropertyValue1 = "/tmp/fs_plugin_data/DOMAIN1";
        final String oldPropertyValue2 = "/tmp/fs_plugin_data";
        final String newPropertyValue1 = "new-property-value1";
        final String newPropertyValue2 = "new-property-value2";
        fSPluginProperties.knownProperties = null;
        Mockito.reset(domibusConfigurationExtService);
        when(domibusConfigurationExtService.isMultiTenantAware()).thenReturn(false);

        // test get value
        String value1 = fSPluginProperties.getKnownPropertyValue(domain, propertyName1);
        String value2 = fSPluginProperties.getKnownPropertyValue(domain, propertyName2);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fSPluginProperties.setKnownPropertyValue(domain, propertyName1, newPropertyValue1);
        fSPluginProperties.setKnownPropertyValue(domain, propertyName2, newPropertyValue2, true);

        value1 = fSPluginProperties.getKnownPropertyValue(domain, propertyName1);
        value2 = fSPluginProperties.getKnownPropertyValue(domain, propertyName2);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fSPluginProperties.setKnownPropertyValue(domain, propertyName1, oldPropertyValue1);
        fSPluginProperties.setKnownPropertyValue(domain, propertyName2, oldPropertyValue2, true);
    }

    @Test
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
        String value1 = fSPluginProperties.getKnownPropertyValue(domain1, propertyName);
        String value2 = fSPluginProperties.getKnownPropertyValue(domain2, propertyName);

        Assert.assertEquals(oldPropertyValue1, value1);
        Assert.assertEquals(oldPropertyValue2, value2);

        // test set value
        fSPluginProperties.setKnownPropertyValue(domain1, propertyName, newPropertyValue1);
        fSPluginProperties.setKnownPropertyValue(domain2, propertyName, newPropertyValue2, true);

        value1 = fSPluginProperties.getKnownPropertyValue(domain1, propertyName);
        value2 = fSPluginProperties.getKnownPropertyValue(domain2, propertyName);

        Assert.assertEquals(newPropertyValue1, value1);
        Assert.assertEquals(newPropertyValue2, value2);

        // reset context
        fSPluginProperties.setKnownPropertyValue(domain1, propertyName, oldPropertyValue1);
        fSPluginProperties.setKnownPropertyValue(domain2, propertyName, oldPropertyValue2, true);
    }
}