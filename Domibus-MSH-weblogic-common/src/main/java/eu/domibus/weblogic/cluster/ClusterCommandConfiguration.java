package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.CommandExecutorService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Creates a scheduler to execute the commands on all the cluster members
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Conditional(ClusterDeploymentCondition.class)
@Configuration
@EnableScheduling
public class ClusterCommandConfiguration {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ClusterCommandConfiguration.class);

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected CommandExecutorService commandExecutorService;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Scheduled(fixedDelay = 5000)
    public void scheduleClusterCommandExecution() {
        String serverName = System.getProperty("weblogic.Name");
        LOGGER.debug("Server name ...[{}]", serverName);

        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            domainTaskExecutor.submit(() -> commandExecutorService.executeCommands(serverName), domain);
        }

        if (domibusConfigurationService.isMultiTenantAware()) {
            domainTaskExecutor.submit(() -> commandExecutorService.executeCommands(serverName));
        }
    }
}
