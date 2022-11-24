package eu.domibus.ext.services;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Interface needed by plugins to access the domibus BackendConnectorProvider
 */
public interface BackendConnectorProviderExtService {

    /**
     * Checks that at least one plugin is enabled on the specified domain; if not, throws a ConfigurationException
     *
     * @param domainCode the domain on which the validation is done
     */
    void validateConfiguration(final String domainCode);

    /**
     * Verifies if the plugin with the specified name can be disabled on the specified domain
     *
     * @param backendName the plugin name
     * @param domainCode  the domain
     * @return false if it is the only one enabled, otherwise true
     */
    boolean canDisableBackendConnector(String backendName, final String domainCode);

    /**
     * The specified plugin notifies domibus that he wants to be enabled on the specified domain;
     * This is necessary because domibus manages message queues and cron trigger jobs for a plugin
     * Validation is performed before
     *
     * @param backendName
     * @param domainCode
     */
    default void backendConnectorEnabled(String backendName, String domainCode) {
    }

    /**
     * The specified plugin notifies domibus that he wants to be disabled on the specified domain;
     * This is necessary because domibus manages message queues and cron trigger jobs for a plugin
     * Validation is performed before
     *
     * @param backendName
     * @param domainCode
     */
    default void backendConnectorDisabled(String backendName, String domainCode) {
    }
}
