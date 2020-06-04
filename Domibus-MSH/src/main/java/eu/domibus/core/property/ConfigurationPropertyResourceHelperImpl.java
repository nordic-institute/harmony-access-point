package eu.domibus.core.property;

import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.*;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    GlobalPropertyMetadataManager globalPropertyMetadataManager;

    @Override
    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain) {
        List<DomibusProperty> result = new ArrayList<>();

        List<DomibusPropertyMetadata> propertiesMetadata = filterProperties(name, showDomain, globalPropertyMetadataManager.getAllProperties());
        List<DomibusProperty> properties = createProperties(propertiesMetadata);

        result.addAll(properties);

        return result;
    }

    @Override
    public void setPropertyValue(String name, boolean isDomain, String value) throws DomibusPropertyException {
        if (isDomain) {
            LOG.debug("Setting the value [{}] for the domain property [{}] in the current domain.", value, name);
            domibusPropertyProvider.setProperty(name, value);
            LOG.info("Property [{}] updated.", name);
            return;
        }
        if (!authUtils.isSuperAdmin()) {
            throw new DomibusPropertyException("Cannot set global or super properties if not a super user.");
        }
        // for non-domain properties, we set the value in the null-domain context:
        domainTaskExecutor.submit(() -> {
            LOG.debug("Setting the value [{}] for the global/super property [{}].", value, name);
            domibusPropertyProvider.setProperty(name, value);
            LOG.info("Property [{}] updated.", name);
        });
    }

    @Override
    public DomibusProperty getProperty(String propertyName) {
        DomibusPropertyMetadata propertyMetadata = globalPropertyMetadataManager.getPropertyMetadata(propertyName);
        DomibusProperty prop = createProperty(propertyMetadata);
        return prop;
    }

    protected List<DomibusProperty> createProperties(List<DomibusPropertyMetadata> properties) {
        List<DomibusProperty> list = new ArrayList<>();

        for (DomibusPropertyMetadata propMeta : properties) {
            DomibusProperty prop = createProperty(propMeta);
            list.add(prop);
        }

        return list;
    }

    private DomibusProperty createProperty(DomibusPropertyMetadata propMeta) {
        String propertyValue = domibusPropertyProvider.getProperty(propMeta.getName());

        DomibusProperty prop = new DomibusProperty();
        prop.setMetadata(propMeta);
        prop.setValue(propertyValue);
        return prop;
    }

    protected List<DomibusPropertyMetadata> filterProperties(String name, boolean showDomain, Map<String, DomibusPropertyMetadata> propertiesMap) {
        List<DomibusPropertyMetadata> knownProps = propertiesMap.values().stream()
                .filter(p -> p.isWritable())
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
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
