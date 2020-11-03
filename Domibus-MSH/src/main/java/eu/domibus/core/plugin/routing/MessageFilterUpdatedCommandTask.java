package eu.domibus.core.plugin.routing;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class MessageFilterUpdatedCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(MessageFilterUpdatedCommandTask.class);

    protected DomainContextProvider domainContextProvider;
    protected RoutingService routingService;

    public MessageFilterUpdatedCommandTask(DomainContextProvider domainContextProvider, RoutingService routingService) {
        this.domainContextProvider = domainContextProvider;
        this.routingService = routingService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.MESSAGE_FILTER_UPDATE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Message filter update command");

        routingService.invalidateBackendFiltersCache();

    }
}
