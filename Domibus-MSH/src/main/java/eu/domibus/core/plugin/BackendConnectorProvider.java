package eu.domibus.core.plugin;

import eu.domibus.plugin.BackendConnector;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class BackendConnectorProvider {

    protected List<BackendConnector<?, ?>> backendConnectors;

    public BackendConnectorProvider(List<BackendConnector<?, ?>> backendConnectors) {
        this.backendConnectors = backendConnectors;
    }

    public BackendConnector<?, ?> getBackendConnector(String backendName) {
        for (final BackendConnector<?, ?> backendConnector : backendConnectors) {
            if (backendConnector.getName().equalsIgnoreCase(backendName)) {
                return backendConnector;
            }
        }
        return null;
    }
}
