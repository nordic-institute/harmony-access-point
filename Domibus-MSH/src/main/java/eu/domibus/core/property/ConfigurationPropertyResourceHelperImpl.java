package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.*;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.rest.validators.DomibusPropertyBlacklistValidator;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * responsible with getting the domibus properties that can be changed at runtime, getting and setting their values
 */
@Service
public class ConfigurationPropertyResourceHelperImpl implements ConfigurationPropertyResourceHelper {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationPropertyResourceHelperImpl.class);

    private DomibusConfigurationService domibusConfigurationService;

    private DomibusPropertyProvider domibusPropertyProvider;

    private AuthUtils authUtils;

    private DomainTaskExecutor domainTaskExecutor;

    private GlobalPropertyMetadataManager globalPropertyMetadataManager;

    private DomibusPropertyBlacklistValidator domibusPropertyBlacklistValidator;

    public ConfigurationPropertyResourceHelperImpl(DomibusConfigurationService domibusConfigurationService,
                                                   DomibusPropertyProvider domibusPropertyProvider,
                                                   AuthUtils authUtils,
                                                   DomainTaskExecutor domainTaskExecutor,
                                                   GlobalPropertyMetadataManager globalPropertyMetadataManager,
                                                   DomibusPropertyBlacklistValidator domibusPropertyBlacklistValidator) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.globalPropertyMetadataManager = globalPropertyMetadataManager;
        this.domibusPropertyBlacklistValidator = domibusPropertyBlacklistValidator;
        this.domibusPropertyBlacklistValidator.init();
    }

    @Override
    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain, String type, String module, String value) {
        List<DomibusProperty> result = new ArrayList<>();

        List<DomibusPropertyMetadata> propertiesMetadata = filterProperties(globalPropertyMetadataManager.getAllProperties(),
                name, showDomain, type, module);

        List<DomibusProperty> properties = createProperties(propertiesMetadata);

        List<DomibusProperty> filteredProps = filterByValue(value, properties);
        result.addAll(filteredProps);

        return result;
    }

    @Override
    public void setPropertyValue(String propertyName, boolean isDomain, String propertyValue) throws DomibusPropertyException {
        validateProperty(propertyName, propertyValue);

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
        if(!globalPropertyMetadataManager.hasKnownProperty(propertyName)) {
            throw new DomibusPropertyException("mama");
        }

        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        return getValueAndCreateProperty(propertyMetadata);
    }

    protected List<DomibusProperty> filterByValue(String value, List<DomibusProperty> properties) {
        if (value == null) {
            return properties;
        }
        return properties.stream()
                .filter(prop -> StringUtils.equals(value, prop.getValue()))
                .collect(Collectors.toList());
    }

    protected void validateProperty(String propertyName, String propertyValue) {
        DomibusProperty prop = getProperty(propertyName);
        prop.setValue(propertyValue);
        domibusPropertyBlacklistValidator.validate(prop);
    }

    protected List<DomibusProperty> createProperties(List<DomibusPropertyMetadata> properties) {
        List<DomibusProperty> list = new ArrayList<>();

        for (DomibusPropertyMetadata propMeta : properties) {
            DomibusProperty prop = getValueAndCreateProperty(propMeta);
            list.add(prop);
        }

        return list;
    }

    protected DomibusProperty getValueAndCreateProperty(DomibusPropertyMetadata propMeta) {
        String propertyValue = getPropertyValue(propMeta);
        return createProperty(propMeta, propertyValue);
    }

    protected String getPropertyValue(DomibusPropertyMetadata propMeta) {
        if (propMeta.isDomain()) {
            String name = propMeta.getName();
            LOG.debug("Getting the value for the domain property [{}].", name);
            return domibusPropertyProvider.getProperty(name);
        }

        // for non-domain properties, we get the value in the null-domain context:
        return domainTaskExecutor.submit(() -> {
            String name = propMeta.getName();
            LOG.debug("Getting the value for the global/super property [{}].", name);
            return domibusPropertyProvider.getProperty(name);
        });
    }

    protected DomibusProperty createProperty(DomibusPropertyMetadata propMeta, String propertyValue) {
        DomibusProperty prop = new DomibusProperty();
        prop.setMetadata(propMeta);
        prop.setValue(propertyValue);
        return prop;
    }

    protected List<DomibusPropertyMetadata> filterProperties(Map<String, DomibusPropertyMetadata> propertiesMap, String name,
                                                             boolean showDomain, String type, String module) {
        List<DomibusPropertyMetadata> knownProps = propertiesMap.values().stream()
                .filter(prop -> prop.isWritable())
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
