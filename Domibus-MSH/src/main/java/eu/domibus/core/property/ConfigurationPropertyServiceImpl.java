package eu.domibus.core.property;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ConfigurationPropertyServiceImpl implements ConfigurationPropertyService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ConfigurationPropertyServiceImpl.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    DomibusPropertyProviderImpl domibusPropertyProvider;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    @Lazy
    DomibusPropertyMetadataManagerImpl globalPropertyMetadataManager;

    @Override
    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain) {
        List<DomibusProperty> result = new ArrayList<>();

        List<DomibusPropertyMetadata> propertiesMetadata = filterProperties(name, showDomain, globalPropertyMetadataManager.getAllProperties());
        List<DomibusProperty> properties = createProperties(propertiesMetadata);

        result.addAll(properties);

        return result;
    }

    @Override
    @Transactional(noRollbackFor = DomibusCoreException.class)
    public void setPropertyValue(String name, boolean isDomain, String value) throws DomibusPropertyException {
        try {
            DomibusPropertyMetadata propMeta = globalPropertyMetadataManager.getPropertyMetadata(name);

            if (isDomain) {
                LOG.debug("Setting the value [{}] for the domain property [{}] in the current domain.", value, name);
                domibusPropertyProvider.setProperty(name, value);
            } else {
                if (!authUtils.isSuperAdmin()) {
                    throw new DomibusPropertyException("Cannot set global or super properties if not a super user.");
                }
                // for non-domain properties, we set the value in the null-domain context:
                domainTaskExecutor.submit(() -> {
                    LOG.debug("Setting the value [{}] for the global/super property [{}].", value, name);
                    domibusPropertyProvider.setProperty(name, value);
                });
            }
        } catch (IllegalArgumentException ex) {
            LOG.error("Could not set property [{}].", name, ex);
        }
    }

    private List<DomibusProperty> createProperties(List<DomibusPropertyMetadata> properties) {
        List<DomibusProperty> list = new ArrayList<>();

        for (DomibusPropertyMetadata propMeta : properties) {
            String propertyValue = domibusPropertyProvider.getProperty(propMeta.getName());

            DomibusProperty prop = new DomibusProperty();
            prop.setMetadata(propMeta);
            prop.setValue(propertyValue);

            list.add(prop);
        }

        return list;
    }

    private List<DomibusPropertyMetadata> filterProperties(String name, boolean showDomain, Map<String, DomibusPropertyMetadata> propertiesMap) {
        List<DomibusPropertyMetadata> knownProps = propertiesMap.values().stream()
                .filter(p -> p.isWritable())
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        if (domibusConfigurationService.isMultiTenantAware()) {
            if (showDomain) {
                knownProps = knownProps.stream().filter(p -> p.isDomain()).collect(Collectors.toList());
            } else {
                if (authUtils.isSuperAdmin()) {
                    knownProps = knownProps.stream().filter(p -> p.isGlobal() || p.isSuper()).collect(Collectors.toList());
                } else {
                    throw new DomibusPropertyException("Cannot request global and super properties if not a super user.");
                }
            }
        }
        return knownProps;
    }

}
