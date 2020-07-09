package eu.domibus.core.property;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.MetricsAspect;
import eu.domibus.core.metrics.Timer;
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

import static com.codahale.metrics.MetricRegistry.name;

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

   /* @Autowired
    private MetricRegistry metricRegistry;*/

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
      /*  com.codahale.metrics.Counter methodCounter = metricRegistry.counter(name(GlobalPropertyMetadataManagerImpl.class, "getPropertyMetadata", "_counter"));
        methodCounter.inc();
        com.codahale.metrics.Timer surrounding = metricRegistry.timer(name(GlobalPropertyMetadataManagerImpl.class, "getPropertyMetadata", "_timer"));
        com.codahale.metrics.Timer.Context surroundingContext = surrounding.time();
        try {*/
            loadPropertiesIfNotFound(propertyName);

            DomibusPropertyMetadata prop = allPropertyMetadataMap.get(propertyName);
            if (prop != null) {
                LOG.trace("Found property [{}], returning its metadata.", propertyName);
                return prop;
            }

            synchronized (propertyMetadataMapLock) {
                LOG.trace("Acquired lock to search new property: [{}]", propertyName);

                // try to see if it is a compose-able property, i.e. propertyName+suffix
                DomibusPropertyMetadata propMeta = getComposableProperty(allPropertyMetadataMap, propertyName);
                if (propMeta != null) {
                    LOG.trace("Found compose-able property [{}], returning its metadata.", propertyName);
                    return clonePropertyMetadata(propertyName, propMeta);
                }

                // if still not found, initialize metadata on-the-fly
                LOG.info("Creating on-the-fly global metadata for unknown property: [{}]", propertyName);
                DomibusPropertyMetadata newProp = DomibusPropertyMetadata.getReadOnlyGlobalProperty(propertyName, Module.UNKNOWN);
                return clonePropertyMetadata(propertyName, newProp);
            }
      /*  } finally {
            methodCounter.dec();
            surroundingContext.stop();
        }*/

    }

    protected boolean hasProperty(Map<String, DomibusPropertyMetadata> map, String propertyName) {
        if (map.containsKey(propertyName)) {
            return true;
        }

        synchronized (propertyMetadataMapLock) {
            LOG.trace("Acquired lock to search new property: [{}]", propertyName);
            return getComposableProperty(map, propertyName) != null;
        }
    }

    protected DomibusPropertyMetadata getComposableProperty(Map<String, DomibusPropertyMetadata> map, String propertyName) {
        return map.values().stream().filter(
                propertyMetadata -> propertyMetadata.isComposable() && propertyName.startsWith(propertyMetadata.getName()))
                .findAny()
                .orElse(null);
    }

    @Override
    @Counter
    @Timer
    public DomibusPropertyManagerExt getManagerForProperty(String propertyName) throws DomibusPropertyException {
        loadPropertiesIfNotFound(propertyName);
        if (hasProperty(internalPropertyMetadataMap, propertyName)) {
            LOG.trace("Property [{}] is internal, returning null as the manager (no external manager involved).", propertyName);
            return null;
        }

        Optional<DomibusPropertyManagerExt> found = extPropertyManagers.stream()
                .filter(manager -> manager.hasKnownProperty(propertyName)).findFirst();

        if (found.isPresent()) {
            LOG.trace("Property [{}] is external, returning its manager [{}].", propertyName, found.get());
            return found.get();
        }

        throw new DomibusPropertyException("Property " + propertyName + " could not be found anywhere.");
    }

    /**
     * Initializes the metadata map with internal and/or external properties,
     * until the given property is found.
     *
     * @param propertyName the name of the property to search for
     * @implNote Initially, during the bean creation stage, only a few domibus-core properties are needed;
     * later on, the properties from all managers will be added to the map.
     */
    protected void loadPropertiesIfNotFound(String propertyName) {
        // We add domibus-core and specific server properties first to avoid infinite loop of bean creation (due to DB properties)
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

    /**
     * Loads external properties (i.e. plugin properties and extension properties)
     * the first time one of them is needed
     */
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

    protected DomibusPropertyMetadata clonePropertyMetadata(String propertyName, DomibusPropertyMetadata propMeta) {
        // make a clone and then add it to the map
        DomibusPropertyMetadata newPropMeta = domainConverter.convert(propMeta, DomibusPropertyMetadata.class);
        // metadata name may be just the prefix, not be the concrete propertyName,
        // so we set the whole property name here to be correctly used down the stream. Not beautiful
        newPropMeta.setName(propertyName);

        allPropertyMetadataMap.put(propertyName, newPropMeta);
        internalPropertyMetadataMap.put(propertyName, newPropMeta);

        return newPropMeta;
    }

}
