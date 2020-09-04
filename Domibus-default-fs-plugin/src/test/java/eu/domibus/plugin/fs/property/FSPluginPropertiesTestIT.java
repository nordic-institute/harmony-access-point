package eu.domibus.plugin.fs.property;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.*;
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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

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
            return new FSPluginProperties();
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
        public DomibusPropertyExtService domibusPropertyExtService() {
            return new DomibusPropertyExtService() {
                @Override
                public String getProperty(String propertyName) {
                    return null;
                }

                @Override
                public String getProperty(DomainDTO domain, String propertyName) {
                    return null;
                }

                @Override
                public Integer getIntegerProperty(String propertyName) {
                    return null;
                }

                @Override
                public Set<String> filterPropertiesName(Predicate<String> predicate) {
                    return null;
                }

                @Override
                public List<String> getNestedProperties(String prefix) {
                    return null;
                }

                @Override
                public String getDomainProperty(DomainDTO domain, String propertyName) {
                    return null;
                }

                @Override
                public void setDomainProperty(DomainDTO domain, String propertyName, String propertyValue) {

                }

                @Override
                public void setProperty(String propertyName, String propertyValue) {

                }

                @Override
                public boolean containsDomainPropertyKey(DomainDTO domain, String propertyName) {
                    return false;
                }

                @Override
                public boolean containsPropertyKey(String propertyName) {
                    return false;
                }

                @Override
                public String getDomainProperty(DomainDTO domain, String propertyName, String defaultValue) {
                    return null;
                }

                @Override
                public String getDomainResolvedProperty(DomainDTO domain, String propertyName) {
                    return null;
                }

                @Override
                public String getResolvedProperty(String propertyName) {
                    return null;
                }
            };
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
    public void testGetLocation() {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(null));
    }

    @Test
    public void testGetLocation_Domain1() {
        Assert.assertEquals(DOMAIN1_LOCATION, fSPluginProperties.getLocation(DOMAIN1));
    }

    @Test
    public void testGetLocation_NonExistentDomain() {
        Assert.assertEquals(DEFAULT_LOCATION, fSPluginProperties.getLocation(NONEXISTENT_DOMAIN));
    }

    @Test
    public void testGetSentAction() {
        Assert.assertEquals(FSPluginProperties.ACTION_DELETE, fSPluginProperties.getSentAction(null));
    }

    @Test
    public void testGetSentPurgeWorkerCronExpression() {
        Assert.assertEquals("0 0/1 * * * ?", fSPluginProperties.getSentPurgeWorkerCronExpression(null));
    }

    @Test
    public void testGetSentPurgeExpired() {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getSentPurgeExpired(null));
    }

    @Test
    public void testGetFailedAction() {
        Assert.assertEquals(FSPluginProperties.ACTION_ARCHIVE, fSPluginProperties.getFailedAction(null));
    }

    @Test
    public void testGetFailedPurgeWorkerCronExpression() {
        Assert.assertEquals("0 0/1 * * * ?", fSPluginProperties.getFailedPurgeWorkerCronExpression(null));
    }

    @Test
    public void testGetFailedPurgeExpired() {
        Assert.assertEquals((Integer) 0, fSPluginProperties.getFailedPurgeExpired(null));
    }

    @Test
    public void testGetReceivedPurgeExpired() {
        Assert.assertEquals(Integer.valueOf(600), fSPluginProperties.getReceivedPurgeExpired(null));
    }

    @Test
    public void testGetUser() {
        Assert.assertEquals("user1", fSPluginProperties.getUser(DOMAIN1));
    }

    @Test
    public void testGetPassword() {
        Assert.assertEquals("pass1", fSPluginProperties.getPassword(DOMAIN1));
    }

    @Test
    public void testGetUser_NotSecured() {
        Assert.assertEquals("", fSPluginProperties.getUser(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_Domain() {
        Assert.assertEquals("cid:attachment", fSPluginProperties.getPayloadId(DOMAIN1));
    }

    @Test
    public void testGetPayloadId_DomainMissing() {
        Assert.assertEquals("cid:message", fSPluginProperties.getPayloadId(DOMAIN2));
    }

    @Test
    public void testGetPayloadId_NullDomain() {
        Assert.assertEquals("cid:message", fSPluginProperties.getPayloadId(null));
    }

    @Test
    public void testGetPassword_NotSecured() {
        Assert.assertEquals("", fSPluginProperties.getPassword(DOMAIN2));
    }

    @Test
    public void testGetExpression_Domain1() {
        Assert.assertEquals("bdx:noprocess#TC1Leg1", fSPluginProperties.getExpression(DOMAIN1));
    }

    @Test
    public void testGetExpression_Domain2() {
        Assert.assertEquals("bdx:noprocess#TC2Leg1", fSPluginProperties.getExpression(DOMAIN2));
    }

    @Test
    public void testGetDomains_Ordered() {
        Assert.assertEquals(DOMAIN1, fSPluginProperties.getDomains().get(0));
        Assert.assertEquals(DOMAIN2, fSPluginProperties.getDomains().get(1));
        Assert.assertEquals(ODR, fSPluginProperties.getDomains().get(2));
        Assert.assertEquals(BRIS, fSPluginProperties.getDomains().get(3));
    }

    @Test
    public void testGetDomains_UnOrdered() {
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