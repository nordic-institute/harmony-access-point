package eu.domibus.api.diagnostics;

import eu.domibus.ext.domain.diagnostics.AlertsCountsDTO;
import eu.domibus.ext.domain.diagnostics.JmsQueuesInfoDTO;
import eu.domibus.ext.domain.diagnostics.MessagesByStatusCountsDTO;
import eu.domibus.ext.domain.diagnostics.VersionInfoDTO;

/**
 * Interface for DiagnosticsService
 *
 * @author Breaz Ionut
 * @since 5.1.9
 */
public interface DiagnosticsService {

    /**
     * Logs diagnostic information
     */
    void logDiagnosticInfo();

    /**
     * Get version information
     * 
     * @return VersionInfoDTO with version information
     */
    VersionInfoDTO getVersionInfo();

    /**
     * Get JMS queues information
     * 
     * @return JmsQueuesInfoDTO with JMS queues information
     */
    JmsQueuesInfoDTO getJmsQueuesInfo();

    /**
     * Get alerts counts
     * 
     * @return AlertsCountsDTO with alerts counts
     */
    AlertsCountsDTO getAlertsCounts();

    /**
     * Get messages by status counts
     * 
     * @return MessagesByStatusCountsDTO with messages by status counts
     */
    MessagesByStatusCountsDTO getMessagesByStatusCounts();
}
