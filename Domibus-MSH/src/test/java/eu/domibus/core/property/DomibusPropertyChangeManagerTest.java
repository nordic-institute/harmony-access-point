package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@RunWith(JMockit.class)
public class DomibusPropertyChangeManagerTest {

    @Tested
    DomibusPropertyChangeManager domibusPropertyChangeManager;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomibusPropertyChangeNotifier propertyChangeNotifier;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    Map<String, DomibusPropertyMetadata> props;
    String domainCode = "domain1";
    Domain domain = new Domain(domainCode, "DomainName1");

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";

    @Before
    public void setUp() {
        props = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_EMAIL, DomibusPropertyMetadata.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadata.Usage.DOMAIN, false),
                DomibusPropertyMetadata.getGlobalProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Test
    public void setPropertyValue() {
        String propValue = "propValue";
        String propertyName = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
        DomibusPropertyMetadata propMeta = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
        String oldValue = "old_value";

        new Expectations(domibusPropertyChangeManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            result = oldValue;
        }};

        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propValue, true);

        new Verifications() {{
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propValue);
            domibusPropertyChangeManager.signalPropertyValueChanged(domain, propertyName, propValue, true, propMeta, oldValue);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_exception() {
        String propValue = "propValue";
        String propertyName = DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN;
        DomibusPropertyMetadata propMeta = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);

        new Expectations(domibusPropertyChangeManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = props.get(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN);
            domibusPropertyChangeManager.validatePropertyValue(propMeta, propValue);
            result = new DomibusPropertyException("Property change listener error");
        }};

        domibusPropertyChangeManager.setPropertyValue(domain, propertyName, propValue, true);

        new Verifications() {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            domibusPropertyProvider.getInternalProperty(domain, propertyName);
            times = 0;
            domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propValue);
            times = 0;

        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_Domain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);
        String propKey = domain.getCode() + "." + propertyName;

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
            domibusPropertyProvider.getPropertyKeyForDomain(domain, propertyName);
            this.result = propKey;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource(propKey, propertyValue);
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_MultiTenancy_Domain_NoDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL, false);

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(domain.getCode() + "." + propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_NoDomain_SuperProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);
        String propKey = "super." + propertyName;
        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
            domibusPropertyProvider.getPropertyKeyForSuper(propertyName);
            this.result = propKey;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource("super." + propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_NoDomain_GlobalProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource(propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setPropertyValue_MultiTenancy_NoDomain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(null, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void setPropertyValue_SingleTenancy() {
        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
        }};

        domibusPropertyChangeManager.doSetPropertyValue(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource(propertyName, propertyValue);
            times = 1;
        }};
    }
}
