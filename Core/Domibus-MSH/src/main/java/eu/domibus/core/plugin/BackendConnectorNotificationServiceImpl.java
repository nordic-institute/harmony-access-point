package eu.domibus.core.plugin;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.plugin.BackendConnectorNotificationService;
import eu.domibus.core.jms.MessageListenerContainerInitializer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class BackendConnectorNotificationServiceImpl implements BackendConnectorNotificationService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendConnectorNotificationServiceImpl.class);

    protected final DomainContextProvider domainContextProvider;
    protected final MessageListenerContainerInitializer messageListenerContainerInitializer;

    public BackendConnectorNotificationServiceImpl(DomainContextProvider domainContextProvider,
                                                   MessageListenerContainerInitializer messageListenerContainerInitializer) {
        this.domainContextProvider = domainContextProvider;
        this.messageListenerContainerInitializer = messageListenerContainerInitializer;
    }

    @Override
    public void backendConnectorEnabled(String backendName) {
        messageListenerContainerInitializer.createMessageListenersForPlugin(backendName, domainContextProvider.getCurrentDomainSafely());
    }

    @Override
    public void backendConnectorDisabled(String backendName) {
        messageListenerContainerInitializer.destroyMessageListenersForPlugin(backendName, domainContextProvider.getCurrentDomainSafely());
    }
}
