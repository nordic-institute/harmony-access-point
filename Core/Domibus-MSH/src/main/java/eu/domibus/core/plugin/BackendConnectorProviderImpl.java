package eu.domibus.core.plugin;

import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Service
public class BackendConnectorProviderImpl implements BackendConnectorProvider {

    protected final List<BackendConnector<?, ?>> backendConnectors;

    //BackendConnector (SPIs) must be injected Lazily to avoid circular dependency with core services
    public BackendConnectorProviderImpl(@Lazy List<BackendConnector<?, ?>> backendConnectors) {
        this.backendConnectors = backendConnectors;
    }

    /**
     * Retrieves the backend connector (plugin) with the specified name
     *
     * @param backendName the name of the backend connector
     * @return the backend connector, if exists, or null
     */
    @Override
    public BackendConnector<?, ?> getBackendConnector(String backendName) {
        for (final BackendConnector<?, ?> backendConnector : backendConnectors) {
            if (backendConnector.getName().equalsIgnoreCase(backendName)) {
                return backendConnector;
            }
        }
        return null;
    }

    /**
     * Retrieves the list of all backend connectors (plugins)
     * @return the list mentioned above
     */
    @Override
    public List<BackendConnector<?, ?>> getBackendConnectors() {
        return backendConnectors;
    }

    @Override
    public List<EnableAware> getEnableAwares() {
        return getBackendConnectors()
                .stream()
                .filter(connector -> connector instanceof EnableAware)
                .map(connector -> (EnableAware) connector)
                .collect(Collectors.toList());
    }

}
