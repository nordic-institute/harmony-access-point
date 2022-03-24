package eu.domibus.ext.delegate.services.backend;

import eu.domibus.ext.services.BackendConnectorExtService;
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
public class BackendConnectorExtServiceDelegate implements BackendConnectorExtService {

    private final BackendConnectorProvider backendConnectorProvider;

    public BackendConnectorExtServiceDelegate(@Lazy BackendConnectorProvider backendConnectorProvider) {
        this.backendConnectorProvider = backendConnectorProvider;
    }

    @Override
    public void ensureValidConfiguration() {
        backendConnectorProvider.ensureValidConfiguration();
    }

    @Override
    public void validateConfiguration(String domainCode) {
        backendConnectorProvider.validateConfiguration(domainCode);
    }
}
