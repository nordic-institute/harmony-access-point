package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Delegate/adapter class that wraps the DomibusPropertyManager and exposes the DomibusPropertyManagerExt
 * Injected in ConfigurationPropertyServiceImpl to handle in a polymorphic way plugin and domibus property management
 */
@Service(DomibusPropertyManagerDelegate.MSH_DELEGATE)
public class DomibusPropertyManagerDelegate implements DomibusPropertyManagerExt {

    public static final String MSH_DELEGATE = "mshDelegate";

//    @Autowired
//    @Qualifier(DomibusPropertyManager.MSH_PROPERTY_MANAGER)
//    private DomibusPropertyManager domibusPropertyManager;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Autowired
    protected DomainService domainService;

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        Map<String, DomibusPropertyMetadata> res = domibusPropertyProvider.getKnownProperties();
        return domainConverter.convert(res, DomibusPropertyMetadataDTO.class);
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        return domibusPropertyProvider.getProperty(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        final Domain domain = domainService.getDomain(domainCode);
        domibusPropertyProvider.setProperty(domain, propertyName, propertyValue, broadcast);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return domibusPropertyManager.getKnownPropertyValue(domainCode, propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        domibusPropertyManager.setKnownPropertyValue(domainCode, propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        domibusPropertyProvider.setProperty(propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return domibusPropertyProvider.hasKnownProperty(name);
    }
}
