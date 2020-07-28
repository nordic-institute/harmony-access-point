package eu.domibus.ext.delegate.services.command;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.ext.services.CommandExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class CommandExtServiceDelegate implements CommandExtService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CommandExtServiceDelegate.class);

    protected SignalService signalService;
    protected DomainContextProvider domainContextProvider;

    public CommandExtServiceDelegate(SignalService signalService, DomainContextProvider domainContextProvider) {
        this.signalService = signalService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public void executeCommand(String commandName, Map<String, Object> properties) {
        properties.put(Command.COMMAND, commandName);
        LOGGER.debug("Added command name [{}] to the command properties", commandName);

        String domain = (String) properties.get(MessageConstants.DOMAIN);
        if (StringUtils.isBlank(domain)) {
            Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
            properties.put(MessageConstants.DOMAIN, currentDomain.getCode());
            LOGGER.debug("Added domain [{}] to the command properties", currentDomain);
        }

        signalService.sendMessage(properties);
    }
}
