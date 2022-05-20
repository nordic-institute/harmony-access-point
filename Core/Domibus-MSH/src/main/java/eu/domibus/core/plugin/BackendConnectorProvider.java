package eu.domibus.core.plugin;

import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;

import java.util.List;

public interface BackendConnectorProvider {

    BackendConnector<?, ?> getBackendConnector(String backendName);
    List<BackendConnector<?, ?>> getBackendConnectors();

    List<EnableAware> getEnableAwares();
}
