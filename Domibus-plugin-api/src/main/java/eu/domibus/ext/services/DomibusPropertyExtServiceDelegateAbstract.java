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
 * Used to derive external property managers that delegate to Domibus property manager. Ex: JmsPluginPropertyManager, DSS PropertyManager
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
        if (propMeta.isStoredGlobally()) {
            return domibusPropertyExtService.getProperty(propertyName);
        }

        LOG.trace("Property [{}] is not stored globally so onGetLocalPropertyValue is called.", propertyName);
        return onGetLocalPropertyValue(propertyName, propMeta);
    }

    /**
     * Method called for a locally stored property; should be overridden by derived classes for all locally stored properties
     *
     * @param propertyName the name of the property
     * @param propMeta     the property metadata
     * @return the property value
     */
    protected String onGetLocalPropertyValue(String propertyName, DomibusPropertyMetadataDTO propMeta) {
        LOG.warn("Property [{}] is not stored globally and not handled locally so null was returned.", propertyName);
        return null;
    }

    @Override
    public Integer getKnownIntegerPropertyValue(String propertyName) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (propMeta.isStoredGlobally()) {
            return domibusPropertyExtService.getIntegerProperty(propertyName);
        }

        LOG.trace("Property [{}] is not stored globally so onGetLocalIntegerPropertyValue is called.", propertyName);
        return onGetLocalIntegerPropertyValue(propertyName, propMeta);
    }

    /**
     * Method called for a locally stored property; should be overridden by derived classes for all locally stored properties
     *
     * @param propertyName the name of the property
     * @param propMeta     the property metadata
     * @return the property value
     */
    protected Integer onGetLocalIntegerPropertyValue(String propertyName, DomibusPropertyMetadataDTO propMeta) {
        LOG.warn("Property [{}] is not stored globally and not handled locally so 0 was returned.", propertyName);
        return 0;
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        return getKnownPropertyValue(propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (propMeta.isStoredGlobally()) {
            final DomainDTO domain = domainExtService.getDomain(domainCode);
            domibusPropertyExtService.setDomainProperty(domain, propertyName, propertyValue);
        }

        LOG.debug("Property [{}] is not stored globally so onSetLocalPropertyValue is called.", propertyName);
        onSetLocalPropertyValue(domainCode, propertyName, propertyValue, broadcast);
    }

    /**
     * Method called for a locally stored property; should be overridden by derived classes for all locally stored properties
     *
     * @param domainCode    the code of the domain
     * @param propertyName  the name of the property
     * @param propertyValue the value of the property
     */
    protected void onSetLocalPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        LOG.warn("Property [{}] is not stored globally and not handled locally.", propertyName);
    }

    @Override
    public void setKnownPropertyValue(String propertyName, String propertyValue) {
        checkPropertyExists(propertyName);

        DomibusPropertyMetadataDTO propMeta = getKnownProperties().get(propertyName);
        if (propMeta.isStoredGlobally()) {
            domibusPropertyExtService.setProperty(propertyName, propertyValue);
        }
        LOG.debug("Property [{}] is not stored globally so onSetLocalPropertyValue is called.", propertyName);
        onSetLocalPropertyValue(propertyName, propertyValue);
    }

    /**
     * Method called for a locally stored property; should be overridden by derived classes for all locally stored properties
     *
     * @param propertyName  the name of the property
     * @param propertyValue the value of the property
     */
    protected void onSetLocalPropertyValue(String propertyName, String propertyValue) {
        LOG.warn("Property [{}] is not stored globally and not handled locally.", propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }

    protected void checkPropertyExists(String propertyName) {
        if (!hasKnownProperty(propertyName)) {
            throw new DomibusPropertyExtException("Unknown property: " + propertyName);
        }
    }
}
