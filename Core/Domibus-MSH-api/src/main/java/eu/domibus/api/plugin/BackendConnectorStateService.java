package eu.domibus.api.plugin;

/**
 * Service used by plugins to notify domibus that the enabled state has changed
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorStateService {

    /**
     * The specified plugin notifies domibus that he wants to be enabled on the specified domain;
     * This is necessary because domibus manages message queues and cron trigger jobs for a plugin
     * Validation is performed before
     * @param backendName
     * @param domainCode
     */
    void backendConnectorEnabled(String backendName, String domainCode);

    /**
     * The specified plugin notifies domibus that he wants to be disabled on the specified domain;
     * This is necessary because domibus manages message queues and cron trigger jobs for a plugin
     * Validation is performed before
     * @param backendName
     * @param domainCode
     */
    void backendConnectorDisabled(String backendName, String domainCode);

}
