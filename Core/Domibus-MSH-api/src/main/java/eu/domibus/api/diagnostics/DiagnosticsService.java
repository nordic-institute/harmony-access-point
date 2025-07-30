package eu.domibus.api.diagnostics;

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
     * @return VersionInfo with version information
     */
    VersionInfo getVersionInfo();

    /**
     * Get JMS queues information
     * 
     * @return JmsQueuesInfo with JMS queues information
     */
    JmsQueuesInfo getJmsQueuesInfo();

    /**
     * Get alerts counts
     * 
     * @return AlertsCounts with alerts counts
     */
    AlertsCounts getAlertsCounts();

    /**
     * Get messages by status counts
     * 
     * @return MessagesByStatusCounts with messages by status counts
     */
    MessagesByStatusCounts getMessagesByStatusCounts();
}
