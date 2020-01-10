package eu.domibus.ext.delegate.services.property;

import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Delegate/adapter class that wraps the DomibusPropertyManager and exposes the DomibusPropertyManagerExt
 * Injected in ConfigurationPropertyServiceImpl to handle in a polymorphic way plugin and domibus property management
 */
@Service
public class DomibusPropertyManagerDelegate implements DomibusPropertyManagerExt {

    @Autowired
    @Qualifier("mshPropertyManager")
    private DomibusPropertyManager domibusPropertyManager;

    @Autowired
    protected DomainExtConverter domainConverter;

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        Map<String, DomibusPropertyMetadata> res = domibusPropertyManager.getKnownProperties();
        return domainConverter.convert(res, DomibusPropertyMetadataDTO.class);
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        return domibusPropertyManager.getKnownPropertyValue(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        domibusPropertyManager.setKnownPropertyValue(domainCode, propertyName, propertyValue, broadcast);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        domibusPropertyManager.setKnownPropertyValue(propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return domibusPropertyManager.hasKnownProperty(name);
    }
}
