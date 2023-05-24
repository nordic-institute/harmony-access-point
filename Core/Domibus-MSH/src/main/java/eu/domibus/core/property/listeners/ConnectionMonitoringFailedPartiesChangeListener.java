package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.monitoring.ConnectionMonitoringHelper;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES;

/**
 * @author Ion Perpegel
 * @since 5.1
 * <p>
 * Handles the change of connection monitoring alert for failed parties, validating that only valid party identifiers are used.
 */
@Service
public class ConnectionMonitoringFailedPartiesChangeListener implements DomibusPropertyChangeListener {

    private final ConnectionMonitoringHelper connectionMonitoringHelper;

    public ConnectionMonitoringFailedPartiesChangeListener(ConnectionMonitoringHelper connectionMonitoringHelper) {
        this.connectionMonitoringHelper = connectionMonitoringHelper;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return DOMIBUS_ALERT_CONNECTION_MONITORING_FAILED_PARTIES.equalsIgnoreCase(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        connectionMonitoringHelper.validateEnabledPartiesValue(propertyValue);
    }

}
