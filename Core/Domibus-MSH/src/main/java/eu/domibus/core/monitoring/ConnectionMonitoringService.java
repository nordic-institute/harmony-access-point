package eu.domibus.core.monitoring;

import eu.domibus.web.rest.ro.ConnectionMonitorRO;

import java.util.List;
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
     * Checks whether the monitoring is enabled for at least a party except self.
     */
    boolean isMonitoringEnabled();

    /**
     * Checks whether the self monitoring is enabled.
     */
    boolean isSelfMonitoringEnabled();

    /**
     * Retrieves the last known connection status for the given parties.
     *
     * @param senderPartyId
     * @param partyIds The party identifier array
     * @return Details about the connection status of the given parties.
     */
    Map<String, ConnectionMonitorRO> getConnectionStatus(String senderPartyId, List<String> partyIds);

    void sendTestMessageToMyself();

    boolean isDeleteHistoryEnabled();

    void deleteReceivedTestMessageHistoryIfApplicable();
}
