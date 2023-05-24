package eu.domibus.core.property;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.RegexUtil;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.util.backup.BackupService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@RunWith(JMockit.class)
public class PropertyChangeManagerTest {

    @Tested
    PropertyChangeManager propertyChangeManager;


    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    PropertyRetrieveManager propertyRetrieveManager;

    @Injectable
    PropertyProviderHelper propertyProviderHelper;

    @Injectable
    ConfigurableEnvironment environment;

    @Injectable
    DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    DomibusLocalCacheService domibusLocalCacheService;

    @Injectable
    DomibusCoreMapper coreMapper;

    @Injectable
    BackupService backupService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    RegexUtil regexUtil;

    Map<String, DomibusPropertyMetadata> props;
    String domainCode = "domain1";
    Domain domain = new Domain(domainCode, "DomainName1");

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";

    @Before
    public void setUp() {
        props = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, false),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT),
        }).collect(Collectors.toMap(DomibusPropertyMetadata::getName, x -> x));
    }

    @Test
    public void setPropertyValue() {
        String propValue = "propValue";
        String propertyName = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
        DomibusPropertyMetadata propMeta = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
        String oldValue = "old_value";

        new Expectations(propertyChangeManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
            propertyRetrieveManager.getInternalProperty(domain, propertyName);
            result = oldValue;
            propertyChangeManager.setValueInDomibusPropertySource(anyString, propValue);
            propertyChangeManager.saveInFile(null, anyString, anyString, anyString);
        }};

        propertyChangeManager.setPropertyValue(domain, propertyName, propValue, true);

        new Verifications() {{
            propertyChangeManager.doSetPropertyValue(domain, propertyName, propValue);
            propertyChangeManager.signalPropertyValueChanged(domain, propertyName, propValue, true, propMeta, oldValue);
        }};
    }

    @Test
    public void setPropertyValue_MultiTenancy_Domain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);
        String propKey = domain.getCode() + "." + propertyName;

        new Expectations(propertyChangeManager) {{
            propertyProviderHelper.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
            propertyProviderHelper.getPropertyKeyForDomain(domain, propertyName);
            this.result = propKey;
            propertyChangeManager.setValueInDomibusPropertySource(propKey, propertyValue);
            propertyChangeManager.saveInFile(domain, propertyName, propertyValue, anyString);
        }};

        propertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            propertyChangeManager.setValueInDomibusPropertySource(propKey, propertyValue);
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_MultiTenancy_Domain_NoDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL, false);

        new Expectations() {{
            propertyProviderHelper.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
        }};

        propertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(domain.getCode() + "." + propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_NoDomain_SuperProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);
        String propKey = "super." + propertyName;
        new Expectations(propertyChangeManager) {{
            propertyProviderHelper.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
            propertyProviderHelper.getPropertyKeyForSuper(propertyName);
            this.result = propKey;
            propertyChangeManager.setValueInDomibusPropertySource("super." + propertyName, propertyValue);
            propertyChangeManager.saveInFile(null, anyString, anyString, anyString);
        }};

        propertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            propertyChangeManager.setValueInDomibusPropertySource("super." + propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_NoDomain_GlobalProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true);

        new Expectations(propertyChangeManager) {{
            propertyProviderHelper.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
            propertyChangeManager.setValueInDomibusPropertySource(propertyName, propertyValue);
            propertyChangeManager.saveInFile(null, anyString, anyString, anyString);
        }};

        propertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            propertyChangeManager.setValueInDomibusPropertySource(propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_MultiTenancy_NoDomain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations() {{
            propertyProviderHelper.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
        }};

        propertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void setPropertyValue_SingleTenancy() {
        new Expectations(propertyChangeManager) {{
            propertyProviderHelper.isMultiTenantAware();
            result = false;
            propertyChangeManager.setValueInDomibusPropertySource(propertyName, propertyValue);
            propertyChangeManager.saveInFile(domain, propertyName, propertyValue, anyString);
        }};

        propertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            propertyChangeManager.setValueInDomibusPropertySource(propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test
    public void setValueInDomibusPropertySource(@Injectable MutablePropertySources propertySources,
                                                @Injectable DomibusPropertiesPropertySource domibusPropertiesPropertySource) {
        new Expectations() {{
            environment.getPropertySources();
            result = propertySources;
            propertySources.get(DomibusPropertiesPropertySource.NAME);
            result = domibusPropertiesPropertySource;
        }};

        propertyChangeManager.setValueInDomibusPropertySource(propertyName, propertyValue);

        new Verifications() {{
            propertySources.get(DomibusPropertiesPropertySource.NAME);
            domibusPropertiesPropertySource.setProperty(propertyName, propertyValue);
        }};
    }

    @Test
    public void getPropertyValueAsInteger(@Injectable Domain domain) {
        String propertyName = DOMIBUS_PROPERTY_BACKUP_PERIOD_MIN;
        String propertyValue = "12.555";
        Integer defaultValue = 24;
        Integer propIntValue;
        new Expectations(propertyChangeManager) {{
            propertyChangeManager.getInternalPropertyValue(domain, propertyName);
            result = propertyValue;
        }};
        propIntValue = propertyChangeManager.getPropertyValueAsInteger(domain, propertyName, defaultValue);
        Assert.assertEquals(propIntValue, defaultValue);
    }
}
