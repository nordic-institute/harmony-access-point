package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.*;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.rest.validators.DomibusPropertyValueValidator;
import eu.domibus.core.rest.validators.FieldBlacklistValidator;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible with getting the domibus properties that can be changed at runtime, getting and setting their values
 *
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class ConfigurationPropertyResourceHelperImpl implements ConfigurationPropertyResourceHelper {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationPropertyResourceHelperImpl.class);

    public static final String ACCEPTED_CHARACTERS_IN_PROPERTY_NAMES = ".";

    private DomibusConfigurationService domibusConfigurationService;

    private DomibusPropertyProvider domibusPropertyProvider;

    private AuthUtils authUtils;

    private DomainTaskExecutor domainTaskExecutor;

    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private DomibusPropertyValueValidator domibusPropertyValueValidator;

    private FieldBlacklistValidator propertyNameBlacklistValidator;

    public ConfigurationPropertyResourceHelperImpl(DomibusConfigurationService domibusConfigurationService,
                                                   DomibusPropertyProvider domibusPropertyProvider,
                                                   AuthUtils authUtils,
                                                   DomainTaskExecutor domainTaskExecutor,
                                                   GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                                   DomibusPropertyValueValidator domibusPropertyValueValidator,
                                                   FieldBlacklistValidator propertyNameBlacklistValidator) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.domibusPropertyValueValidator = domibusPropertyValueValidator;
        this.propertyNameBlacklistValidator = propertyNameBlacklistValidator;
        this.propertyNameBlacklistValidator.init();
    }

    @Override
    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain, String type, String module, String value, Boolean isWritable) {
        List<DomibusPropertyMetadata> propertiesMetadata = filterProperties(globalPropertyMetadataManager.getAllProperties(),
                name, showDomain, type, module, isWritable);

        if (CollectionUtils.isEmpty(propertiesMetadata)) {
            return new ArrayList();
        }

        List<DomibusProperty> properties;

        if (showDomain) {
            properties = getPropertyValues(propertiesMetadata);
        } else {
            // for non-domain properties, we get the values in the null-domain context:
            properties = domainTaskExecutor.submit(() -> getPropertyValues(propertiesMetadata));
        }

        properties = filterByValue(value, properties);
        properties = sortProperties(properties);

        return properties;
    }

    @Override
    public void setPropertyValue(String propertyName, boolean isDomain, String propertyValue) throws DomibusPropertyException {
        validatePropertyValue(propertyName, propertyValue);

        if (isDomain) {
            LOG.debug("Setting the value [{}] for the domain property [{}] in the current domain.", propertyValue, propertyName);
            domibusPropertyProvider.setProperty(propertyName, propertyValue);
            LOG.info("Property [{}] updated.", propertyName);
            return;
        }
        if (!authUtils.isSuperAdmin()) {
            throw new DomibusPropertyException("Cannot set global or super properties if not a super user.");
        }
        // for non-domain properties, we set the value in the null-domain context:
        domainTaskExecutor.submit(() -> {
            LOG.debug("Setting the value [{}] for the global/super property [{}].", propertyValue, propertyName);
            domibusPropertyProvider.setProperty(propertyName, propertyValue);
            LOG.info("Property [{}] updated.", propertyName);
        });
    }

    @Override
    public DomibusProperty getProperty(String propertyName) {
        if (!globalPropertyMetadataManager.hasKnownProperty(propertyName)) {
            throw new DomibusPropertyException("Unknown property: " + propertyName);
        }

        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        return getValueAndCreateProperty(propertyMetadata);
    }

    protected List<DomibusProperty> getPropertyValues(List<DomibusPropertyMetadata> properties) {
        Map<String, DomibusProperty> result = new HashMap<>();

        for (DomibusPropertyMetadata propMeta : properties) {
            if (!propMeta.isComposable()) {
                DomibusProperty prop = getValueAndCreateProperty(propMeta);
                addIfMissing(result, prop);
            } else {
                List<DomibusProperty> props = getNestedProperties(propMeta);
                props.forEach(prop -> addIfMissing(result, prop));
            }
        }

        return new ArrayList<>(result.values());
    }

    protected List<DomibusProperty> getNestedProperties(DomibusPropertyMetadata propMeta) {
        List<String> suffixes = domibusPropertyProvider.getNestedProperties(propMeta.getName());
        List<DomibusProperty> result = suffixes.stream()
                .map(suffix -> getProperty(propMeta.getName() + "." + suffix))
                .collect(Collectors.toList());
        result.add(getProperty(propMeta.getName()));
        return result;
    }

    protected List<DomibusProperty> sortProperties(List<DomibusProperty> properties) {
        List<DomibusProperty> list = properties.stream()
                .filter(property -> property.getMetadata() != null && property.getMetadata().getName() != null)
                .collect(Collectors.toList());
        list.sort(Comparator.comparing(property -> property.getMetadata().getName()));
        return list;
    }

    protected List<DomibusProperty> filterByValue(String value, List<DomibusProperty> properties) {
        if (value == null) {
            return properties;
        }
        return properties.stream()
                .filter(prop -> StringUtils.equals(value, prop.getValue()))
                .collect(Collectors.toList());
    }

    protected void validatePropertyValue(String propertyName, String propertyValue) {
        propertyNameBlacklistValidator.validate(propertyName, ACCEPTED_CHARACTERS_IN_PROPERTY_NAMES);

        DomibusPropertyMetadata propMeta = getPropertyMetadata(propertyName);

        if (propMeta == null) {
            throw new DomibusPropertyException("Cannot set property " + propertyName + " because it does not exist.");
        }

        if (!propMeta.isWritable()) {
            throw new DomibusPropertyException("Cannot set property " + propertyName + " because it is not writable.");
        }

        if (propMeta.getName().equals(propertyName) && propMeta.isComposable()) {
            throw new DomibusPropertyException("Cannot set property " + propertyName + ". You can only set its nested properties.");
        }

        DomibusProperty prop = createProperty(propMeta, propertyValue);

        prop.setValue(propertyValue);
        domibusPropertyValueValidator.validate(prop);
    }

    protected DomibusPropertyMetadata getPropertyMetadata(String propertyName) {
        if (globalPropertyMetadataManager.hasKnownProperty(propertyName)) {
            return globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        }
        //find parent if composable
        return globalPropertyMetadataManager.getComposableProperty(propertyName);
    }

    private void addIfMissing(Map<String, DomibusProperty> result, DomibusProperty prop) {
        String name = prop.getMetadata().getName();
        if (!result.containsKey(name)) {
            result.put(name, prop);
        }
    }

    protected DomibusProperty getValueAndCreateProperty(DomibusPropertyMetadata propMeta) {
        String propertyValue = domibusPropertyProvider.getProperty(propMeta.getName());
        return createProperty(propMeta, propertyValue);
    }

    protected DomibusProperty createProperty(DomibusPropertyMetadata propMeta, String propertyValue) {
        DomibusProperty prop = new DomibusProperty();
        prop.setMetadata(propMeta);
        prop.setValue(propertyValue);
        return prop;
    }

    protected List<DomibusPropertyMetadata> filterProperties(Map<String, DomibusPropertyMetadata> propertiesMap,
                                                             String name, boolean showDomain, String type, String module, Boolean isWritable) {
        List<DomibusPropertyMetadata> knownProps = propertiesMap.values().stream()
                .filter(prop -> isWritable == null || isWritable == prop.isWritable())
                .filter(prop -> name == null || StringUtils.containsIgnoreCase(prop.getName(), name))
                .filter(prop -> type == null || StringUtils.equals(type, prop.getType()))
                .filter(prop -> module == null || StringUtils.equals(module, prop.getModule()))
                .collect(Collectors.toList());

        if (!domibusConfigurationService.isMultiTenantAware()) {
            return knownProps;
        }

        if (showDomain) {
            return knownProps.stream().filter(p -> p.isDomain()).collect(Collectors.toList());
        }

        if (authUtils.isSuperAdmin()) {
            return knownProps.stream().filter(p -> p.isGlobal() || p.isSuper()).collect(Collectors.toList());
        }

        throw new DomibusPropertyException("Cannot request global and super properties if not a super user.");
    }

}
