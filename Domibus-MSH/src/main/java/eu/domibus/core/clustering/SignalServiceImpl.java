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

    protected final JMSManager jmsManager;

    protected final Topic clusterCommandTopic;

    protected final DomainContextProvider domainContextProvider;

    protected final DomibusConfigurationService domibusConfigurationService;

    public SignalServiceImpl(JMSManager jmsManager, Topic clusterCommandTopic, DomainContextProvider domainContextProvider, DomibusConfigurationService domibusConfigurationService) {
        this.jmsManager = jmsManager;

        this.clusterCommandTopic = clusterCommandTopic;
        this.domainContextProvider = domainContextProvider;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public void signalTrustStoreUpdate(Domain domain) {
        LOG.debug("Signaling truststore update on [{}] domain", domain);

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_TRUSTSTORE);
        commandProperties.put(MessageConstants.DOMAIN, domain.getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalKeyStoreUpdate(Domain domain) {
        LOG.debug("Signaling keystore update on [{}] domain", domain);

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.RELOAD_KEYSTORE);
        commandProperties.put(MessageConstants.DOMAIN, domain.getCode());

        sendMessage(commandProperties);
    }

    @Override
    public void signalPModeUpdate() {
        LOG.debug("Signaling PMode update on [{}] domain", domainContextProvider.getCurrentDomain().getCode());

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

        signalOperation(domainCode, Command.MESSAGE_FILTER_UPDATE);
    }

    @Override
    public void signalSessionInvalidation(String userName) {
        LOG.debug("Signaling user session invalidation for user", userName);
        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, Command.USER_SESSION_INVALIDATION);
        commandProperties.put(CommandProperty.USER_NAME, userName);

        sendMessage(commandProperties);
    }

    @Override
    public void signalClearCaches() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = domain == null ? null : domain.getCode();

        signalOperation(domainCode, Command.EVICT_CACHES);
    }

    @Override
    public void signalClear2LCCaches() {
        Domain domain = domainContextProvider.getCurrentDomainSafely();
        String domainCode = domain == null ? null : domain.getCode();

        signalOperation(domainCode, Command.EVICT_2LC_CACHES);
    }

    @Override
    public void signalTLSTrustStoreUpdate(Domain domain) {
        String domainCode = domain == null ? null : domain.getCode();
        signalOperation(domainCode, Command.RELOAD_TLS_TRUSTSTORE);
    }

    @Override
    public void signalDomainsAdded(String domainCode) {
        signalOperation(domainCode, Command.DOMAIN_ADDED);
    }

    @Override
    public void signalDomainsRemoved(String domainCode) {
        signalOperation(domainCode, Command.DOMAIN_REMOVED);
    }

    private void signalOperation(String domainCode, String command) {
        LOG.debug("Signaling [{}] command on [{}] domain", command, domainCode);

        Map<String, String> commandProperties = new HashMap<>();
        commandProperties.put(Command.COMMAND, command);
        commandProperties.put(MessageConstants.DOMAIN, domainCode);

        sendMessage(commandProperties);
    }
}
