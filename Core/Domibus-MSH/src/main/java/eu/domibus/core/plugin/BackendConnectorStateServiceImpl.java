package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.plugin.BackendConnectorStateService;
import eu.domibus.api.scheduler.DomibusScheduler;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.ext.domain.CronJobInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.EnableAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible with creating or destroying expensive plugin resources managed by Domibus( message listener container and cron jobs)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Service
public class BackendConnectorStateServiceImpl implements BackendConnectorStateService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorStateServiceImpl.class);

    protected final DomainService domainService;
    protected final MessageListenerContainerInitializer messageListenerContainerInitializer;
    protected final DomibusScheduler domibusScheduler;
    protected final BackendConnectorProvider backendConnectorProvider;
    protected final RoutingService routingService;

    public BackendConnectorStateServiceImpl(DomainService domainService, MessageListenerContainerInitializer messageListenerContainerInitializer,
                                            DomibusScheduler domibusScheduler, BackendConnectorProvider backendConnectorProvider, RoutingService routingService) {
        this.domainService = domainService;
        this.messageListenerContainerInitializer = messageListenerContainerInitializer;
        this.domibusScheduler = domibusScheduler;
        this.backendConnectorProvider = backendConnectorProvider;
        this.routingService = routingService;
    }

    @Override
    public void backendConnectorEnabled(String backendName, String domainCode) {
        domainService.validateDomain(domainCode);

        LOG.debug("Enabling plugin [{}] on domain [{}]; creating resources for it.", backendName, domainCode);

        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.createMessageListenersForPlugin(backendName, domain);

        resumeJobs(backendName, domain);

        routingService.refreshBackendFilters();
    }

    @Override
    public void backendConnectorDisabled(String backendName, String domainCode) {
        domainService.validateDomain(domainCode);

        List<EnableAware> plugins = backendConnectorProvider.getEnableAwares();
        if (plugins.stream().noneMatch(plugin -> plugin.isEnabled(domainCode))) {
            throw new ConfigurationException(String.format("Cannot disable the plugin [%s] on domain {%s] because there won't remain any enabled plugin."
                    , backendName, domainCode));
        }

        LOG.debug("Disabling plugin [{}] on domain [{}]; destroying resources for it.", backendName, domainCode);

        Domain domain = domainService.getDomain(domainCode);
        messageListenerContainerInitializer.destroyMessageListenersForPlugin(backendName, domain);

        pauseJobs(backendName, domain);

        routingService.refreshBackendFilters();
    }

    private void resumeJobs(String backendName, Domain domain) {
        String[] jobNames = getJobNames(backendName);
        if (jobNames == null) {
            LOG.debug("Could not find any job names for the plugin called [{}]; exiting.", backendName);
            return;
        }
        domibusScheduler.resumeJobs(domain, jobNames);
    }
    
    private void pauseJobs(String backendName, Domain domain) {
        String[] jobNames = getJobNames(backendName);
        if (jobNames == null) {
            LOG.debug("Could not find any job names for the plugin called [{}]; exiting.", backendName);
            return;
        }
        domibusScheduler.pauseJobs(domain, jobNames);
    }

    private EnableAware getEnableAware(String backendName) {
        BackendConnector<?, ?> backendConnector = backendConnectorProvider.getBackendConnector(backendName);
        if (!(backendConnector instanceof EnableAware)) {
            LOG.info("Backend connector [{}] does not implement EnableAware; exiting.", backendName);
            return null;
        }
        EnableAware plugin = (EnableAware) backendConnector;
        return plugin;
    }

    private String[] getJobNames(String backendName) {
        EnableAware plugin = getEnableAware(backendName);
        if (plugin == null) {
            LOG.info("Could not find a plugin called [{}]; exiting.", backendName);
            return null;
        }

        List<CronJobInfoDTO> jobsInfo = plugin.getJobsInfo();
        if (CollectionUtils.isEmpty(jobsInfo)) {
            LOG.info("No job names returned; nothing to pause.");
            return null;
        }
        String[] jobNamesToResume = jobsInfo.stream().map(CronJobInfoDTO::getName)
                .collect(Collectors.toList())
                .toArray(new String[]{});
        return jobNamesToResume;
    }
}
