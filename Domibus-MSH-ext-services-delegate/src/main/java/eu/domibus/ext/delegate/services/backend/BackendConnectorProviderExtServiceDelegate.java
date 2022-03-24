package eu.domibus.ext.delegate.services.backend;

import eu.domibus.ext.services.BackendConnectorProviderExtService;
import eu.domibus.plugin.BackendConnectorProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Service used by plugins that delegate to BackendConnectorProvider
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class BackendConnectorProviderExtServiceDelegate implements BackendConnectorProviderExtService {

    private final BackendConnectorProvider backendConnectorProvider;

    public BackendConnectorProviderExtServiceDelegate(@Lazy BackendConnectorProvider backendConnectorProvider) {
        this.backendConnectorProvider = backendConnectorProvider;
    }

    @Override
    public void validateConfiguration(String domainCode) {
        backendConnectorProvider.validateConfiguration(domainCode);
    }

    @Override
    public boolean canDisableBackendConnector(String backendName, String domainCode) {
        return backendConnectorProvider.canDisableBackendConnector(backendName, domainCode);
    }
}
