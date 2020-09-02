package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 */
@RunWith(JMockit.class)
public class DomibusPropertyProviderImplTest {

    @Tested
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

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

    @Test
    public void getProperty() {
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
        }};
    }

    @Test
    public void getPropertyWithDomain() {
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        }};
    }

    @Test
    public void setProperty() {

        domibusPropertyProvider.setProperty(propertyName, propertyValue);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
        }};
    }

    @Test
    public void setPropertyWithDomain() {

        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);
        }};
    }

    @Test
    public void setPropertyWithDomainNull() {

        domibusPropertyProvider.setProperty(null, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
        }};
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void getIntegerProperty() {
        String val = "2";
        Integer intVal = Integer.valueOf(val);
        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
            primitivePropertyTypesManager.getIntegerInternal(propertyName, val);
            result = intVal;
        }};

        Integer res = domibusPropertyProvider.getIntegerProperty(propertyName);

        assertEquals(intVal, res);
    }

    @Test
    public void getLongProperty() {
        String val = "2";
        Long longVal = Long.valueOf(val);

        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
            primitivePropertyTypesManager.getLongInternal(propertyName, val);
            result = longVal;
        }};

        Long res = domibusPropertyProvider.getLongProperty(propertyName);

        assertEquals(longVal, res);
    }

    @Test
    public void getBooleanProperty() {
        String val = "true";
        boolean boolVal = Boolean.valueOf(val);

        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(propertyName);
            result = val;
            primitivePropertyTypesManager.getBooleanInternal(propertyName, val);
            result = boolVal;
        }};

        Boolean res = domibusPropertyProvider.getBooleanProperty(propertyName);

        assertEquals(boolVal, res);
    }

    @Test
    public void getBooleanDomainProperty() {
        String val = "true";
        boolean boolVal = Boolean.valueOf(val);

        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.getProperty(domain, propertyName);
            result = val;
            primitivePropertyTypesManager.getBooleanInternal(propertyName, val);
            result = boolVal;
        }};

        Boolean res = domibusPropertyProvider.getBooleanProperty(domain, propertyName);

        assertEquals(boolVal, res);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void setValueInDomibusPropertySource(@Injectable MutablePropertySources propertySources,
                                                @Injectable DomibusPropertiesPropertySource domibusPropertiesPropertySource) {
        new Expectations(domibusPropertyProvider) {{
            environment.getPropertySources();
            result = propertySources;
            propertySources.get(DomibusPropertiesPropertySource.NAME);
            result = domibusPropertiesPropertySource;
        }};

        domibusPropertyProvider.setValueInDomibusPropertySource(propertyName, propertyValue);

        new Verifications() {{
            environment.getPropertySources();
            propertySources.get(DomibusPropertiesPropertySource.NAME);
            domibusPropertiesPropertySource.setProperty(propertyName, propertyValue);
        }};
    }

    @Test
    public void getPropertyValue() {

        new Expectations(domibusPropertyProvider) {{
            environment.getProperty(propertyName);
            result = propertyValue;
            passwordEncryptionService.isValueEncrypted(anyString);
            result = true;
        }};

        domibusPropertyProvider.getPropertyValue(propertyName, domain, true);

        new Verifications() {{
            passwordEncryptionService.decryptProperty(domain, propertyName, propertyValue);
        }};

    }

    @Test
    public void testGetNestedProperties() {
        String prefix = "routing";

        Set<String> propertiesStartingWithPrefix = new HashSet<>();
        propertiesStartingWithPrefix.add("routing.rule1");
        propertiesStartingWithPrefix.add("routing.rule1.queue");
        propertiesStartingWithPrefix.add("routing.rule1.service");

        new Expectations(domibusPropertyProvider) {{
            domibusPropertyProvider.filterPropertiesName((Predicate) any);
            result = propertiesStartingWithPrefix;
        }};

        List<String> nestedProperties = domibusPropertyProvider.getNestedProperties(prefix);
        Assert.assertEquals(1, nestedProperties.size());
        Assert.assertTrue(nestedProperties.contains("rule1"));
    }

    @Test
    public void getPropertyPrefix() {
        String prefix = "rule1";

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
        }};

        String propertyPrefix = domibusPropertyProvider.getPropertyPrefix(DomainService.DEFAULT_DOMAIN, prefix);
        assertEquals(prefix + ".", propertyPrefix);

        new Verifications() {{
            domibusPropertyProvider.computePropertyPrefix((Domain) any, anyString);
            times = 0;
        }};
    }

    @Test
    public void getPropertyPrefixForMultitenancy() {
        String prefix = "rule1";
        String domainPrefix = "default.rule1";
        String propertyPrefix = domainPrefix + ".";

        new Expectations(domibusPropertyProvider) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domibusPropertyProvider.computePropertyPrefix(DomainService.DEFAULT_DOMAIN, prefix);
            result = domainPrefix;
        }};


        String propertyName = domibusPropertyProvider.getPropertyPrefix(DomainService.DEFAULT_DOMAIN, prefix);
        assertEquals(propertyPrefix, propertyName);
    }

    @Test
    public void computePropertyPrefixForDefaultDomain() {
        String value = domibusPropertyProvider.computePropertyPrefix(DomainService.DEFAULT_DOMAIN, "rule1");
        Assert.assertEquals(DomainService.DEFAULT_DOMAIN.getCode() + ".rule1", value);
    }

    @Test
    public void computePropertyPrefix(@Injectable Domain domain) {
        String domainCode = "digit";

        new Expectations() {{
            domain.getCode();
            result = domainCode;
        }};

        String value = domibusPropertyProvider.computePropertyPrefix(domain, "rule1");
        Assert.assertEquals(domainCode + ".rule1", value);
    }
}