package eu.domibus.core.plugin;

import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;

import java.util.List;

public interface BackendConnectorProvider {

    BackendConnector<?, ?> getBackendConnector(String backendName);

    List<BackendConnector<?, ?>> getBackendConnectors();

    /**
     * Get backend connectors which are EnableAware
     */
    List<EnableAware> getEnableAwares();

    EnableAware getEnableAware(String name);

    /**
     * Checks if the EnableAware is enabled in at least one domain
     */
    boolean atLeastOneDomainEnabled(EnableAware enableAware);
}
