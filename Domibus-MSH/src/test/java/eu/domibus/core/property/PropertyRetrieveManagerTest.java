package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordEncryptionService;
import mockit.*;
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

import static eu.domibus.api.property.DomibusPropertyMetadata.NAME_SEPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class PropertyRetrieveManagerTest {

    @Tested
    PropertyRetrieveManager propertyRetrieveManager;

    @Injectable
    @Qualifier("domibusProperties")
    protected Properties domibusProperties;

    @Injectable
    @Qualifier("domibusDefaultProperties")
    protected Properties domibusDefaultProperties;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    ConfigurableEnvironment environment;

    @Injectable
    private PropertyProviderHelper propertyProviderHelper;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

    @Test
    public void getPropertyOnlyGlobal() {
        DomibusPropertyMetadata global = DomibusPropertyMetadata.getReadOnlyGlobalProperty(propertyName);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = global;

            propertyRetrieveManager.getGlobalProperty(global);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(global);
            times = 1;
            propertyProviderHelper.isMultiTenantAware();
            times = 0;
        }};
    }

    @Test
    public void getPropertySingleTenancy() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, false);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = false;

            propertyRetrieveManager.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 1;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
        }};
    }

    @Test
    public void getProperty_MultiTenancy_Domain() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyProviderHelper.getCurrentDomain();
            result = domain;

            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 0;
            propertyProviderHelper.isMultiTenantAware();

            times = 1;
            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            times = 1;
        }};
    }

    @Test
    public void getProperty_MultiTenancy_NotDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.SUPER, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyProviderHelper.getCurrentDomain();
            result = domain;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(null, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 0;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            times = 0;
        }};
    }

    @Test
    public void getProperty_MultiTenancy_NullDomain_GlobalProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyProviderHelper.getCurrentDomain();
            result = null;

            propertyRetrieveManager.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 1;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            times = 0;
        }};
    }

    @Test
    public void getProperty_MultiTenancy_NullDomain_SuperProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyProviderHelper.getCurrentDomain();
            result = null;

            propertyRetrieveManager.getSuperOrDefaultValue(prop);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 0;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
            propertyRetrieveManager.getSuperOrDefaultValue(prop);
            times = 1;
        }};
    }

    @Test
    public void getProperty_MultiTenancy_NullDomain_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyProviderHelper.getCurrentDomain();
            result = null;
        }};

        String result = propertyRetrieveManager.getInternalProperty(propertyName);
        assertEquals(null, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 0;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
            propertyRetrieveManager.getSuperOrDefaultValue(prop);
            times = 0;
        }};
    }

    @Test
    public void getDomainProperty_SingleTenancy() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = false;

            propertyRetrieveManager.getGlobalProperty(prop);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 1;
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void getDomainProperty_MultiTenancy_NoDomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.GLOBAL, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;
        }};

        String result = propertyRetrieveManager.getInternalProperty(domain, propertyName);

        new Verifications() {{
            propertyProviderHelper.isMultiTenantAware();

            times = 1;
            propertyRetrieveManager.getGlobalProperty(prop);
            times = 0;
        }};
    }

    @Test
    public void getDomainProperty_MultiTenancy_DomainProp() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        new Expectations(propertyRetrieveManager) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = prop;

            propertyProviderHelper.isMultiTenantAware();
            result = true;

            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            result = propertyValue;
        }};

        String result = propertyRetrieveManager.getInternalProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyProviderHelper.isMultiTenantAware();
            times = 1;
            propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);
            times = 1;
        }};
    }

    @Test
    public void getDomainOrDefaultValue_SpecificValue() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(propertyRetrieveManager) {{
            propertyProviderHelper.getPropertyKeyForDomain(domain, prop.getName());
            result=propertyKey;
            propertyRetrieveManager.getPropertyValue(propertyKey);
            result = propertyValue;
        }};

        String res = propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);

        assertEquals(propertyValue, res);

        new Verifications() {{
            propertyRetrieveManager.getPropertyValue(prop.getName());
            times = 0;
        }};
    }

    @Test
    public void getDomainOrDefaultValue_Fallback() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, true);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(propertyRetrieveManager) {{
            propertyProviderHelper.getPropertyKeyForDomain(domain, prop.getName());
            result=propertyKey;
            propertyRetrieveManager.getPropertyValue(propertyKey);
            result = null;
            propertyRetrieveManager.getPropertyValue(prop.getName());
            result = propertyValue;
        }};

        String res = propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);

        assertEquals(propertyValue, res);

        new Verifications() {{
            propertyRetrieveManager.getPropertyValue(prop.getName());
            times = 1;
        }};
    }

    @Test
    public void getDomainOrDefaultValue_NoFallback() {
        DomibusPropertyMetadata prop = new DomibusPropertyMetadata(propertyName, DomibusPropertyMetadata.Usage.DOMAIN, false);

        String propertyKey = domain.getCode() + "." + propertyName;
        new Expectations(propertyRetrieveManager) {{
            propertyProviderHelper.getPropertyKeyForDomain(domain, prop.getName());
            result=propertyKey;
            propertyRetrieveManager.getPropertyValue(propertyKey);
            result = null;
        }};

        String res = propertyRetrieveManager.getDomainOrDefaultValue(prop, domain);

        assertEquals(null, res);

        new Verifications() {{
            propertyRetrieveManager.getPropertyValue(prop.getName());
            times = 0;
        }};
    }

}
