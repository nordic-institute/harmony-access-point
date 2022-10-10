package eu.domibus.ext.delegate.services.backend;

import eu.domibus.api.plugin.BackendConnectorNotificationService;
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

    private final BackendConnectorNotificationService backendConnectorNotificationService;

    public BackendConnectorProviderExtServiceDelegate(@Lazy BackendConnectorService backendConnectorService, BackendConnectorNotificationService backendConnectorNotificationService) {
        this.backendConnectorService = backendConnectorService;
        this.backendConnectorNotificationService = backendConnectorNotificationService;
    }

    @Override
    public void validateConfiguration(String domainCode) {
        backendConnectorService.validateConfiguration(domainCode);
    }

    @Override
    public boolean canDisableBackendConnector(String backendName, String domainCode) {
        return backendConnectorService.canDisableBackendConnector(backendName, domainCode);
    }

    @Override
    public void backendConnectorEnabled(String backendName, String domainCode) {
        backendConnectorNotificationService.backendConnectorEnabled(backendName, domainCode);
    }

    @Override
    public void backendConnectorDisabled(String backendName, String domainCode) {
        backendConnectorNotificationService.backendConnectorDisabled(backendName, domainCode);
    }
}
