package eu.domibus.core.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

@RunWith(JMockit.class)
public class GlobalPropertyMetadataManagerImplTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(GlobalPropertyMetadataManagerImplTest.class);

    @Tested
    GlobalPropertyMetadataManagerImpl globalPropertyMetadataManager;

    @Injectable
    List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers;

    @Injectable
    List<DomibusPropertyManagerExt> domibusPropertyManagerExts;

    @Injectable
    DomainCoreConverter domainConverter;

    @Injectable
    private List<DomibusPropertyManagerExt> extPropertyManagers;

    @Mocked
    @Spy
    private DomibusPropertyManagerExt propertyManager1;

    @Mocked
    private DomibusPropertyManagerExt propertyManager2;

    Map<String, DomibusPropertyMetadataDTO> props1;
    Map<String, DomibusPropertyMetadata> props2;
    String domainCode = "domain1";
    Domain domain = new Domain(domainCode, "DomainName1");

    @Before
    public void setUp() {
        props1 = Arrays.stream(new DomibusPropertyMetadataDTO[]{
                new DomibusPropertyMetadataDTO(DOMIBUS_UI_TITLE_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_UI_REPLICATION_ENABLED, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, DomibusPropertyMetadataDTO.Usage.DOMAIN, false),
                new DomibusPropertyMetadataDTO(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        props2 = Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata(DOMIBUS_UI_SUPPORT_TEAM_NAME, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
                new DomibusPropertyMetadata(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, DomibusPropertyMetadataDTO.Usage.DOMAIN, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        extPropertyManagers = Arrays.asList(propertyManager1, propertyManager2);
    }

    @Test
    public void getPropertyMetadataNotNullPropertyTest(@Injectable DomibusPropertyMetadata prop,
                                                       @Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                                       @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);
            allPropertyMetadataMap.get(propertyName);
            result = prop;
        }};
        globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        new Verifications() {{
            allPropertyMetadataMap.get(propertyName);
            times = 1;
            allPropertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
            times = 0;
        }};
    }

    @Test
    public void getPropertyMetadataWithComposeablePropertyTest(@Injectable DomibusPropertyMetadata propMeta,
                                                               @Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                                               @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);
            times = 1;
            allPropertyMetadataMap.get(anyString);
            result = null;
            globalPropertyMetadataManager.getComposableProperty(allPropertyMetadataMap, propertyName);
            result = propMeta;
            domainConverter.convert(propMeta, DomibusPropertyMetadata.class);
            result = propMeta;
        }};
        DomibusPropertyMetadata meta = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        Assert.assertNotNull(meta);
        new Verifications() {{
            allPropertyMetadataMap.get(anyString);
            times = 1;
            propMeta.setName(propertyName);
            times = 1;
        }};
    }

    @Test
    public void initializeIfNeededTest_loadExternalProps(@Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                                         @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadInternalProperties();
            times = 0;
        }};

        globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);

        new Verifications() {{
            globalPropertyMetadataManager.loadExternalProperties();
            times = 1;
        }};
    }

    @Test
    public void initializeIfNeededTest_loadInternalProps(@Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadInternalProperties();
            globalPropertyMetadataManager.hasProperty(allPropertyMetadataMap, propertyName);
            result = true;
        }};

        globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);

        new Verifications() {{
            globalPropertyMetadataManager.loadInternalProperties();
            times = 1;
            globalPropertyMetadataManager.loadExternalPropertiesIfNeeded();
            times = 0;
        }};
    }

    @Test
    public void initializeIfNeededNullPropertyMetadataMapTest(@Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                                              @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.hasProperty(allPropertyMetadataMap, propertyName);
            result = false;
        }};
        globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);
        new Verifications() {{
            globalPropertyMetadataManager.loadExternalProperties();
            times = 1;
        }};
    }

    @Test
    public void getManagerForProperty_internal(@Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                               @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);

            globalPropertyMetadataManager.hasProperty(internalPropertyMetadataMap, propertyName);
            result = true;
        }};

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);

        Assert.assertEquals(null, manager);
    }

    @Test
    public void getManagerForProperty_external(@Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                               @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;

        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);

            globalPropertyMetadataManager.hasProperty(internalPropertyMetadataMap, propertyName);
            result = false;
            propertyManager1.hasKnownProperty(propertyName);
            result = false;
            propertyManager2.hasKnownProperty(propertyName);
            result = true;
        }};

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);

        Assert.assertEquals(propertyManager2, manager);
    }

    @Test(expected = DomibusPropertyException.class)
    public void getManagerForProperty_not_found(@Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap,
                                                @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap) {
        String propertyName = DOMIBUS_UI_TITLE_NAME;
        new Expectations(globalPropertyMetadataManager) {{
            globalPropertyMetadataManager.loadPropertiesIfNotFound(propertyName);

            globalPropertyMetadataManager.hasProperty(internalPropertyMetadataMap, propertyName);
            result = false;
            propertyManager1.hasKnownProperty(propertyName);
            result = false;
            propertyManager2.hasKnownProperty(propertyName);
            result = false;
        }};

        DomibusPropertyManagerExt manager = globalPropertyMetadataManager.getManagerForProperty(propertyName);
    }

    @Test
    public void loadExternalPropertiesIfNeeded() {
        globalPropertyMetadataManager.loadExternalPropertiesIfNeeded();
        globalPropertyMetadataManager.loadExternalPropertiesIfNeeded();
        new Verifications() {{
            globalPropertyMetadataManager.loadExternalProperties();
            times = 1;
        }};
    }

    @Test
    public void loadExternalPropertiesTest(@Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap,
                                           @Injectable DomibusPropertyMetadata propMeta) {
        new Expectations(globalPropertyMetadataManager) {{
            propertyManager1.getKnownProperties();
            result = props1;
            domainConverter.convert((DomibusPropertyMetadataDTO) any, DomibusPropertyMetadata.class);
            result = propMeta;
        }};

        globalPropertyMetadataManager.loadExternalProperties(propertyManager1);

        new Verifications() {{
            domainConverter.convert((DomibusPropertyMetadataDTO) any, DomibusPropertyMetadata.class);
            times = props1.size();
            allPropertyMetadataMap.put(anyString, propMeta);
            times = props1.size();
        }};
    }

    @Test
    public void loadPropertiesTest(@Mocked DomibusPropertyMetadataManagerSPI propertyManager,
                                   @Injectable Map<String, DomibusPropertyMetadata> allPropertyMetadataMap,
                                   @Injectable Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap) {
        new Expectations(globalPropertyMetadataManager) {{
            propertyManager.getKnownProperties();
            result = props2;
        }};

        globalPropertyMetadataManager.loadProperties(propertyManager, "tomcatManager");

        new Verifications() {{
            allPropertyMetadataMap.put(anyString, (DomibusPropertyMetadata) any);
            times = props2.size();
            internalPropertyMetadataMap.put(anyString, (DomibusPropertyMetadata) any);
            times = props2.size();
        }};
    }

    @Test
    public void testSynchronizedBlocksWhenAddingPropertiesOnTheFly() {
        new Expectations(globalPropertyMetadataManager) {{
            domainConverter.convert(any, DomibusPropertyMetadata.class);
            result = DomibusPropertyMetadata.getReadOnlyGlobalProperty("dummy");
        }};

        // When multiple properties are added to the properties map at the same time,
        // concurrent access to the map may result in ConcurrentModificationException.
        // This test verifies that this situation does not occur (anymore).

        ExecutorService ex = Executors.newFixedThreadPool(3);

        Map<String, Future<DomibusPropertyMetadata>> futures = new HashMap();
        for (int i = 0; i < 100; i++) {
            String newPropertyName = "propertyName" + new Random().nextInt();
            Future<DomibusPropertyMetadata> get = ex.submit(() -> globalPropertyMetadataManager.getPropertyMetadata(newPropertyName));
            futures.put(newPropertyName, get);
        }
        futures.forEach((newPropertyName, future) -> {
            try {
                DomibusPropertyMetadata metadata = future.get();
                Assert.assertNotNull(metadata);
            } catch (InterruptedException e) {
                LOG.debug("Interrupted", e);
            } catch (ExecutionException e) {
                LOG.error("Unexpected error", e);
                Assert.fail(e.getClass().getSimpleName() + " caught");
            }
        });
    }

}
