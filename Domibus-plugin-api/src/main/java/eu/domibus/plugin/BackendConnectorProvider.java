package eu.domibus.plugin;

import java.util.List;

/**
 * Service used for operations related with plugins
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorProvider {

    /**
     * Retrieves the backend connector (plugin) with the specified name
     * @param backendName the name of the backend connector
     * @return the backend connector, if exists, or null
     */
    BackendConnector<?, ?> getBackendConnector(String backendName);

    /**
     * Retrieves the list of all backend connectors (plugins)
     * @return the list mentioned above
     */
    List<BackendConnector<?, ?>> getBackendConnectors();

    /**
     * Checks that at least one plugin is enabled on each domain; if not, takes one and enables it
     * At this point, only default plugins are enable-aware and only the FSPlugin can actually be disabled
     */
    void ensureValidConfiguration();

    /**
     * Checks that at least one plugin is enabled on the specified domain; if not, throws a ConfigurationException
     * @param domainCode the domain on which the validation is done
     */
    void validateConfiguration(final String domainCode);
}
