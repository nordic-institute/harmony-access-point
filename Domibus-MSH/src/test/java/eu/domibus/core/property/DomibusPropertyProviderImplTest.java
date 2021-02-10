package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.encryption.PasswordDecryptionService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

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
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    ConfigurableEnvironment environment;

    @Injectable
    PropertyProviderDispatcher propertyProviderDispatcher;

    @Injectable
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Injectable
    private NestedPropertiesManager nestedPropertiesManager;

    @Injectable
    private PropertyProviderHelper propertyProviderHelper;

    @Injectable
    private PasswordDecryptionService passwordDecryptionService;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

    @Test
    public void getProperty() {
        new Expectations(domibusPropertyProvider) {{
            propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getProperty(propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, null);
        }};
    }

    @Test
    public void getPropertyWithDomain() {
        new Expectations(domibusPropertyProvider) {{
            propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
            result = propertyValue;
        }};

        String result = domibusPropertyProvider.getProperty(domain, propertyName);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        }};
    }

    @Test
    public void setProperty() {

        domibusPropertyProvider.setProperty(propertyName, propertyValue);

        new Verifications() {{
            propertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
        }};
    }

    @Test
    public void setPropertyWithDomain() {

        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);
        }};
    }

    @Test
    public void setPropertyWithDomainNull() {

        domibusPropertyProvider.setProperty(null, propertyName, propertyValue, true);

        new Verifications() {{
            propertyProviderDispatcher.setInternalOrExternalProperty(null, propertyName, propertyValue, true);
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
    public void getPropertyValue(@Mocked DomibusPropertyMetadata meta) {

        new Expectations() {{
            propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
            result = propertyValue;
            meta.isEncrypted();
            result=true;
            passwordDecryptionService.isValueEncrypted(anyString);
            result = true;
        }};

        String result = domibusPropertyProvider.getPropertyValue(propertyName, domain);

        new Verifications() {{
            passwordDecryptionService.decryptProperty(domain, propertyName, propertyValue);
        }};

    }

}
