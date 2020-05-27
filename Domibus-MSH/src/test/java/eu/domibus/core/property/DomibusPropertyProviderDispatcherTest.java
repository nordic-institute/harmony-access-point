package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 */
@RunWith(JMockit.class)
public class DomibusPropertyProviderDispatcherTest {
    @Tested
    DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;

    @Injectable
    ClassUtil classUtil;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Injectable
    private DomibusPropertyProviderImpl domibusPropertyProvider;

    @Injectable
    DomibusPropertyChangeManager domibusPropertyChangeManager;

    @Mocked
    DomibusPropertyMetadata propMeta;

    private String propertyName = "domibus.property.name";
    private String propertyValue = "domibus.property.value";
    private Domain domain = new Domain("domain1", "Domain 1");

    @Test()
    public void getInternalOrExternalPproperty_internal() {
        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
            domibusPropertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
            result = propertyValue;
        }};

        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        assertEquals(propertyValue, result);

        new Verifications() {{
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            times = 0;
            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, (DomibusPropertyManagerExt) any);
            times = 0;
        }};
    }

    @Test()
    public void getInternalOrExternalPproperty_external(@Mocked DomibusPropertyManagerExt manager) {
        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = manager;
            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            result = propertyValue;
        }};

        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
        assertEquals(propertyValue, result);

        new Verifications() {{
            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            domibusPropertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
            times = 0;
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void getInternalOrExternalPproperty_external_error(@Mocked DomibusPropertyManagerExt manager) {
        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = null;
        }};

        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);

        new Verifications() {{
            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
            times = 0;
        }};
    }

    @Test()
    public void setInternalOrExternalPproperty_internal() {
        String currentValue = "currentVal";
        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = true;
        }};

        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            times = 0;
            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
            times = 0;
        }};
    }

    @Test()
    public void setInternalOrExternalPproperty_external(@Mocked DomibusPropertyManagerExt manager) {

        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = manager;
        }};

        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
            times=0;
            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
        }};
    }

    @Test(expected = DomibusPropertyException.class)
    public void setInternalOrExternalPproperty_external_error(@Mocked DomibusPropertyManagerExt manager) {

        new Expectations(domibusPropertyProviderDispatcher) {{
            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
            result = propMeta;
            propMeta.isStoredGlobally();
            result = false;
            globalPropertyMetadataManager.getManagerForProperty(propertyName);
            result = null;
        }};

        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);

        new Verifications() {{
            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
            times=0;
        }};
    }

    @Test
    public void getPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
        String propertyName = "prop1";
        new Expectations(domibusPropertyProviderDispatcher) {{
            classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class});
            returns(true, false);
        }};

        domibusPropertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
        new Verifications() {{
            propertyManager.getKnownPropertyValue(propertyName);
        }};

        domibusPropertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
        new Verifications() {{
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            propertyManager.getKnownPropertyValue(currentDomain.getCode(), propertyName);
        }};
    }

    @Test
    public void setPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
        String propertyName = "prop1";
        String proertyValue = "propVal1";
        new Expectations(domibusPropertyProviderDispatcher) {{
            classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class});
            returns(true, false);
        }};

        domibusPropertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
        new Verifications() {{
            propertyManager.setKnownPropertyValue(propertyName, proertyValue);
        }};

        domibusPropertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
        new Verifications() {{
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            propertyManager.setKnownPropertyValue(currentDomain.getCode(), propertyName, proertyValue);
        }};
    }

}