package eu.domibus.core.monitoring;

import eu.domibus.web.rest.ro.ConnectionMonitorRO;


/**
 * @author Ion Perpegel
 * @since 4.2
 */
public interface ConnectionMonitoringService {

    /**
     * Sends test messages to all monitored parties.
     */
    void sendTestMessages();

    /**
     * Checks whether the monitoring is enabled for at least a party.
     */
    boolean isMonitoringEnabled();

    /**
     * Retrieves the last known connection status for the given party.
     * @param partyId The party identifier
     * @return Details about the connection status of the given party.
     */
    ConnectionMonitorRO getConnectionStatus(String partyId);
}
