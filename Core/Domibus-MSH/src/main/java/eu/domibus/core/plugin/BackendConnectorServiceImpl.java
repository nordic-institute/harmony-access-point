package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.plugin.BackendConnectorService;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendConnectorServiceImpl implements BackendConnectorService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorServiceImpl.class);

    protected final DomainService domainService;
    protected final BackendConnectorProvider backendConnectorProvider;
    protected final DomainContextProvider domainContextProvider;

    public BackendConnectorServiceImpl(DomainService domainService, BackendConnectorProvider backendConnectorProvider, DomainContextProvider domainContextProvider) {
        this.domainService = domainService;
        this.backendConnectorProvider = backendConnectorProvider;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public void ensureValidConfiguration() {
        List<EnableAware> plugins = backendConnectorProvider.getEnableAwares();

        if (CollectionUtils.isEmpty(plugins)) {
            LOG.info("No plugins found that can be disabled. Exiting");
            return;
        }

        domainService.getDomains().forEach(domain -> {
            domainContextProvider.setCurrentDomain(domain);
            if (plugins.stream().allMatch(plugin -> !plugin.isEnabled(domain.getCode()))) {
                EnableAware plugin = plugins.get(0);
                LOG.warn("Cannot let all plugins to be disabled on domain [{}]. Enabling [{}].", domain, plugin.getName());
                try {
                    plugin.setEnabled(domain.getCode(), true);
                } catch (DomibusPropertyException ex) {
                    LOG.error("Could not enable plugin [{}] on domain [{}]", plugin.getName(), domain, ex);
                }
            }
            domainContextProvider.clearCurrentDomain();
        });

    }

    @Override
    public void validateConfiguration(String domainCode) {
        List<EnableAware> plugins = backendConnectorProvider.getEnableAwares();

        if (plugins.stream().allMatch(plugin1 -> !plugin1.isEnabled(domainCode))) {
            throw new ConfigurationException(String.format("No plugin is enabled on domain {[}]", domainCode));
        }
    }

    @Override
    public boolean canDisableBackendConnector(String backendName, String domainCode) {
        BackendConnector<?, ?> plugin = backendConnectorProvider.getBackendConnector(backendName);
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

        List<EnableAware> enabled = backendConnectorProvider.getEnableAwares().stream().filter(item -> item.isEnabled(domainCode)).collect(Collectors.toList());
        if (enabled.size() > 1) {
            LOG.debug("Backend connector with the name [{}] can be disabled on domain [{}]; returning true; ", backendName, domainCode);
            return true;
        }

        LOG.info("Cannot disable backend connector with the name [{}] since it is the only one enabled on domain [{}]; returning false; ",
                backendName, domainCode);
        return false;
    }


}
