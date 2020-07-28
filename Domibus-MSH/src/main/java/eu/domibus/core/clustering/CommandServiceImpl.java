package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.services.CommandExtTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Service
public class CommandServiceImpl implements CommandService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandServiceImpl.class);

    protected CommandDao commandDao;
    protected DomainCoreConverter domainConverter;
    protected ServerInfoService serverInfoService;

    protected List<CommandTask> commandTasks;
    protected List<CommandExtTask> pluginCommands;

    public CommandServiceImpl(CommandDao commandDao,
                              DomainCoreConverter domainConverter,
                              ServerInfoService serverInfoService,
                              List<CommandTask> commandTasks,
                              @Autowired(required = false) List<CommandExtTask> pluginCommands) {
        this.commandDao = commandDao;
        this.domainConverter = domainConverter;
        this.serverInfoService = serverInfoService;
        this.commandTasks = commandTasks;
        this.pluginCommands = pluginCommands;
    }

    @Override
    public void createClusterCommand(String command, String domain, String server, Map<String, Object> commandProperties) {
        LOG.debug("Creating command [{}] for domain [{}] and server [{}]", command, domain, server);
        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setCommandName(command);
        commandEntity.setDomain(domain);
        commandEntity.setServerName(server);
        commandEntity.setCreationTime(new Date());
        commandEntity.setCommandProperties(getCommandProperties(commandProperties));
        commandDao.create(commandEntity);
    }

    @Override
    public List<Command> findCommandsByServerName(String serverName) {
        final List<CommandEntity> commands = commandDao.findCommandsByServerName(serverName);
        return domainConverter.convert(commands, Command.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Command> findCommandsByServerAndDomainName(String serverName, String domain) {
        LOG.debug("Find commands by serverName [{}] for domain [{}]", serverName, domain);
        final List<CommandEntity> commands = commandDao.findCommandsByServerAndDomainName(serverName, domain);
        LOG.debug("There are [{}] commands", commands.size());
        return domainConverter.convert(commands, Command.class);
    }

    @Override
    public void executeCommand(String command, Domain domain, Map<String, String> commandProperties) {
        //skip the command if runs on same server
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

    @Override
    public void deleteCommand(Integer commandId) {
        final CommandEntity commandEntity = commandDao.read(commandId);
        if (commandEntity == null) {
            return;
        }
        commandDao.delete(commandEntity);
    }

    @Override
    @Transactional
    public void executeAndDeleteCommand(Command command, Domain domain) {
        if (command == null) {
            LOG.warn("Attempting to execute and delete a null command");
            return;
        }
        LOG.debug("Execute command [{}] [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties(), domain);
        executeCommand(command.getCommandName(), domain, command.getCommandProperties());
        LOG.debug("Delete command [{}] [{}] [{}] [{}] ", command.getCommandName(), command.getServerName(), command.getCommandProperties(), domain);
        deleteCommand(command.getEntityId());
    }

    /**
     * just extract all message properties (of type {@code String})
     * excepting Command and Domain
     *
     * @param messageProperties
     * @return
     */
    protected Map<String, String> getCommandProperties(Map<String, Object> messageProperties) {
        HashMap<String, String> properties = new HashMap<>();

        if (MapUtils.isNotEmpty(messageProperties)) {
            for (Map.Entry<String, Object> entry : messageProperties.entrySet()) {
                if (!Command.COMMAND.equalsIgnoreCase(entry.getKey()) && !MessageConstants.DOMAIN.equalsIgnoreCase(entry.getKey())
                        && messageProperties.get(entry.getKey()) instanceof String) {
                    properties.put(entry.getKey(), (String) messageProperties.get(entry.getKey()));
                }
            }
        }
        return properties;
    }

    /**
     * Returns true if the commands is send to same server
     *
     * @param command
     * @param domain
     * @param commandProperties
     * @return
     */
    protected boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties) {
        if (commandProperties == null) {
            //execute the command
            return false;
        }
        String originServerName = commandProperties.get(CommandProperty.ORIGIN_SERVER);
        if (StringUtils.isBlank(originServerName)) {
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
