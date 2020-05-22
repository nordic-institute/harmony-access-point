package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_TITLE_NAME;

@RunWith(JMockit.class)
public class GlobalPropertyMetadataManagerImplTest {

    @Tested
    GlobalPropertyMetadataManagerImpl globalPropertyMetadataManager;

    @Injectable
    List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers;

    @Injectable
    List<DomibusPropertyManagerExt> domibusPropertyManagerExts;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Test
    public void getPropertyMetadataNotNullPropertyTest(@Injectable DomibusPropertyMetadata prop,
                                                       @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.initializeIfNeeded(propertyName);
            times = 1;
            allPropertyMetadataMap.get(anyString);
            result = prop;
        }};
        globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        new Verifications() {{
            allPropertyMetadataMap.get(anyString);
            times = 1;
            allPropertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
            times = 0;
        }};
    }

    @Test
    public void getPropertyMetadataWithComposeablePropertyTest(@Injectable DomibusPropertyMetadata prop,
                                                               @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap,
                                                               @Injectable Map<String, DomibusPropertyMetadata> internlPropertyMetadataMap,
                                                               @Injectable Optional<DomibusPropertyMetadata> propMeta) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        allPropertyMetadataMap.put(propertyName, prop);
        internlPropertyMetadataMap.put(propertyName, prop);
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.initializeIfNeeded(propertyName);
            times = 1;
            allPropertyMetadataMap.get(anyString);
            result = null;
        }};
        globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        new Verifications() {{
            allPropertyMetadataMap.get(anyString);
            times = 1;
            allPropertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
            times = 1;
            propMeta.get();
            times = 0;
        }};
    }

    @Test
    public void initializeIfNeededTest(@Injectable DomibusPropertyMetadata prop,
                                       @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap,
                                       @Injectable Map<String, DomibusPropertyMetadata> internlPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        allPropertyMetadataMap.put(propertyName, prop);
        internlPropertyMetadataMap.put(propertyName, prop);
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadInternalProperties();
            times = 0;
        }};
        globalPropertyMetadataManager.initializeIfNeeded(propertyName);
        new Verifications() {{
            globalPropertyMetadataManager.loadExternalProperties();
            times = 1;
        }};
    }

    @Test
    public void initializeIfNeededNullPropertyMetadataMapTest(@Injectable DomibusPropertyMetadata prop, @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        new Expectations(globalPropertyMetadataManager) {{
            Map<String, DomibusPropertyMetadata> allPropertyMetadataMap = null;
            boolean externalPropertiesLoaded = true;
        }};
        globalPropertyMetadataManager.initializeIfNeeded(propertyName);
        new Verifications() {{
            globalPropertyMetadataManager.loadExternalProperties();
            times = 1;
        }};
    }

    //
//    @Test
//    public void getManagerForProperty() {
//        new Expectations(configurationPropertyService) {{
//            propertyManager1.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
//            result = false;
//            propertyManager2.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
//            result = true;
//        }};
//
//        DomibusPropertyManagerExt manager = configurationPropertyService.getManagerForProperty(DOMIBUS_UI_TITLE_NAME);
//
//        Assert.assertEquals(propertyManager2, manager);
//    }

//    @Test
//    public void loadInternalPropertiesTest() {
//        final String MSH_PROPERTY_MANAGER = "mshPropertyManager";
//
//        new Expectations(globalPropertyMetadataManager) {{
////            globalPropertyMetadataManager.loadProperties(globalPropertyMetadataManager, MSH_PROPERTY_MANAGER);
////            times = 1;
//        }};
//
//        globalPropertyMetadataManager.loadInternalProperties();
//
//        new Verifications() {{
//            globalPropertyMetadataManager.loadExternalProperties();
//            times = 0;
//        }};
//    }
}
