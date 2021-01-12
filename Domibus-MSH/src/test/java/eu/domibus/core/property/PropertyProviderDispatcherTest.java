package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_LENGTH_MAX;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;
import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 */
@RunWith(JMockit.class)
public class PropertyProviderDispatcherTest {
    @Tested
    PropertyProviderDispatcher propertyProviderDispatcher;

    @Injectable
    ClassUtil classUtil;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    PropertyChangeManager propertyChangeManager;

    @Injectable
    PropertyRetrieveManager propertyRetrieveManager;

    @Injectable
    PrimitivePropertyTypesManager primitivePropertyTypesManager;

    @Injectable
    PropertyProviderHelper propertyProviderHelper;

    @Mocked
    DomibusPropertyMetadata propMeta;

    private final String propertyName = "domibus.property.name";
    private final String propertyValue = "domibus.property.value";
    private final Domain domain = new Domain("domain1", "Domain 1");

    @Test()
    public void getInternalOrExternalProperty_internal() {
        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
            propertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
            result = propertyValue;
        }};

        String result = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        assertEquals(propertyValue, result);

        new Verifications() {{
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            times = 0;
            propertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, (DomibusPropertyManagerExt) any);
            times = 0;
        }};
    }

    @Test()
    public void getInternalOrExternalProperty_external(@Mocked DomibusPropertyManagerExt manager) {
        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = manager;
            propertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            result = propertyValue;
        }};

        String result = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            propertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
            times = 0;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void getInternalOrExternalProperty_external_error(@Mocked DomibusPropertyManagerExt manager) {
        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = null;
        }};

        String result = propertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);

        new Verifications() {{
            propertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            times = 0;
        }};
    }

    @Test()
    public void setInternalOrExternalProperty_internal() {
        String currentValue = "currentVal";
        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
        }};

        propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            propertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            times = 0;
            propertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
            times = 0;
        }};
    }

    @Test()
    public void setInternalOrExternalProperty_external(@Mocked DomibusPropertyManagerExt manager) {

        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = manager;
        }};

        propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            propertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
            times = 0;
            propertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setInternalOrExternalProperty_external_error(@Mocked DomibusPropertyManagerExt manager) {

        new Expectations(propertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = null;
        }};

        propertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            propertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
            times = 0;
        }};
    }

    @Test
    public void getPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
        String propertyName = "prop1";
        new Expectations(propertyProviderDispatcher) {{
            classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class});
            returns(true, false);
            propertyProviderHelper.getCurrentDomainCode();
            result = "default";
        }};

        propertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
        new Verifications() {{
            propertyManager.getKnownPropertyValue(propertyName);
        }};

        propertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
        new Verifications() {{
            propertyProviderHelper.getCurrentDomainCode();
            propertyManager.getKnownPropertyValue("default", propertyName);
        }};
    }

    @Test
    public void setPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
        String propertyName = "prop1";
        String proertyValue = "propVal1";
        new Expectations(propertyProviderDispatcher) {{
            classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class});
            returns(true, false);
            propertyProviderHelper.getCurrentDomainCode();
            result = "default";
        }};

        propertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
        new Verifications() {{
            propertyManager.setKnownPropertyValue(propertyName, proertyValue);
        }};

        propertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
        new Verifications() {{
            propertyProviderHelper.getCurrentDomainCode();
            propertyManager.setKnownPropertyValue("default", propertyName, proertyValue);
        }};
    }

    @Test
    public void getExternalPropertyValue_noDomain(@Mocked DomibusPropertyManagerExt manager) {
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        new Expectations(propertyProviderDispatcher) {{
            propertyProviderDispatcher.getExternalModulePropertyValue(manager, propertyName);
            result = propertyValue;
        }};

        String result = propertyProviderDispatcher.getExternalPropertyValue(propertyName, null, manager);

        assertEquals(propertyValue, result);

        new Verifications() {{
            propertyProviderDispatcher.getExternalModulePropertyValue(manager, propertyName);
            times = 1;
        }};
    }

    @Test
    public void getExternalPropertyValue_domain(@Mocked DomibusPropertyManagerExt manager, @Mocked Domain domain) {
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";

        new Expectations() {{
            manager.getKnownPropertyValue(anyString, propertyName);
            result = propertyValue;
        }};

        String result = propertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
        assertEquals(propertyValue, result);

        new Verifications() {{
            manager.getKnownPropertyValue(domain.getCode(), propertyName);
            times = 1;
        }};
    }

}