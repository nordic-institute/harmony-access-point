//package eu.domibus.core.property;
//
//import eu.domibus.api.property.DomibusPropertyManager;
//import eu.domibus.api.property.DomibusPropertyMetadata;
//import mockit.*;
//import mockit.integration.junit4.JMockit;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.context.ApplicationContext;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_UI_TITLE_NAME;
//
//@RunWith(JMockit.class)
//public class DomibusPropertyMetadataManagerImplTest {
//
//    @Tested
//    DomibusPropertyMetadataManagerImpl domibusPropertyMetadataManager;
//
//    @Injectable
//    ApplicationContext applicationContext;
//
//    @Test
//    public void getKnownProperties_nonExisting() {
//        Map<String, DomibusPropertyMetadata> props = domibusPropertyMetadataManager.getKnownProperties();
//        DomibusPropertyMetadata actual = props.get("non_existing");
//
//        Assert.assertEquals(null, actual);
//    }
//
//    @Test
//    public void getKnownProperties() {
//        Map<String, DomibusPropertyMetadata> props = domibusPropertyMetadataManager.getKnownProperties();
//        DomibusPropertyMetadata actual = props.get(DOMIBUS_UI_TITLE_NAME);
//
//        Assert.assertEquals(DOMIBUS_UI_TITLE_NAME, actual.getName());
//        Assert.assertEquals(actual.getUsage(), DomibusPropertyMetadata.Usage.DOMAIN);
//        Assert.assertTrue(actual.isWithFallback());
//    }
//
//    @Test
//    public void hasKnownProperty_nonExisting() {
//        boolean actual = domibusPropertyMetadataManager.hasKnownProperty("non_existing");
//
//        Assert.assertEquals(false, actual);
//    }
//
//    @Test
//    public void hasKnownProperty() {
//        boolean actual = domibusPropertyMetadataManager.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
//
//        Assert.assertTrue(actual);
//    }
//
//    @Test
//    public void getPropertyMetadataNotNullPropertyTest(@Injectable DomibusPropertyMetadata prop,
//                                                       @Injectable Map<String, DomibusPropertyMetadata> propertyMetadataMap) {
//        String propertyName = DOMIBUS_UI_TITLE_NAME;
//        new Expectations(domibusPropertyMetadataManager) {{
//            domibusPropertyMetadataManager.initializeIfNeeded(propertyName);
//            times = 1;
//            propertyMetadataMap.get(anyString);
//            result = prop;
//        }};
//        domibusPropertyMetadataManager.getPropertyMetadata(propertyName);
//        new Verifications() {{
//            propertyMetadataMap.get(anyString);
//            times = 1;
//            propertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
//            times = 0;
//        }};
//    }
//
//    @Test
//    public void getPropertyMetadataWithComposeablePropertyTest(@Injectable DomibusPropertyMetadata prop,
//                                                               @Injectable Map<String, DomibusPropertyMetadata> propertyMetadataMap,
//                                                               @Injectable Optional<DomibusPropertyMetadata> propMeta) {
//        String propertyName = DOMIBUS_UI_TITLE_NAME;
//        propertyMetadataMap.put(propertyName, prop);
//        new Expectations(domibusPropertyMetadataManager) {{
//            domibusPropertyMetadataManager.initializeIfNeeded(propertyName);
//            times = 1;
//            propertyMetadataMap.get(anyString);
//            result = null;
//        }};
//        domibusPropertyMetadataManager.getPropertyMetadata(propertyName);
//        new Verifications() {{
//            propertyMetadataMap.get(anyString);
//            times = 1;
//            propertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
//            times = 1;
//            propMeta.get();
//            times = 0;
//        }};
//    }
//
//    @Test
//    public void initializeIfNeededTest(@Injectable DomibusPropertyMetadata prop,
//                                       @Injectable Map<String, DomibusPropertyMetadata> propertyMetadataMap,
//                                       @Injectable Optional<DomibusPropertyMetadata> propMeta) {
//        String propertyName = DOMIBUS_UI_TITLE_NAME;
//        propertyMetadataMap.put(propertyName, prop);
//        new Expectations(domibusPropertyMetadataManager) {{
//            domibusPropertyMetadataManager.loadInternalProperties();
//            times = 0;
//        }};
//        domibusPropertyMetadataManager.initializeIfNeeded(propertyName);
//        new Verifications() {{
//            domibusPropertyMetadataManager.loadExternalProperties();
//            times = 1;
//        }};
//    }
//
//    @Test
//    public void initializeIfNeededNullPropertyMetadataMapTest(@Injectable DomibusPropertyMetadata prop,
//                                                              @Injectable Optional<DomibusPropertyMetadata> propMeta) {
//        String propertyName = DOMIBUS_UI_TITLE_NAME;
//        new Expectations(domibusPropertyMetadataManager) {{
//            Map<String, DomibusPropertyMetadata> propertyMetadataMap = null;
//            boolean externalPropertiesLoaded = true;
//            domibusPropertyMetadataManager.loadInternalProperties();
//            times = 1;
//        }};
//        domibusPropertyMetadataManager.initializeIfNeeded(propertyName);
//        new Verifications() {{
//            domibusPropertyMetadataManager.loadExternalProperties();
//            times = 1;
//        }};
//    }
//
//    @Test
//    public void loadInternalPropertiesTest(@Injectable DomibusPropertyMetadata prop,
//                                           @Injectable Optional<DomibusPropertyMetadata> propMeta,
//                                           @Mocked DomibusPropertyManager propertyManager) {
//        final String MSH_PROPERTY_MANAGER = "mshPropertyManager";
//
//        new Expectations(domibusPropertyMetadataManager) {{
//            domibusPropertyMetadataManager.loadProperties(domibusPropertyMetadataManager, MSH_PROPERTY_MANAGER);
//            times = 1;
//            applicationContext.getBeanNamesForType(DomibusPropertyManager.class);
//            times = 1;
//        }};
//
//        domibusPropertyMetadataManager.loadInternalProperties();
//
//        new Verifications() {{
//            domibusPropertyMetadataManager.loadExternalProperties();
//            times = 0;
//        }};
//    }
//}