package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
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
public abstract class DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainExtService domainExtService;

    public abstract Map<String, DomibusPropertyMetadataDTO> getKnownProperties();

    @Override
    public String getKnownPropertyValue(String propertyName) {
        checkPropertyExists(propertyName);

        return domibusPropertyExtService.getProperty(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        checkPropertyExists(propertyName);

        final DomainDTO domain = domainExtService.getDomain(domainCode);
        domibusPropertyExtService.setDomainProperty(domain, propertyName, propertyValue);
    }

    private void checkPropertyExists(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyExtException("Unknown property: " + propertyName);
        }
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        checkPropertyExists(propertyName);
        domibusPropertyExtService.setProperty(propertyName, propertyValue);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return getKnownPropertyValue(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }
}
