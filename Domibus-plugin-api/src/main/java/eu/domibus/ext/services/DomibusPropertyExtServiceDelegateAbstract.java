package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Abstract class that implements DomibusPropertyManagerExt and delegates its methods to DomibusPropertyExtService
 * Used to derive external property managers that delegate to Domibus property manager. Ex: JmsPluginProperyManager, DSS PropertyManager
 */
public abstract class DomibusPropertyExtServiceDelegateAbstract implements DomibusPropertyManagerExt {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPropertyExtServiceDelegateAbstract.class);

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainExtService domainExtService;

    public abstract Map<String, DomibusPropertyMetadataDTO> getKnownProperties();

    @Override
    public String getKnownPropertyValue(String propertyName) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (!propMeta.isStoredGlobally()) {
            LOG.debug("Property [{}] is not stored globally so null was returned.", propertyName);
            return null;
        }

        return domibusPropertyExtService.getProperty(propertyName);
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return getKnownPropertyValue(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (!propMeta.isStoredGlobally()) {
            LOG.debug("Property [{}] is not stored globally so did not forward the setProperty call.", propertyName);
            return;
        }

        final DomainDTO domain = domainExtService.getDomain(domainCode);
        domibusPropertyExtService.setDomainProperty(domain, propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (!propMeta.isStoredGlobally()) {
            LOG.debug("Property [{}] is not stored globally so did not forward the setProperty call.", propertyName);
            return;
        }
        domibusPropertyExtService.setProperty(propertyName, propertyValue);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }

    private void checkPropertyExists(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyExtException("Unknown property: " + propertyName);
        }
    }
}
