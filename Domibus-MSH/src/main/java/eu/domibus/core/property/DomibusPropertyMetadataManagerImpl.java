package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DomibusPropertyMetadataManagerImpl {
    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyMetadataManagerImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private List<DomibusPropertyMetadataManager> propertyMetadataManagers;

    @Autowired
    @Lazy
    private List<DomibusPropertyManagerExt> extPropertyManagers;

    private Map<String, DomibusPropertyMetadata> propertyMetadataMap;

    private volatile boolean internalPropertiesLoaded = false;
    private volatile boolean externalPropertiesLoaded = false;
    private final Object propertyMetadataMapLock = new Object();

    public Map<String, DomibusPropertyMetadata> getAllProperties() {
        return propertyMetadataMap;
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

        DomibusPropertyMetadata prop = propertyMetadataMap.get(propertyName);
        if (prop != null) {
            LOGGER.trace("Found property [{}], returning its metadata.", propertyName);
            return prop;
        }

        // try to see if it is a compose-able property, i.e. propertyName+suffix
        Optional<DomibusPropertyMetadata> propMeta = propertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
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
            propertyMetadataMap.put(propertyName, newProp);
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
        if (propertyMetadataMap == null) {
            synchronized (propertyMetadataMapLock) {
                if (!internalPropertiesLoaded) { // double-check locking
                    LOGGER.trace("Initializing core properties");

                    propertyMetadataMap = new HashMap<>();
                    loadInternalProperties();

                    LOGGER.trace("Finished loading property metadata for internal property managers.");
                    internalPropertiesLoaded = true;
                }
            }
        }
        if (propertyMetadataMap.containsKey(propertyName)) {
            LOGGER.trace("Found property metadata [{}] in core properties. Returning.", propertyName);
            return;
        }

        // load external properties (i.e. plugin properties and extension properties) the first time one of them is needed
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
            //use converter
            DomibusPropertyMetadata domibusProp = new DomibusPropertyMetadata(extProp.getName(), extProp.getModule(), extProp.isWritable(), extProp.getUsage(), extProp.isWithFallback(),
                    extProp.isClusterAware(), extProp.isEncrypted(), extProp.isComposable());
            domibusProp.setType(extProp.getType());

            propertyMetadataMap.put(entry.getKey(), domibusProp);
        }
    }

    protected void loadProperties(DomibusPropertyMetadataManager propertyManager, String managerName) {
        LOGGER.trace("Loading property metadata for [{}] property manager.", managerName);
        for (Map.Entry<String, DomibusPropertyMetadata> entry : propertyManager.getKnownProperties().entrySet()) {
            DomibusPropertyMetadata prop = entry.getValue();
            propertyMetadataMap.put(entry.getKey(), prop);
        }
    }
}
