package eu.domibus.core.monitoring;

import eu.domibus.web.rest.ro.ConnectionMonitorRO;


/**
 * @author Ion Perpegel
 * @since 4.2
 */
public interface ConnectionMonitoringService {
    void sendTestMessages();

    boolean isMonitoringEnabled();

    ConnectionMonitorRO getConnectionStatus(String partyId);
}
