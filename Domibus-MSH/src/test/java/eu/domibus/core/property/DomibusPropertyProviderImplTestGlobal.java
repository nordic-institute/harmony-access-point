package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Properties;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 */
@RunWith(JMockit.class)
public class DomibusPropertyProviderImplTestGlobal {
    @Tested
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Injectable
    protected PropertyResolver propertyResolver;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PasswordEncryptionService passwordEncryptionService;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    ConfigurableEnvironment environment;

    @Injectable
    DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Injectable
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

    @Test()
    public void getPropertyOnlyGlobal() {
        DomibusPropertyMetadata global = DomibusPropertyMetadata.getReadOnlyGlobalProperty(propertyName);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = global;

            domibusPropertyProvider.getGlobalProperty(global);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(global);
            times = 1;
            domibusConfigurationService.isMultiTenantAware();
            times = 0;
        }};
    }

    @Test()
    public void getPropertySingleTenancy() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, false);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = false;

            domibusPropertyProvider.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 1;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
        }};
    }

    @Test()
    public void getProperty_MultiTenancy_Domain() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 0;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            times = 1;
        }};
    }

    @Test()
    public void getProperty_MultiTenancy_NotDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.SUPER, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = domain;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(null, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 0;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            times = 0;
        }};
    }

    @Test()
    public void getProperty_MultiTenancy_NullDomain_GlobalProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = null;

            domibusPropertyProvider.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 1;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            times = 0;
        }};
    }

    @Test()
    public void getProperty_MultiTenancy_NullDomain_SuperProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = null;

            domibusPropertyProvider.getSuperOrDefaultValue(prop);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 0;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getSuperOrDefaultValue(prop);
            times = 1;
        }};
    }

    @Test()
    public void getProperty_MultiTenancy_NullDomain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainContextProvider.getCurrentDomainSafely();
            result = null;
        }};

        String result = domibusPropertyProvider.getInternalProperty(propertyName);
        assertEquals(null, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 0;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getSuperOrDefaultValue(prop);
            times = 0;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void getDomainProperty_NullDomain() {
        String result = domibusPropertyProvider.getProperty(null, propertyName);

        new Verifications() {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            times = 0;
        }};
    }

    @Test()
    public void getDomainProperty_SingleTenancy() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = false;

            domibusPropertyProvider.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 1;
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void getDomainProperty_MultiTenancy_NoDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};

        String result = domibusPropertyProvider.getInternalProperty(domain, propertyName);

        new Verifications() {{
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getGlobalProperty(prop);
            times = 0;
        }};
    }

    @Test()
    public void getDomainProperty_MultiTenancy_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getInternalProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusConfigurationService.isMultiTenantAware();
            times = 1;
            domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);
            times = 1;
        }};
    }

    @Test()
    public void setPropertyValue_SingleTenancy() {
        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            domibusPropertyProvider.setValueInDomibusPropertySource(anyString, anyString);
        }};

        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource(propertyName, propertyValue);
            times = 1;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_Domain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusPropertyProvider.setValueInDomibusPropertySource(anyString, anyString);
        }};

        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProvider.setValueInDomibusPropertySource(domain.getCode() + "." + propertyName, propertyValue);
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

        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(domain.getCode() + "." + propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void setPropertyValue_MultiTenancy_NoDomain_SuperProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            domibusPropertyProvider.setValueInDomibusPropertySource(anyString, anyString);
        }};

        domibusPropertyProvider.setProperty(null, propertyName, propertyValue);

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

            domibusPropertyProvider.setValueInDomibusPropertySource(anyString, anyString);
        }};

        domibusPropertyProvider.setProperty(null, propertyName, propertyValue);

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

        domibusPropertyProvider.setProperty(null, propertyName, propertyValue);

        new Verifications() {{
            domibusProperties.setProperty(propertyName, propertyValue);
            times = 0;
        }};
    }

    @Test()
    public void getIntegerProperty() {
        String val = "2";
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
        }};

        Integer res = domibusPropertyProvider.getIntegerProperty(propertyName);

        assertEquals(Integer.valueOf(val), res);
    }

    @Test()
    public void getLongProperty() {
        String val = "2";
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
        }};

        Long res = domibusPropertyProvider.getLongProperty(propertyName);

        assertEquals(Long.valueOf(val), res);
    }

    @Test()
    public void getBooleanProperty() {
        String val = "true";
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
        }};

        Boolean res = domibusPropertyProvider.getBooleanProperty(propertyName);

        assertEquals(Boolean.valueOf(val), res);
    }

    @Test()
    public void getBooleanDomainProperty() {
        String val = "true";
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(domain, propertyName);
            result = val;
        }};

        Boolean res = domibusPropertyProvider.getBooleanProperty(domain, propertyName);

        assertEquals(Boolean.valueOf(val), res);
    }

    @Test()
    public void filterPropertiesName(@Injectable PropertySource propertySource,
                                     @Injectable Predicate<String> predicate) {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(propertySource);

        new Expectations(domibusPropertyProvider) {{
            environment.getPropertySources();
            result = propertySources;

            domibusPropertyProvider.filterPropertySource((Predicate<String>) any, (PropertySource) any);
        }};

        domibusPropertyProvider.filterPropertiesName(predicate);

        new Verifications() {{
            domibusPropertyProvider.filterPropertySource(predicate, propertySource);
        }};
    }

    @Test()
    public void getDomainOrDefaultValue_SpecificValue() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getPropertyValue(propertyKey, domain, prop.isEncrypted());
            result = propertyValue;
        }};

        String res = domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);

        assertEquals(propertyValue, res);

        new Verifications() {{
            domibusPropertyProvider.getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            times = 0;
        }};
    }

    @Test()
    public void getDomainOrDefaultValue_Fallback() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getPropertyValue(propertyKey, domain, prop.isEncrypted());
            result = null;
            domibusPropertyProvider.getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            result = propertyValue;
        }};

        String res = domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);

        assertEquals(propertyValue, res);

        new Verifications() {{
            domibusPropertyProvider.getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            times = 1;
        }};
    }

    @Test()
    public void getDomainOrDefaultValue_NoFallback() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, false);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getPropertyValue(propertyKey, domain, prop.isEncrypted());
            result = null;
        }};

        String res = domibusPropertyProvider.getDomainOrDefaultValue(prop, domain);

        assertEquals(null, res);

        new Verifications() {{
            domibusPropertyProvider.getPropertyValue(prop.getName(), domain, prop.isEncrypted());
            times = 0;
        }};
    }
}