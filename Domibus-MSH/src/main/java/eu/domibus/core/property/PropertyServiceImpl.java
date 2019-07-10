package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.Property;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.DomibusPropertyManager;
import eu.domibus.property.DomibusPropertyMetadata;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
@Service
public class PropertyServiceImpl implements PropertyService {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PropertyServiceImpl.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private List<DomibusPropertyManager> domibusPropertyManagers;

    public List<Property> getProperties(String name) {
        List<Property> list = new ArrayList<>();

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = currentDomain == null ? null : currentDomain.getCode();

        for (DomibusPropertyManager propertyManager : domibusPropertyManagers) {
            List<DomibusPropertyMetadata> knownProps = propertyManager.getKnownProperties().values().stream()
                    .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());

            if (domibusConfigurationService.isMultiTenantAware()) {
                if (currentDomain == null) {
                    knownProps = knownProps.stream().filter(p -> !p.isDomainSpecific()).collect(Collectors.toList());
                } else {
                    knownProps = knownProps.stream().filter(p -> p.isDomainSpecific()).collect(Collectors.toList());
                }
            }

            for (DomibusPropertyMetadata p : knownProps) {
                String value = propertyManager.getKnownPropertyValue(domainCode, p.getName());
                Property prop = new Property();
                prop.setName(p.getName());
                prop.setValue(value);
                list.add(prop);
            }
        }

        return list;
    }


    public void setPropertyValue(String name, String value) {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = currentDomain == null ? null : currentDomain.getCode();

        boolean handled = false;
        for (DomibusPropertyManager propertyManager : domibusPropertyManagers) {
            if (propertyManager.hasKnownProperty(name)) {
                propertyManager.setKnownPropertyValue(domainCode, name, value);
                handled = true;
            }
        }

        if (!handled) {
            LOG.debug("Property manager not found for [{}]", name);
            throw new IllegalArgumentException(name);
        }
    }

}
