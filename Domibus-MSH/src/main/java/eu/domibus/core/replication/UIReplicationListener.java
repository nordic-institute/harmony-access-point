package eu.domibus.core.replication;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.util.DatabaseUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * JMS listener for the queue {@code domibus.jms.queue.ui.replication}
 *
 * @author Catalin Enache
 * @since 4.0
 */
@Component
public class UIReplicationListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UIReplicationListener.class);

    @Autowired
    private UIReplicationDataService uiReplicationDataService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    private DatabaseUtil databaseUtil;

    @JmsListener(destination = "${domibus.jms.queue.ui.replication}", containerFactory = "uiReplicationJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processUIReplication(final MapMessage map) throws JMSException {

        final String domainCode = map.getStringProperty(MessageConstants.DOMAIN);
        domainContextProvider.setCurrentDomain(domainCode);
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        //disabling read of JMS messages
        if (!uiReplicationSignalService.isReplicationEnabled()) {
            LOG.debug("UIReplication is disabled - no processing will occur");
            return;
        }

        final String messageId = map.getStringProperty(MessageConstants.MESSAGE_ID);
        final String jmsType = map.getJMSType();
        LOG.debug("processUIReplication for messageId=[{}] domain=[{}] jmsType=[{}]", messageId, domainCode, jmsType);

        switch (UIJMSType.valueOf(jmsType)) {
            case USER_MESSAGE_RECEIVED:
                uiReplicationDataService.userMessageReceived(messageId, map.getJMSTimestamp());
                break;
            case USER_MESSAGE_SUBMITTED:
                uiReplicationDataService.userMessageSubmitted(messageId, map.getJMSTimestamp());
                break;
            case MESSAGE_CHANGE:
                uiReplicationDataService.messageChange(messageId, map.getJMSTimestamp());
                break;
            case SIGNAL_MESSAGE_SUBMITTED:
                uiReplicationDataService.signalMessageSubmitted(messageId, map.getJMSTimestamp());
                break;
            case SIGNAL_MESSAGE_RECEIVED:
                uiReplicationDataService.signalMessageReceived(messageId, map.getJMSTimestamp());
                break;
            default:
                throw new AssertionError("Invalid UIJMSType enum value");
        }
    }
}
