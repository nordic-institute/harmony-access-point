package eu.domibus.core.crypto.spi.dss;

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
 * Property manager for the DSS properties.
 */
@Service
public class DSSPropertyManager implements DomibusPropertyManagerExt {

    public static final String DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_NAME = "domibus.authentication.dss.constraint.name";
    public static final String DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_STATUS = "domibus.authentication.dss.constraint.status";
    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainExtService domainExtService;

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
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        String[] knownPropertyNames = new String[]{
                DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_NAME,
                DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_STATUS
        };
        return Arrays.stream(knownPropertyNames)
                .map(name -> new DomibusPropertyMetadataDTO(name, Module.DSS, false, DomibusPropertyMetadataDTO.Usage.DOMAIN, true, true, false, true))
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
    }
//String name, String module, boolean writable, int usage, boolean withFallback, boolean clusterAware, boolean encrypted, boolean isComposable) {
    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }
}
