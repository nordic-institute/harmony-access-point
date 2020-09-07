package eu.domibus.core.crypto.spi.dss.listeners;

import eu.domibus.ext.exceptions.DomibusPropertyExtException;
import eu.domibus.plugin.property.PluginPropertyChangeListener;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class CertificateVerifierPropertiesChangeListener implements PluginPropertyChangeListener {
    @Override
    public boolean handlesProperty(String propertyName) {
        return false;
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) throws DomibusPropertyExtException {

    }
}
