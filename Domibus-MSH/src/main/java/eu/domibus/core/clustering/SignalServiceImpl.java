package eu.domibus.core.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for {@link SignalService}
 * We are using a {@JMS topic} implementation
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service
public class SignalServiceImpl implements SignalService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SignalServiceImpl.class);

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    protected Topic clusterCommandTopic;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Override
    public void signalTrustStoreUpdate(Domain domain) {

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
        commandProperties.put(MessageConstants.DOMAIN, domain.getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalPModeUpdate() {

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_PMODE);
        commandProperties.put(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalLoggingSetLevel(String name, String level) {

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.LOGGING_SET_LEVEL);
        commandProperties.put(CommandProperty.LOG_NAME, name);
        commandProperties.put(CommandProperty.LOG_LEVEL, level);

        sendMessage(commandProperties);
    }

    @Override
    public void signalLoggingReset() {

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.LOGGING_RESET);

        sendMessage(commandProperties);
    }

    @Override
    public void signalDomibusPropertyChange(String domainCode, String propertyName, String propertyValue) {
        LOG.debug("Signaling [{}] property change on [{}] domain", propertyName, domainCode);
        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.DOMIBUS_PROPERTY_CHANGE);
        commandProperties.put(MessageConstants.DOMAIN, domainCode);
        commandProperties.put(CommandProperty.PROPERTY_NAME, propertyName);
        commandProperties.put(CommandProperty.PROPERTY_VALUE, propertyValue);

        sendMessage(commandProperties);
    }

    @Override
    public void sendMessage(Map<String, String> commandProperties) {
        if (!domibusConfigurationService.isClusterDeployment()) {
            LOG.debug("No cluster deployment: no need to signal command [{}]", commandProperties.get(Command.COMMAND));
            return;
        }

        JmsMessage jmsMessage = JMSMessageBuilder.create().properties(commandProperties).build();

        // Sends a command message to topic cluster
        jmsManager.sendMessageToTopic(jmsMessage, clusterCommandTopic, true);
    }

    @Override
    public void signalMessageFiltersUpdated() {
        String domainCode = domainContextProvider.getCurrentDomain().getCode();

        LOG.debug("Signaling message filters change [{}] domain", domainCode);

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.MESSAGE_FILTER_UPDATE);
        commandProperties.put(MessageConstants.DOMAIN, domainCode);

        sendMessage(commandProperties);
    }

    @Override
    public void signalSessionInvalidation(String userName) {
        LOG.debug("Signaling user session invalidation for user", userName);
        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.USER_SESSION_INVALIDATION);
        commandProperties.put(CommandProperty.USER_NAME, userName);

        sendMessage(commandProperties);
    }

    public void signalMessageClearCaches() {
        String domainCode = domainContextProvider.getCurrentDomain().getCode();

        LOG.debug("Signaling clearing caches [{}] domain", domainCode);

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.EVICT_CACHES);
        commandProperties.put(MessageConstants.DOMAIN, domainCode);
        sendMessage(commandProperties);
    }
}
