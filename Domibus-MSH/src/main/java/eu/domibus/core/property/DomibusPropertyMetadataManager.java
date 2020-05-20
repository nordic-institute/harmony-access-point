package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class involved in managing the property metadata of core or external property managers ( metadata integrator)
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Service
public class DomibusPropertyMetadataManager {
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyMetadataManager.class);

    @Autowired
    @Lazy
    private List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers;

    @Autowired
    @Lazy
    private List<DomibusPropertyManagerExt> extPropertyManagers;

    @Autowired
    protected DomainCoreConverter domainConverter;

    private Map<String, DomibusPropertyMetadata> allPropertyMetadataMap;
    private Map<String, DomibusPropertyMetadata> internlPropertyMetadataMap;

    private volatile boolean internalPropertiesLoaded = false;
    private volatile boolean externalPropertiesLoaded = false;
    private final Object propertyMetadataMapLock = new Object();

    public Map<String, DomibusPropertyMetadata> getAllProperties() {
        loadExternalPropertiesIfNeeded();
        return allPropertyMetadataMap;
    }

    /**
     * Returns the metadata for a given propertyName,
     * by interrogating all property managers known to Domibus in order to find it.
     * If not found, it assumes it is a global property and it creates the corresponding metadata on-the-fly.
     *
     * @param propertyName
     * @return DomibusPropertyMetadata
     */
    public DomibusPropertyMetadata getPropertyMetadata(String propertyName) {
        initializeIfNeeded(propertyName);

        DomibusPropertyMetadata prop = allPropertyMetadataMap.get(propertyName);
        if (prop != null) {
            LOGGER.trace("Found property [{}], returning its metadata.", propertyName);
            return prop;
        }

        // try to see if it is a compose-able property, i.e. propertyName+suffix
        Optional<DomibusPropertyMetadata> propMeta = allPropertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
        if (propMeta.isPresent()) {
            LOGGER.trace("Found compose-able property [{}], returning its metadata.", propertyName);
            DomibusPropertyMetadata meta = propMeta.get();
            // metadata name is a prefix of propertyName so we set the whole property name here to be correctly used down the stream. Not beautiful
            meta.setName(propertyName);
            return meta;
        }

        // if still not found, initialize metadata on-the-fly
        LOGGER.warn("Creating on-the-fly global metadata for unknown property: [{}]", propertyName); //TODO: lower log level after testing
        synchronized (propertyMetadataMapLock) {
            DomibusPropertyMetadata newProp = DomibusPropertyMetadata.getReadOnlyGlobalProperty(propertyName, Module.UNKNOWN);
            allPropertyMetadataMap.put(propertyName, newProp);
            internlPropertyMetadataMap.put(propertyName, prop);
            return newProp;
        }
    }

    /**
     * Initializes the metadata map.
     * Initially, during the bean creation stage, only a few domibus-core properties are needed;
     * later on, the properties from all managers will be added to the map.
     */
    protected void initializeIfNeeded(String propertyName) {
        // add domibus-core and specific server  properties first, to avoid infinite loop of bean creation (due to DB properties)
        if (allPropertyMetadataMap == null) {
            synchronized (propertyMetadataMapLock) {
                if (!internalPropertiesLoaded) { // double-check locking
                    LOGGER.trace("Initializing core properties");

                    loadInternalProperties();

                    LOGGER.trace("Finished loading property metadata for internal property managers.");
                    internalPropertiesLoaded = true;
                }
            }
        }
        if (allPropertyMetadataMap.containsKey(propertyName)) {
            LOGGER.trace("Found property metadata [{}] in core properties. Returning.", propertyName);
            return;
        }

        loadExternalPropertiesIfNeeded();
    }

    // load external properties (i.e. plugin properties and extension properties) the first time one of them is needed
    private void loadExternalPropertiesIfNeeded() {
        if (!externalPropertiesLoaded) {
            synchronized (propertyMetadataMapLock) {
                if (!externalPropertiesLoaded) { // double-check locking
                    LOGGER.trace("Initializing external properties");

                    loadExternalProperties();

                    externalPropertiesLoaded = true;
                    LOGGER.trace("Finished loading property metadata for external property managers.");
                }
            }
        }
    }

    protected void loadInternalProperties() {
        allPropertyMetadataMap = new HashMap<>();
        internlPropertyMetadataMap = new HashMap<>();

        propertyMetadataManagers.stream()
                .forEach(propertyManager -> loadProperties(propertyManager, propertyManager.toString()));
    }

    protected void loadExternalProperties() {
        extPropertyManagers.stream().forEach(this::loadExternalProperties);
    }

    protected void loadExternalProperties(DomibusPropertyManagerExt propertyManager) {
        LOGGER.trace("Loading property metadata for [{}] external property manager.", propertyManager);
        for (Map.Entry<String, DomibusPropertyMetadataDTO> entry : propertyManager.getKnownProperties().entrySet()) {
            DomibusPropertyMetadataDTO extProp = entry.getValue();
            DomibusPropertyMetadata domibusProp = domainConverter.convert(extProp, DomibusPropertyMetadata.class);
            domibusProp.setType(extProp.getType());

            allPropertyMetadataMap.put(entry.getKey(), domibusProp);
        }
    }

    protected void loadProperties(DomibusPropertyMetadataManagerSPI propertyManager, String managerName) {
        LOGGER.trace("Loading property metadata for [{}] property manager.", managerName);
        for (Map.Entry<String, DomibusPropertyMetadata> entry : propertyManager.getKnownProperties().entrySet()) {
            DomibusPropertyMetadata prop = entry.getValue();
            allPropertyMetadataMap.put(entry.getKey(), prop);
            internlPropertyMetadataMap.put(entry.getKey(), prop);
        }
    }

    public DomibusPropertyManagerExt getManagerForProperty(String propertyName) {
        // save prop manager on property itself??
        initializeIfNeeded(propertyName);
        if (internlPropertyMetadataMap.containsKey(propertyName)) {
            //core property, no external manager involved
            return null;
        }

        Optional<DomibusPropertyManagerExt> found = extPropertyManagers.stream()
                .filter(manager -> manager.hasKnownProperty(propertyName)).findFirst();

        if (found.isPresent()) {
            return found.get();
        }
        return null;
    }
}
