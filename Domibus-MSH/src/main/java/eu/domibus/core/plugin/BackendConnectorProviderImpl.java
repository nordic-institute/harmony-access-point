package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.BackendConnectorProvider;
import eu.domibus.plugin.EnableAware;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorProviderImpl implements BackendConnectorProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorProviderImpl.class);

    protected final List<BackendConnector<?, ?>> backendConnectors;

    protected final DomainService domainService;

    //BackendConnector (SPIs) must be injected Lazily to avoid circular dependency with core services
    public BackendConnectorProviderImpl(@Lazy List<BackendConnector<?, ?>> backendConnectors, DomainService domainService) {
        this.backendConnectors = backendConnectors;
        this.domainService = domainService;
    }

    public BackendConnector<?, ?> getBackendConnector(String backendName) {
        for (final BackendConnector<?, ?> backendConnector : backendConnectors) {
            if (backendConnector.getName().equalsIgnoreCase(backendName)) {
                return backendConnector;
            }
        }
        return null;
    }

    public List<BackendConnector<?, ?>> getBackendConnectors() {
        return backendConnectors;
    }

    @Override
    public void ensureValidConfiguration() {
        List<EnableAware> plugins = getEnableAwares();

        if (CollectionUtils.isEmpty(plugins)) {
            LOG.info("No plugins found that can be disabled. Exiting");
            return;
        }

        domainService.getDomains().forEach(domain -> {
            if (plugins.stream().allMatch(plugin -> !plugin.isEnabled(domain.getCode()))) {
                EnableAware plugin = plugins.get(0);
                LOG.warn("Cannot let all plugins to be disabled on domain [{}]. Enabling [{}].", domain, plugin.getName());
                try {
                    plugin.setEnabled(domain.getCode(), true);
                } catch (DomibusPropertyException ex) {
                    LOG.error("Could not enable plugin [{}] on domain [{}]", plugin.getName(), domain);
                }
            }
        });

    }

    @Override
    public void validateConfiguration(String domainCode) {
        List<EnableAware> plugins = getEnableAwares();

        if (plugins.stream().allMatch(plugin1 -> !plugin1.isEnabled(domainCode))) {
            throw new ConfigurationException(String.format("No plugin is enabled on domain {[}]", domainCode));
        }
    }

    @Override
    public boolean canDisableBackendConnector(String backendName, String domainCode) {
        BackendConnector<?, ?> plugin = getBackendConnector(backendName);
        if (plugin == null) {
            LOG.info("Could not find backend connector with the name [{}]; returning false; ", backendName);
            return false;
        }
        if (!(plugin instanceof EnableAware)) {
            LOG.info("Backend connector with the name [{}] is not domain aware; returning false; ", backendName);
            return false;
        }
        EnableAware enableAware = (EnableAware) plugin;
        if (!enableAware.isEnabled(domainCode)) {
            LOG.info("Cannot disable backend connector with the name [{}] since it is already disabled; returning false; ", backendName);
            return false;
        }

        List<EnableAware> enabled = getEnableAwares().stream().filter(item -> item.isEnabled(domainCode)).collect(Collectors.toList());
        if (enabled.size() > 1) {
            LOG.debug("Backend connector with the name [{}] can be disabled on domain [{}]; returning true; ", backendName, domainCode);
            return true;
        }

        LOG.info("Cannot disable backend connector with the name [{}] since it is the only one enabled on domain [{}]; returning false; ",
                backendName, domainCode);
        return false;
    }

    private List<EnableAware> getEnableAwares() {
        return getBackendConnectors()
                .stream()
                .filter(connector -> connector instanceof EnableAware)
                .map(connector -> (EnableAware) connector)
                .collect(Collectors.toList());
    }
}
