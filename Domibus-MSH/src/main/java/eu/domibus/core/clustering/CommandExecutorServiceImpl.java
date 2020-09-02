package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandExecutorService;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
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

    public CommandExecutorServiceImpl(CommandService commandService,
                                      ServerInfoService serverInfoService,
                                      List<CommandTask> commandTasks,
                                      @Autowired(required = false) List<CommandExtTask> pluginCommands) {
        this.commandService = commandService;
        this.serverInfoService = serverInfoService;
        this.commandTasks = commandTasks;
        this.pluginCommands = pluginCommands;
    }

    @Override
    public void executeCommands(String serverName) {
        LOG.debug("Executing commands for server [{}] ...", serverName);

        final List<Command> commandsByServerName = commandService.findCommandsByServerName(serverName);
        if (CollectionUtils.isEmpty(commandsByServerName)) {
            LOG.debug("No commands found for server [{}]", serverName);
            return;
        }
        for (Command command : commandsByServerName) {
            try {
                executeAndDeleteCommand(command);
            } catch (RuntimeException e) {
                LOG.error("Error executing command [{}]", command.getCommandName(), e);
            }
        }
    }

    @Override
    public void executeCommand(String command, Map<String, String> commandProperties) {
        if (skipCommandSameServer(command, commandProperties)) {
            LOG.trace("Skip the execution of command [{}] as it is executing of the same server", command);
            return;
        }
        LOG.debug("Executing command [{}] having properties [{}]", command, commandProperties);
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

    public void executeAndDeleteCommand(Command command) {
        if (command == null) {
            LOG.warn("Attempting to execute and delete a null command");
            return;
        }
        LOG.debug("Execute command [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties());
        executeCommand(command.getCommandName(), command.getCommandProperties());
        LOG.debug("Delete command [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties());
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

    protected boolean skipCommandSameServer(final String command, Map<String, String> commandProperties) {
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
            LOG.debug("Command [{}] not executed as origin and actual server signature is the same [{}]", command, serverName);
            return true;
        }
        return false;
    }
}
