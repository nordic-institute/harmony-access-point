package eu.domibus.core.plugin.initializer;

import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.initialize.PluginInitializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Service
public class PluginInitializerProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginInitializerProvider.class);

    protected BackendConnectorProvider backendConnectorProvider;

    public PluginInitializerProvider(BackendConnectorProvider backendConnectorProvider) {
        this.backendConnectorProvider = backendConnectorProvider;
    }

    public List<PluginInitializer> getPluginInitializersForEnabledPlugins() {
        return backendConnectorProvider.getEnableAwares().stream()
                .filter(enableAware -> backendConnectorProvider.atLeastOneDomainEnabled(enableAware))
                .map(enableAware -> ((BackendConnector) enableAware).getPluginInitializer())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
