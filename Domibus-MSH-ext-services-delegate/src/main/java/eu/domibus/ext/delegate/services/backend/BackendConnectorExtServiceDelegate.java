package eu.domibus.ext.delegate.services.backend;

import eu.domibus.plugin.BackendConnectorProvider;
import eu.domibus.ext.services.BackendConnectorExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used for operations related with plugins that delegated to BackendConnectorProvider
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class BackendConnectorExtServiceDelegate implements BackendConnectorExtService {

    @Autowired
    BackendConnectorProvider backendConnectorProvider;

    @Override
    public void ensureValidConfiguration() {
        backendConnectorProvider.ensureValidConfiguration();
    }

    @Override
    public void validateConfiguration(String backendName, String domainCode) {
        backendConnectorProvider.validateConfiguration(backendName, domainCode);
    }
}
