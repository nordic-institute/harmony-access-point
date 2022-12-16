package eu.domibus.core.property.listeners;

import eu.domibus.api.party.PartyService;
import eu.domibus.api.property.DomibusPropertyChangeListener;
import eu.domibus.core.monitoring.ConnectionMonitoringHelper;
import eu.domibus.core.pmode.provider.PModeProvider;
import org.springframework.stereotype.Service;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Handles the change of connection monitoring properties, validating that only known party identifiers are used.
 */
@Service
public class ConnectionMonitoringChangeListener implements DomibusPropertyChangeListener {

    private final ConnectionMonitoringHelper connectionMonitoringHelper;

    public ConnectionMonitoringChangeListener(PartyService partyService, PModeProvider pModeProvider, ConnectionMonitoringHelper connectionMonitoringHelper) {
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
