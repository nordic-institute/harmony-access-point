package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 *
 * Delegate/adapter class that wraps the DomibusPropertyManager and exposes the DomibusPropertyManagerExt
 * Injected in DomibusPropertyService to handle in a polymorphic way plugin and domibus property management
 */
@Service
public class DomibusPropertyManagerDelegate implements DomibusPropertyManagerExt {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyManagerDelegate.class);

    @Autowired
    private DomibusPropertyManager domibusPropertyManager;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        Map<String, DomibusPropertyMetadata> res = domibusPropertyManager.getKnownProperties();
        return domainConverter.convert(res, DomibusPropertyMetadataDTO.class);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return domibusPropertyManager.getKnownPropertyValue(domainCode, propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        domibusPropertyManager.setKnownPropertyValue(domainCode, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        domibusPropertyManager.setKnownPropertyValue(domainCode, propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return domibusPropertyManager.hasKnownProperty(name);
    }
}
