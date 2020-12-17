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

import java.util.HashMap;
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
    public void executeCommand(String commandName, Map<String, String> properties) {
        properties.put(Command.COMMAND, commandName);
        LOGGER.debug("Added command name [{}] to the command properties", commandName);

        setDomain(properties);

        Map<String, String> commandProperties = new HashMap<>(properties);
        signalService.sendMessage(commandProperties);
    }

    protected void setDomain(Map<String, String> properties) {
        String domain = (String) properties.get(MessageConstants.DOMAIN);
        if (StringUtils.isNotBlank(domain)) {
            LOGGER.debug("Domain is already added to the properties");
            return;
        }

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        if(currentDomain == null) {
            LOGGER.debug("Could not set domain property: domain is null");
            return;
        }

        properties.put(MessageConstants.DOMAIN, currentDomain.getCode());
        LOGGER.debug("Added domain [{}] to the command properties", currentDomain);
    }
}
