package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Abstract class that implements DomibusPropertyManagerExt and delegates its methods to DomibusPropertyExtService
 * Used to derive external property managers that delegate to Domibus property manager. Ex: JmsPluginProperyManager, DSS PropertyManager
 */
@Service
public abstract class DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainExtService domainExtService;

    @Override
    public abstract Map<String, DomibusPropertyMetadataDTO> getKnownProperties();

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return getKnownPropertyValue(propertyName);
    }

    @Override
    public String getKnownPropertyValue(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }

        return domibusPropertyExtService.getProperty(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }

        final DomainDTO domain = domainExtService.getDomain(domainCode);
        domibusPropertyExtService.setDomainProperty(domain, propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        if (!hasKnownProperty(propertyName)) {
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        domibusPropertyExtService.setProperty(propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }
}
