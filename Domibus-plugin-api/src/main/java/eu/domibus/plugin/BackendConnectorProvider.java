package eu.domibus.plugin;

import java.util.List;

/**
 * Service used for operations related with plugins
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface BackendConnectorProvider {

    BackendConnector<?, ?> getBackendConnector(String backendName);

    List<BackendConnector<?, ?>> getBackendConnectors();

    void ensureValidConfiguration();

    void validateConfiguration(String backendName, final String domainCode);
}
