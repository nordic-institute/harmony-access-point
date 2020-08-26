package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.MapUtils;
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


    public CommandServiceImpl(CommandDao commandDao,
                              DomainCoreConverter domainConverter,
                              ServerInfoService serverInfoService) {
        this.commandDao = commandDao;
        this.domainConverter = domainConverter;
        this.serverInfoService = serverInfoService;
    }

    @Override
    public void createClusterCommand(String command, String domain, String server, Map<String, String> commandProperties) {
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

    public void deleteCommand(Integer commandId) {
        final CommandEntity commandEntity = commandDao.read(commandId);
        if (commandEntity == null) {
            return;
        }
        commandDao.delete(commandEntity);

    }

    /**
     * just extract all message properties (of type {@code String})
     * excepting Command and Domain
     *
     * @param messageProperties
     * @return
     */
    protected Map<String, String> getCommandProperties(Map<String, String> messageProperties) {
        HashMap<String, String> properties = new HashMap<>();

        if (MapUtils.isEmpty(messageProperties)) {
            LOG.trace("Provided message properties is empty");
            return properties;
        }
        for (Map.Entry<String, String> entry : messageProperties.entrySet()) {
            if (!Command.COMMAND.equalsIgnoreCase(entry.getKey()) && !MessageConstants.DOMAIN.equalsIgnoreCase(entry.getKey())
                    && messageProperties.get(entry.getKey()) instanceof String) {
                properties.put(entry.getKey(), (String) messageProperties.get(entry.getKey()));
            }
        }
        return properties;
    }
}
