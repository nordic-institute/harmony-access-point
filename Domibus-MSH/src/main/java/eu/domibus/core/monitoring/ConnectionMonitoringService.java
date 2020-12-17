package eu.domibus.core.monitoring;

import eu.domibus.web.rest.ro.ConnectionMonitorRO;

import java.util.Map;


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
     * Retrieves the last known connection status for the given parties.
     *
     * @param partyIds The party identifier array
     * @return Details about the connection status of the given parties.
     */
    Map<String, ConnectionMonitorRO> getConnectionStatus(String[] partyIds);
}
