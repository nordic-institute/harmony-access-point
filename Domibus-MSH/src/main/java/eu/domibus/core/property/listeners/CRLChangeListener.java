package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.certificate.crl.CRLService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS;

/**
 * Handles the change of property to exclude protocols from CRL list
 *
 * @author Soumya Chandran
 * @since 4.2
 */
@Service
public class CRLChangeListener implements DomibusPropertyChangeListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CRLChangeListener.class);

    @Autowired
    private CRLService crlService;


    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.equalsIgnoreCase(propertyName, DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        LOG.trace("Clearing cache and supported CrlProtocols.");
        crlService.reset();
    }
}
