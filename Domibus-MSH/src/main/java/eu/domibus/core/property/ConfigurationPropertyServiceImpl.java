package eu.domibus.core.property;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.validators.DomibusPropertyValidator;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.ClassUtil;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    protected DomainExtConverter domainConverter;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    /**
     * We inject here all property managers: one for each plugin, external module, specific server
     * and domibus property manager delegate( which adapts DomibusPropertyManager to DomibusPropertyManagerExt)
     */
    @Autowired
    private List<DomibusPropertyManagerExt> propertyManagers;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    ClassUtil classUtil;

    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain) {
        List<DomibusProperty> allProperties = new ArrayList<>();

        for (DomibusPropertyManagerExt propertyManager : propertyManagers) {
            List<DomibusPropertyMetadataDTO> propertyMetadata = filterProperties(name, showDomain, propertyManager);
            List<DomibusProperty> moduleProperties = createProperties(propertyManager, propertyMetadata);
            allProperties.addAll(moduleProperties);
        }

        return allProperties;
    }

    @Transactional(noRollbackFor = DomibusCoreException.class)
    public void setPropertyValue(String name, boolean isDomain, String value) throws DomibusPropertyException {
        try {
            DomibusPropertyManagerExt propertyManager = getManagerForProperty(name);

            DomibusPropertyMetadataDTO propMeta = propertyManager.getKnownProperties().get(name);
            // validate the property value against the type
            // We are doing it here since it covers all properties, core and external and server specific
            validatePropertyValue(propMeta, value);

            if (isDomain) {
                LOG.trace("Setting the value [{}] for the domain property [{}] in the current domain.", value, name);
                setPropertyValue(propertyManager, name, value);
            } else {
                if (!authUtils.isSuperAdmin()) {
                    throw new DomibusPropertyException("Cannot set global or super properties if not a super user.");
                }
                // for non-domain properties, we set the value in the null-domain context:
                domainTaskExecutor.submit(() -> {
                    LOG.trace("Setting the value [{}] for the global/super property [{}].", value, name);
                    setPropertyValue(propertyManager, name, value);
                });
            }
        } catch (IllegalArgumentException ex) {
            LOG.error("Could not set property [{}].", name, ex);
        }
    }

    protected DomibusPropertyManagerExt getManagerForProperty(String propertyName) {
        Optional<DomibusPropertyManagerExt> found = propertyManagers.stream()
                .filter(manager -> manager.hasKnownProperty(propertyName)).findFirst();
        if (found.isPresent()) {
            return found.get();
        }

        throw new DomibusPropertyException("Property manager not found for property " + propertyName);
    }

    protected void validatePropertyValue(DomibusPropertyMetadataDTO propMeta, String propertyValue) throws DomibusPropertyException {
        if (propMeta == null) {
            LOG.warn("Property metadata is null; exiting validation.");
            return;
        }

        try {
            DomibusPropertyMetadata.Type type = DomibusPropertyMetadata.Type.valueOf(propMeta.getType());
            DomibusPropertyValidator validator = type.getValidator();
            if (validator == null) {
                LOG.debug("Validator for type [{}] of property [{}] is null; exiting validation.", propMeta.getType(), propMeta.getName());
                return;
            }

            if (!validator.isValid(propertyValue)) {
                throw new DomibusPropertyException("Property value [" + propertyValue + "] of property [" + propMeta.getName() + "] does not match property type [" + type.name() + "].");
            }
        } catch (IllegalArgumentException ex) {
            LOG.warn("Property type [{}] of property [{}] is not known; exiting validation.", propMeta.getType(), propMeta.getName());
        }
    }

    private List<DomibusProperty> createProperties(DomibusPropertyManagerExt propertyManager, List<DomibusPropertyMetadataDTO> knownProps) {
        List<DomibusProperty> list = new ArrayList<>();

        for (DomibusPropertyMetadataDTO p : knownProps) {
            String propertyName = p.getName();
            String value = getPropertyValue(propertyManager, propertyName);
            DomibusPropertyMetadata meta = domainConverter.convert(p, DomibusPropertyMetadata.class);

            DomibusProperty prop = new DomibusProperty();
            prop.setMetadata(meta);
            prop.setValue(value);

            list.add(prop);
        }

        return list;
    }

    private List<DomibusPropertyMetadataDTO> filterProperties(String name, boolean showDomain, DomibusPropertyManagerExt propertyManager) {
        List<DomibusPropertyMetadataDTO> knownProps = propertyManager.getKnownProperties().values().stream()
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

    protected String getPropertyValue(DomibusPropertyManagerExt propertyManager, String propertyName) {
        String value;
        if (classUtil.isMethodDefined(propertyManager, "getKnownPropertyValue", new Class[]{String.class})) {
            LOG.debug("Calling getKnownPropertyValue method");
            value = propertyManager.getKnownPropertyValue(propertyName);
        } else {
            LOG.debug("Calling deprecated getKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            value = propertyManager.getKnownPropertyValue(currentDomain.getCode(), propertyName);
        }
        return value;
    }

    protected void setPropertyValue(DomibusPropertyManagerExt propertyManager, String name, String value) {
        if (classUtil.isMethodDefined(propertyManager, "setKnownPropertyValue", new Class[]{String.class, String.class})) {
            LOG.debug("Calling setKnownPropertyValue method");
            propertyManager.setKnownPropertyValue(name, value);
        } else {
            LOG.debug("Calling deprecated setKnownPropertyValue method");
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            propertyManager.setKnownPropertyValue(currentDomain.getCode(), name, value);
        }
    }


}
