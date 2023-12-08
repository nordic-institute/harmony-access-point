package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;
import org.apache.commons.lang3.StringUtils;
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

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorProviderImpl.class);

    protected final List<BackendConnector<?, ?>> backendConnectors;

    protected DomainService domainService;

    //BackendConnector (SPIs) must be injected Lazily to avoid circular dependency with core services
    public BackendConnectorProviderImpl(@Lazy List<BackendConnector<?, ?>> backendConnectors,
                                        DomainService domainService) {
        this.backendConnectors = backendConnectors;
        this.domainService = domainService;
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
     *
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

    @Override
    public EnableAware getEnableAware(String name) {
        return getEnableAwares().stream().filter(plugin -> StringUtils.equals(plugin.getName(), name)).findFirst().orElse(null);
    }


    @Override
    public boolean atLeastOneDomainEnabled(EnableAware enableAware) {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            if (enableAware.isEnabled(domain.getCode())) {
                LOG.debug("Plugin/extension [{}] is enabled for domain [{}]", enableAware.getName(), domain.getCode());
                return true;
            }
        }
        LOG.debug("Plugin/extension [{}] is not enabled", enableAware.getName());
        return false;
    }

}
