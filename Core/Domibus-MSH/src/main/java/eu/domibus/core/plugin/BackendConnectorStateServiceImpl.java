package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.plugin.BackendConnectorStateService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.EnableAware;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class BackendConnectorStateServiceImpl implements BackendConnectorStateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorStateServiceImpl.class);

    protected final DomainService domainService;
    protected final MessageListenerContainerInitializer messageListenerContainerInitializer;
    protected final DomibusScheduler domibusScheduler;
    protected final BackendConnectorProvider backendConnectorProvider;

    public BackendConnectorStateServiceImpl(DomainService domainService, MessageListenerContainerInitializer messageListenerContainerInitializer,
                                            DomibusScheduler domibusScheduler, BackendConnectorProvider backendConnectorProvider) {
        this.domainService = domainService;
        this.messageListenerContainerInitializer = messageListenerContainerInitializer;
        this.domibusScheduler = domibusScheduler;
        this.backendConnectorProvider = backendConnectorProvider;
    }

    @Override
    public void backendConnectorEnabled(String backendName, String domainCode) {
        domainService.validateDomain(domainCode);

        LOG.debug("Enabling plugin [{}] on domain [{}]; creating resources for it.", backendName, domainCode);

        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.createMessageListenersForPlugin(backendName, domain);

        EnableAware plugin = (EnableAware) backendConnectorProvider.getBackendConnector(backendName);
        String[] jobNamesToResume = plugin.getJobNames().toArray(new String[]{});
        domibusScheduler.resumeJobs(domain, jobNamesToResume);
    }

    @Override
    public void backendConnectorDisabled(String backendName, String domainCode) {
        domainService.validateDomain(domainCode);

        List<EnableAware> plugins = backendConnectorProvider.getEnableAwares();
        if (plugins.stream().noneMatch(plugin -> plugin.isEnabled(domainCode))) {
            throw new ConfigurationException(String.format("No plugin is enabled on domain {[}]", domainCode));
        }
        
        LOG.debug("Disabling plugin [{}] on domain [{}]; destroying resources for it.", backendName, domainCode);

        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.destroyMessageListenersForPlugin(backendName, domain);

        EnableAware plugin = (EnableAware) backendConnectorProvider.getBackendConnector(backendName);
        String[] jobNamesToResume = plugin.getJobNames().toArray(new String[]{});
        domibusScheduler.pauseJobs(domain, jobNamesToResume);
    }
}
