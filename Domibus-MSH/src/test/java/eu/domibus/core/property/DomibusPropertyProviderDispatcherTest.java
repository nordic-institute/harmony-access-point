//package eu.domibus.core.property;
//
//import eu.domibus.api.multitenancy.Domain;
//import eu.domibus.api.multitenancy.DomainContextProvider;
//import eu.domibus.api.multitenancy.DomainService;
//import eu.domibus.api.property.DomibusPropertyException;
//import eu.domibus.api.property.DomibusPropertyMetadata;
//import eu.domibus.api.util.ClassUtil;
//import eu.domibus.ext.services.DomibusPropertyManagerExt;
//import mockit.*;
//import mockit.integration.junit4.JMockit;
//import org.apache.commons.lang3.StringUtils;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PROPERTY_LENGTH_MAX;
//import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;
//import static org.junit.Assert.assertEquals;
//
///**
// * @author Ion Perpegel
// */
//@RunWith(JMockit.class)
//public class DomibusPropertyProviderDispatcherTest {
//    @Tested
//    DomibusPropertyProviderDispatcher domibusPropertyProviderDispatcher;
//
//    @Injectable
//    ClassUtil classUtil;
//
//    @Injectable
//    protected DomainContextProvider domainContextProvider;
//
//    @Injectable
//    GlobalPropertyMetadataManager globalPropertyMetadataManager;
//
//    @Injectable
//    private DomibusPropertyProviderImpl domibusPropertyProvider;
//
//    @Injectable
//    DomibusPropertyChangeManager domibusPropertyChangeManager;
//
//    @Injectable
//    DomainService domainService;
//
//    @Mocked
//    DomibusPropertyMetadata propMeta;
//
//    private String propertyName = "domibus.property.name";
//    private String propertyValue = "domibus.property.value";
//    private Domain domain = new Domain("domain1", "Domain 1");
//
//    @Test()
//    public void getInternalOrExternalProperty_internal() {
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = true;
//            domibusPropertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
//            result = propertyValue;
//        }};
//
//        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
//        assertEquals(propertyValue, result);
//
//        new Verifications() {{
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            times = 0;
//            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, (DomibusPropertyManagerExt) any);
//            times = 0;
//        }};
//    }
//
//    @Test()
//    public void getInternalOrExternalProperty_external(@Mocked DomibusPropertyManagerExt manager) {
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = false;
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            result = manager;
//            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
//            result = propertyValue;
//        }};
//
//        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
//        assertEquals(propertyValue, result);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
//            domibusPropertyProviderDispatcher.getInternalPropertyValue(domain, propertyName);
//            times = 0;
//        }};
//    }
//
//    @Test(expected = DomibusPropertyException.class)
//    public void getInternalOrExternalProperty_external_error(@Mocked DomibusPropertyManagerExt manager) {
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = false;
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            result = null;
//        }};
//
//        String result = domibusPropertyProviderDispatcher.getInternalOrExternalProperty(propertyName, domain);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
//            times = 0;
//        }};
//    }
//
//    @Test()
//    public void setInternalOrExternalProperty_internal() {
//        String currentValue = "currentVal";
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = true;
//        }};
//
//        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            times = 0;
//            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
//            times = 0;
//        }};
//    }
//
//    @Test()
//    public void setInternalOrExternalProperty_external(@Mocked DomibusPropertyManagerExt manager) {
//
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = false;
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            result = manager;
//        }};
//
//        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.setInternalPropertyValue(domain, propertyName, propertyValue, true);
//            times = 0;
//            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
//        }};
//    }
//
//    @Test(expected = DomibusPropertyException.class)
//    public void setInternalOrExternalProperty_external_error(@Mocked DomibusPropertyManagerExt manager) {
//
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            globalPropertyMetadataManager.getPropertyMetadata(propertyName);
//            result = propMeta;
//            propMeta.isStoredGlobally();
//            result = false;
//            globalPropertyMetadataManager.getManagerForProperty(propertyName);
//            result = null;
//        }};
//
//        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(domain, propertyName, propertyValue, true);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.setExternalPropertyValue(domain, propertyName, propertyValue, true, (DomibusPropertyManagerExt) any);
//            times = 0;
//        }};
//    }
//
//    @Test
//    public void getPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
//        String propertyName = "prop1";
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class});
//            returns(true, false);
//            domibusPropertyProviderDispatcher.getCurrentDomainCode();
//            result = "default";
//        }};
//
//        domibusPropertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
//        new Verifications() {{
//            propertyManager.getKnownPropertyValue(propertyName);
//        }};
//
//        domibusPropertyProviderDispatcher.getExternalModulePropertyValue(propertyManager, propertyName);
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.getCurrentDomainCode();
//            propertyManager.getKnownPropertyValue("default", propertyName);
//        }};
//    }
//
//    @Test
//    public void setPropertyValue(@Mocked DomibusPropertyManagerExt propertyManager) {
//        String propertyName = "prop1";
//        String proertyValue = "propVal1";
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class});
//            returns(true, false);
//            domibusPropertyProviderDispatcher.getCurrentDomainCode();
//            result = "default";
//        }};
//
//        domibusPropertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
//        new Verifications() {{
//            propertyManager.setKnownPropertyValue(propertyName, proertyValue);
//        }};
//
//        domibusPropertyProviderDispatcher.setExternalModulePropertyValue(propertyManager, propertyName, proertyValue);
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.getCurrentDomainCode();
//            propertyManager.setKnownPropertyValue("default", propertyName, proertyValue);
//        }};
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void setPropertyValue_tooLong() {
//        int limit = 100;
//        String propertyToTest = DOMIBUS_UI_TITLE_NAME;
//        String longValue = StringUtils.repeat("A", limit + 1);
//        new Expectations() {{
//            domibusPropertyProvider.getIntegerProperty(DOMIBUS_PROPERTY_LENGTH_MAX);
//            result = limit;
//        }};
//
//        domibusPropertyProviderDispatcher.setInternalOrExternalProperty(null, propertyToTest, longValue, false);
//    }
//
//    @Test
//    public void getExternalPropertyValue_noDomain(@Mocked DomibusPropertyManagerExt manager) {
//        String propertyName = "propertyName";
//        String propertyValue = "propertyValue";
//
//        new Expectations(domibusPropertyProviderDispatcher) {{
//            domibusPropertyProviderDispatcher.getExternalModulePropertyValue(manager, propertyName);
//            result = propertyValue;
//        }};
//
//        String result = domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, null, manager);
//
//        assertEquals(propertyValue, result);
//
//        new Verifications() {{
//            domibusPropertyProviderDispatcher.getExternalModulePropertyValue(manager, propertyName);
//            times = 1;
//        }};
//    }
//
//    @Test
//    public void getExternalPropertyValue_domain(@Mocked DomibusPropertyManagerExt manager, @Mocked Domain domain) {
//        String propertyName = "propertyName";
//        String propertyValue = "propertyValue";
//
//        new Expectations() {{
//            manager.getKnownPropertyValue(anyString, propertyName);
//            result = propertyValue;
//        }};
//
//        String result = domibusPropertyProviderDispatcher.getExternalPropertyValue(propertyName, domain, manager);
//        assertEquals(propertyValue, result);
//
//        new Verifications() {{
//            manager.getKnownPropertyValue(domain.getCode(), propertyName);
//            times = 1;
//        }};
//    }
//
//}