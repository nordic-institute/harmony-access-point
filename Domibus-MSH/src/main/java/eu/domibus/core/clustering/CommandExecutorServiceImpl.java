package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandExecutorService;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author idragusa
 * @since 4.2
 */
@Service
public class CommandExecutorServiceImpl implements CommandExecutorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandExecutorServiceImpl.class);

    @Autowired
    protected CommandService commandService;

    @Override
    public void executeCommands(String serverName, Domain domain) {
        LOG.debug("Executing comamnds for server [{}] ...", serverName);

        final List<Command> commandsByServerName = commandService.findCommandsByServerAndDomainName(serverName, domain.getCode());
        if (CollectionUtils.isEmpty(commandsByServerName)) {
            LOG.debug("commandsByServerName is null");
            return;
        }
        for (Command command : commandsByServerName) {
            commandService.executeAndDeleteCommand(command, domain);
        }
    }

}
