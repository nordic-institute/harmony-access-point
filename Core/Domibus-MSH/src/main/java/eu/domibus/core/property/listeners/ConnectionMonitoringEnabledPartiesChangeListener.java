package eu.domibus.core.property.listeners;

import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.monitoring.ConnectionMonitoringHelper;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Handles the change of connection monitoring enabled parties, validating that only valid party identifiers are used.
 */
@Service
public class ConnectionMonitoringEnabledPartiesChangeListener implements DomibusPropertyChangeListener {

    private final ConnectionMonitoringHelper connectionMonitoringHelper;

    public ConnectionMonitoringEnabledPartiesChangeListener(ConnectionMonitoringHelper connectionMonitoringHelper) {
        this.connectionMonitoringHelper = connectionMonitoringHelper;
    }

    @Override
    public boolean handlesProperty(String propertyName) {
        return DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED.equalsIgnoreCase(propertyName);
    }

    @Override
    public void propertyValueChanged(String domainCode, String propertyName, String propertyValue) {
        connectionMonitoringHelper.validateEnabledPartiesValue(propertyValue);
    }

}
