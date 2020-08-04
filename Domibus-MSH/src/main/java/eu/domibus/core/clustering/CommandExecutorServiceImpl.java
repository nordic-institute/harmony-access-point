package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandExecutorService;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.ext.services.CommandExtTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author idragusa
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class CommandExecutorServiceImpl implements CommandExecutorService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandExecutorServiceImpl.class);

    protected CommandService commandService;
    protected ServerInfoService serverInfoService;
    protected List<CommandTask> commandTasks;
    protected List<CommandExtTask> pluginCommands;
    protected DomainTaskExecutor domainTaskExecutor;

    public CommandExecutorServiceImpl(CommandService commandService,
                                      ServerInfoService serverInfoService,
                                      List<CommandTask> commandTasks,
                                      @Autowired(required = false) List<CommandExtTask> pluginCommands,
                                      DomainTaskExecutor domainTaskExecutor) {
        this.commandService = commandService;
        this.serverInfoService = serverInfoService;
        this.commandTasks = commandTasks;
        this.pluginCommands = pluginCommands;
        this.domainTaskExecutor = domainTaskExecutor;
    }

    @Override
    public void executeCommands(String serverName, Domain domain) {
        LOG.debug("Executing commands for server [{}] ...", serverName);

        final List<Command> commandsByServerName = commandService.findCommandsByServerAndDomainName(serverName, domain.getCode());
        if (CollectionUtils.isEmpty(commandsByServerName)) {
            LOG.debug("No commands found for server [{}] and domain [{}]", serverName, domain.getCode());
            return;
        }
        for (Command command : commandsByServerName) {
            try {
                executeAndDeleteCommand(command, domain);
            } catch (RuntimeException e) {
                LOG.error("Error executing command [{}]", command.getCommandName());
            }

        }
    }

    @Override
    public void executeCommand(String command, Domain domain, Map<String, String> commandProperties) {
        if (skipCommandSameServer(command, domain, commandProperties)) {
            return;
        }
        LOG.debug("Executing command [{}] for domain [{}] having properties [{}]", command, domain, commandProperties);
        CommandTask commandTask = getCommandTask(command);
        if (commandTask != null) {
            LOG.debug("Found command task [{}]", command);
            commandTask.execute(commandProperties);
            return;
        }
        CommandExtTask pluginCommand = getPluginCommand(command);
        if (pluginCommand != null) {
            LOG.debug("Found plugin command task [{}]", command);
            pluginCommand.execute(commandProperties);
            return;
        }
        LOG.error("Unknown command received: [{}]", command);
    }

    public void executeAndDeleteCommand(Command command, Domain domain) {
        if (command == null) {
            LOG.warn("Attempting to execute and delete a null command");
            return;
        }
        LOG.debug("Execute command [{}] [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties(), domain);
        executeCommand(command.getCommandName(), domain, command.getCommandProperties());
        LOG.debug("Delete command [{}] [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties(), domain);
        commandService.deleteCommand(command.getEntityId());
    }

    protected CommandTask getCommandTask(String commandName) {
        return commandTasks.stream().filter(commandTask -> commandTask.canHandle(commandName)).findFirst().orElse(null);
    }

    protected CommandExtTask getPluginCommand(String commandName) {
        if (CollectionUtils.isEmpty(pluginCommands)) {
            LOG.debug("No plugin command tasks found");
            return null;
        }
        return pluginCommands.stream().filter(commandTask -> commandTask.canHandle(commandName)).findFirst().orElse(null);
    }

    protected boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties) {
        if (commandProperties == null) {
            LOG.trace("Skipping command [{}]: no command properties found", command);
            return false;
        }
        String originServerName = commandProperties.get(CommandProperty.ORIGIN_SERVER);
        if (StringUtils.isBlank(originServerName)) {
            LOG.trace("Skipping command [{}]: no origin server found", command);
            return false;
        }
        final String serverName = serverInfoService.getServerName();
        if (serverName.equalsIgnoreCase(originServerName)) {
            LOG.debug("Command [{}] for domain [{}] not executed as origin and actual server signature is the same [{}]", command, domain, serverName);
            return true;
        }
        return false;
    }
}
