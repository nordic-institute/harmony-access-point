package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyMetadataManager;
import eu.domibus.core.alerts.service.MultiDomainAlertConfigurationService;
import eu.domibus.plugin.property.PluginPropertyChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * Handles the change of alert properties that are related to configuration of certificate expiration alerts
 */
@Service
public class AlertCertificateExpiredConfigurationChangeListener implements PluginPropertyChangeListener {

    @Autowired
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Override
    public boolean handlesProperty(String propertyName) {
        return StringUtils.startsWithIgnoreCase(propertyName, DomibusPropertyMetadataManager.DOMIBUS_ALERT_CERT_EXPIRED_PREFIX);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        multiDomainAlertConfigurationService.clearExpiredCertificateConfiguration();
    }
}
