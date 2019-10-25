package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.security.AuthUtils;
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
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Service called from the ConfigurationPropertyResource REST class
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
     * We inject here all managers: one for each plugin + domibus property manager delegate( which adapts DomibusPropertyManager to DomibusPropertyManagerExt)
     */
    @Autowired
    private List<DomibusPropertyManagerExt> propertyManagers;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    public List<DomibusProperty> getAllWritableProperties(String name, boolean showDomain) {
        List<DomibusProperty> list = new ArrayList<>();

//        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();

        for (DomibusPropertyManagerExt propertyManager : propertyManagers) {
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
                        throw new IllegalArgumentException("Cannot request global and super properties if not a super user.");
                    }
                }
            }

            for (DomibusPropertyMetadataDTO p : knownProps) {
                try {
                    String value = propertyManager.getKnownPropertyValue(p.getName());
                    DomibusPropertyMetadata meta = domainConverter.convert(p, DomibusPropertyMetadata.class);

                    DomibusProperty prop = new DomibusProperty();
                    prop.setMetadata(meta);
                    prop.setValue(value);

                    list.add(prop);
                } catch (Throwable ex) {
                    int i = 1;
                }
            }
        }

        return list;
    }

    @Transactional(noRollbackFor = DomibusCoreException.class)
    public void setPropertyValue(String name, boolean isDomain, String value) {
        boolean handled = false;
        for (DomibusPropertyManagerExt propertyManager : propertyManagers) {
            if (!propertyManager.hasKnownProperty(name)) {
                continue;
            }

            if (isDomain) {
                propertyManager.setKnownPropertyValue(name, value);
            } else {
                if (!authUtils.isSuperAdmin()) {
                    throw new IllegalArgumentException("Cannot set global or super properties if not a super user.");
                }
                // for non-domain properties, we set the value in the null-domain context:
                domainTaskExecutor.submit(() -> {
                    propertyManager.setKnownPropertyValue(name, value);
                });
            }
            handled = true;
        }

        if (!handled) {
            LOG.debug("Property manager not found for [{}]", name);
            throw new IllegalArgumentException("Property not found: " + name);
        }
    }

}
