package eu.domibus.ext.delegate.services.backend;

import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.ext.services.BackendConnectorProviderExtService;
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

    private final BackendConnectorService backendConnectorService;

    public BackendConnectorProviderExtServiceDelegate(@Lazy BackendConnectorService backendConnectorService) {
        this.backendConnectorService = backendConnectorService;
    }

    @Override
    public void validateConfiguration(String domainCode) {
        backendConnectorService.validateConfiguration(domainCode);
    }

    @Override
    public boolean canDisableBackendConnector(String backendName, String domainCode) {
        return backendConnectorService.canDisableBackendConnector(backendName, domainCode);
    }
}
