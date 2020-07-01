package eu.domibus.core.property;

import eu.domibus.api.property.DomibusPropertyException;
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
public class GlobalPropertyMetadataManagerImpl implements GlobalPropertyMetadataManager {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(GlobalPropertyMetadataManagerImpl.class);

    @Autowired
    protected List<DomibusPropertyMetadataManagerSPI> propertyMetadataManagers;

    @Autowired
    @Lazy
    protected List<DomibusPropertyManagerExt> extPropertyManagers;

    @Autowired
    protected DomainCoreConverter domainConverter;

    protected Map<String, DomibusPropertyMetadata> allPropertyMetadataMap;
    protected Map<String, DomibusPropertyMetadata> internalPropertyMetadataMap;

    private volatile boolean internalPropertiesLoaded = false;
    private volatile boolean externalPropertiesLoaded = false;
    private final Object propertyMetadataMapLock = new Object();

    @Override
    public Map<String, DomibusPropertyMetadata> getAllProperties() {
        loadExternalPropertiesIfNeeded();
        return allPropertyMetadataMap;
    }

    @Override
    public DomibusPropertyMetadata getPropertyMetadata(String propertyName) {
        loadPropertiesIfNotFound(propertyName);

        DomibusPropertyMetadata prop = allPropertyMetadataMap.get(propertyName);
        if (prop != null) {
            LOG.trace("Found property [{}], returning its metadata.", propertyName);
            return prop;
        }

        // try to see if it is a compose-able property, i.e. propertyName+suffix
        Optional<DomibusPropertyMetadata> propMeta = allPropertyMetadataMap.values().stream().filter(p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
        if (propMeta.isPresent()) {
            LOG.trace("Found compose-able property [{}], returning its metadata.", propertyName);
            DomibusPropertyMetadata meta2 = addMetadataForAPrefixedProperty(propertyName, propMeta.get());
            return meta2;
        }

        // if still not found, initialize metadata on-the-fly
        LOG.info("Creating on-the-fly global metadata for unknown property: [{}]", propertyName);
        synchronized (propertyMetadataMapLock) {
            DomibusPropertyMetadata newProp = DomibusPropertyMetadata.getReadOnlyGlobalProperty(propertyName, Module.UNKNOWN);
            allPropertyMetadataMap.put(propertyName, newProp);
            internalPropertyMetadataMap.put(propertyName, newProp);
            return newProp;
        }
    }

    protected boolean hasProperty(Map<String, DomibusPropertyMetadata> map, String propertyName) {
        if (map.containsKey(propertyName)) {
            return true;
        }

        try {
            Optional<DomibusPropertyMetadata> propMeta = map.values().stream().filter(
                    p -> p.isComposable() && propertyName.startsWith(p.getName())).findAny();
            return propMeta.isPresent();
        } catch (Exception ex) {
            LOG.error("Could not find metadata of [{}] property in property map [{}]", propertyName, map, ex);
            return false;
        }
    }

    @Override
    public DomibusPropertyManagerExt getManagerForProperty(String propertyName) throws DomibusPropertyException {
        loadPropertiesIfNotFound(propertyName);
        if (hasProperty(internalPropertyMetadataMap, propertyName)) {
            //core property, no external manager involved
            LOG.trace("Property [{}] is internal, returning null as the manager.", propertyName);
            return null;
        }

        Optional<DomibusPropertyManagerExt> found = extPropertyManagers.stream()
                .filter(manager -> manager.hasKnownProperty(propertyName)).findFirst();

        if (found.isPresent()) {
            LOG.trace("Property [{}] is external, returning its manager [{}].", propertyName, found.get());
            return found.get();
        }

        throw new DomibusPropertyException("Property" + propertyName + "could not be found anywhere.");
    }

    /**
     * Initializes the metadata map.
     * Initially, during the bean creation stage, only a few domibus-core properties are needed;
     * later on, the properties from all managers will be added to the map.
     */
    protected void loadPropertiesIfNotFound(String propertyName) {
        // add domibus-core and specific server  properties first, to avoid infinite loop of bean creation (due to DB properties)
        if (internalPropertyMetadataMap == null) {
            synchronized (propertyMetadataMapLock) {
                if (!internalPropertiesLoaded) { // double-check locking
                    LOG.trace("Initializing core properties");

                    loadInternalProperties();

                    LOG.trace("Finished loading property metadata for internal property managers.");
                    internalPropertiesLoaded = true;
                }
            }
        }
        if (hasProperty(allPropertyMetadataMap, propertyName)) {
            LOG.trace("Found property metadata [{}] in core properties. Returning.", propertyName);
            return;
        }

        loadExternalPropertiesIfNeeded();
    }

    // load external properties (i.e. plugin properties and extension properties) the first time one of them is needed
    protected void loadExternalPropertiesIfNeeded() {
        if (!externalPropertiesLoaded) {
            synchronized (propertyMetadataMapLock) {
                if (!externalPropertiesLoaded) { // double-check locking
                    LOG.trace("Initializing external properties");

                    loadExternalProperties();

                    externalPropertiesLoaded = true;
                    LOG.trace("Finished loading property metadata for external property managers.");
                }
            }
        }
    }

    protected void loadInternalProperties() {
        allPropertyMetadataMap = new HashMap<>();
        internalPropertyMetadataMap = new HashMap<>();

        propertyMetadataManagers.forEach(propertyManager -> loadProperties(propertyManager, propertyManager.toString()));
    }

    protected void loadExternalProperties() {
        extPropertyManagers.forEach(this::loadExternalProperties);
    }

    protected void loadExternalProperties(DomibusPropertyManagerExt propertyManager) {
        LOG.trace("Loading property metadata for [{}] external property manager.", propertyManager);
        for (Map.Entry<String, DomibusPropertyMetadataDTO> entry : propertyManager.getKnownProperties().entrySet()) {
            DomibusPropertyMetadataDTO extProp = entry.getValue();
            DomibusPropertyMetadata domibusProp = domainConverter.convert(extProp, DomibusPropertyMetadata.class);
            allPropertyMetadataMap.put(entry.getKey(), domibusProp);
        }
    }

    protected void loadProperties(DomibusPropertyMetadataManagerSPI propertyManager, String managerName) {
        LOG.trace("Loading property metadata for [{}] property manager.", managerName);
        for (Map.Entry<String, DomibusPropertyMetadata> entry : propertyManager.getKnownProperties().entrySet()) {
            DomibusPropertyMetadata prop = entry.getValue();
            allPropertyMetadataMap.put(entry.getKey(), prop);
            internalPropertyMetadataMap.put(entry.getKey(), prop);
        }
    }

    protected DomibusPropertyMetadata addMetadataForAPrefixedProperty(String propertyName, DomibusPropertyMetadata propMeta) {
        //make a clone
        DomibusPropertyMetadata newPropMeta = domainConverter.convert(propMeta, DomibusPropertyMetadata.class);
        // metadata name is a prefix of propertyName so we set the whole property name here to be correctly used down the stream. Not beautiful
        newPropMeta.setName(propertyName);

        allPropertyMetadataMap.put(propertyName, newPropMeta);
        internalPropertyMetadataMap.put(propertyName, newPropMeta);

        return newPropMeta;
    }

}
