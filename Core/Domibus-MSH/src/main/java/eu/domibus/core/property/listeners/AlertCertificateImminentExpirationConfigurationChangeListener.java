package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.core.alerts.configuration.certificate.imminent.ImminentExpirationCertificateConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to configuration of imminent certificate expiration alerts
 */
@Service
public class AlertCertificateImminentExpirationConfigurationChangeListener implements DomibusPropertyChangeListener {

    @Autowired
    private ImminentExpirationCertificateConfigurationManager imminentExpirationCertificateConfigurationManager;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        imminentExpirationCertificateConfigurationManager.reset();
    }
}
