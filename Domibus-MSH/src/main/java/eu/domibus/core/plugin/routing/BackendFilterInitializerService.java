package eu.domibus.core.plugin.routing;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class BackendFilterInitializerService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFilterInitializerService.class);

    protected DomibusConfigurationService domibusConfigurationService;
    protected BackendConnectorProvider backendConnectorProvider;
    protected AuthUtils authUtils;
    protected DomainTaskExecutor domainTaskExecutor;
    protected DomainService domainService;
    protected RoutingService routingService;

    public BackendFilterInitializerService(DomibusConfigurationService domibusConfigurationService,
                                           BackendConnectorProvider backendConnectorProvider,
                                           AuthUtils authUtils,
                                           DomainTaskExecutor domainTaskExecutor,
                                           DomainService domainService,
                                           RoutingService routingService) {
        this.domibusConfigurationService = domibusConfigurationService;
        this.backendConnectorProvider = backendConnectorProvider;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.domainService = domainService;
        this.routingService = routingService;
    }

    public void updateMessageFilters() {
        LOG.info("Checking and updating the configured plugins");

        if (CollectionUtils.isEmpty(backendConnectorProvider.getBackendConnectors())) {
            throw new ConfigurationException("No Plugin available! Please configure at least one backend plugin in order to run domibus");
        }

        if (domibusConfigurationService.isSingleTenantAware()) {
            LOG.debug("Creating plugin backend filters in Non MultiTenancy environment");
            authUtils.runWithSecurityContext(routingService::createBackendFilters,
                    "domibus", "domibus", AuthRole.ROLE_AP_ADMIN, true);
            return;
        }

        // multitenancy
        final List<Domain> domains = domainService.getDomains();
        LOG.debug("Checking and updating the configured plugins for all the domains in MultiTenancy environment");
        for (Domain domain : domains) {
            LOG.debug("Checking and updating the configured plugins for domain [{}]", domain);

            Runnable wrappedCreateBackendFilters = () -> authUtils.runWithSecurityContext(
                    routingService::createBackendFilters, "domibus",
                    "domibus", AuthRole.ROLE_AP_ADMIN, true);
            //wait 3 minutes to complete the task; the actual execution of the business logic is fast but
            //sometime at server startup it might take a while to have enough threads available
            domainTaskExecutor.submit(wrappedCreateBackendFilters, domain, true, 3L, TimeUnit.MINUTES);

            LOG.debug("Finished checking and updating the configured plugins for domain [{}]", domain);
        }

        LOG.info("Finished checking and updating the configured plugins");
    }
}
