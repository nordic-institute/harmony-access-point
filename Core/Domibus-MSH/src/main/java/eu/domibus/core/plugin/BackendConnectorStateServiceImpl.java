package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.plugin.BackendConnectorStateService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class BackendConnectorStateServiceImpl implements BackendConnectorStateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorStateServiceImpl.class);

    protected final DomainService domainService;
    protected final DomainContextProvider domainContextProvider;
    protected final MessageListenerContainerInitializer messageListenerContainerInitializer;
    protected final DomibusScheduler domibusScheduler;
    protected final BackendConnectorProvider backendConnectorProvider;
    
    public BackendConnectorStateServiceImpl(DomainService domainService, DomainContextProvider domainContextProvider,
                                            MessageListenerContainerInitializer messageListenerContainerInitializer,
                                            DomibusScheduler domibusScheduler, BackendConnectorProvider backendConnectorProvider) {
        this.domainService = domainService;
        this.domainContextProvider = domainContextProvider;
        this.messageListenerContainerInitializer = messageListenerContainerInitializer;
        this.domibusScheduler = domibusScheduler;
        this.backendConnectorProvider = backendConnectorProvider;
    }

    @Override
    public void backendConnectorEnabled(String backendName, String domainCode) {
        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.createMessageListenersForPlugin(backendName, domain);

        EnableAware plugin = (EnableAware)backendConnectorProvider.getBackendConnector(backendName);
        String[] jobNamesToResume = plugin.getJobNames().toArray(new String[]{});
        domibusScheduler.resumeJobs(domain, jobNamesToResume);
    }

    @Override
    public void backendConnectorDisabled(String backendName, String domainCode) {
        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.destroyMessageListenersForPlugin(backendName, domain);

        EnableAware plugin = (EnableAware)backendConnectorProvider.getBackendConnector(backendName);
        String[] jobNamesToResume = plugin.getJobNames().toArray(new String[]{});
        domibusScheduler.pauseJobs(domain, jobNamesToResume);
    }
}
