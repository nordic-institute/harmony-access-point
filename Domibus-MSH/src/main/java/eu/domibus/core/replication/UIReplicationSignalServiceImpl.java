package eu.domibus.core.replication;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_REPLICATION_ENABLED;

/**
 * Implementation for {@code UIReplicationSignalService}
 *
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0
 */
@Service
public class UIReplicationSignalServiceImpl implements UIReplicationSignalService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationSignalServiceImpl.class);

    static final String UI_REPLICATION_ENABLED = DOMIBUS_UI_REPLICATION_ENABLED;

    @Autowired
    @Qualifier("uiReplicationQueue")
    private Queue uiReplicationQueue;

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    /**
     * just loads the properties value for switching on/off the UI Replication
     *
     * @return boolean replication enabled or not
     */
    @Override
    public boolean isReplicationEnabled() {
        boolean uiReplicationEnabled = Boolean.parseBoolean(domibusPropertyProvider.getProperty(UI_REPLICATION_ENABLED));

        if (!uiReplicationEnabled) {
            LOG.debug("UIReplication is disabled - no processing will occur");
        }
        return uiReplicationEnabled;
    }

    @Override
    public void userMessageReceived(String messageId) {
        checkReplicationStatusAndSendMessage(messageId, UIJMSType.USER_MESSAGE_RECEIVED);
    }

    @Override
    public void userMessageSubmitted(String messageId) {
        checkReplicationStatusAndSendMessage(messageId, UIJMSType.USER_MESSAGE_SUBMITTED);
    }

    @Override
    public void messageChange(String messageId) {
        checkReplicationStatusAndSendMessage(messageId, UIJMSType.MESSAGE_CHANGE);
    }

    @Override
    public void signalMessageSubmitted(String messageId) {
        checkReplicationStatusAndSendMessage(messageId, UIJMSType.SIGNAL_MESSAGE_SUBMITTED);
    }

    @Override
    public void signalMessageReceived(String messageId) {
        checkReplicationStatusAndSendMessage(messageId, UIJMSType.SIGNAL_MESSAGE_RECEIVED);
    }


    void checkReplicationStatusAndSendMessage(final String messageId, UIJMSType uiJMSType) {
        if (!isReplicationEnabled()) {
            return;
        }
        final JmsMessage message = createJMSMessage(messageId, uiJMSType);

        jmsManager.sendMapMessageToQueue(message, uiReplicationQueue);
    }

    JmsMessage createJMSMessage(String messageId, UIJMSType uiJMSType) {
        return JMSMessageBuilder.create()
                .type(uiJMSType.name())
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.DOMAIN, domainContextProvider.getCurrentDomain().getCode())
                .build();
    }

}
