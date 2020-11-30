package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.queue.WSSendMessageListener;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Francois Gautier
 * @since 5.0
 */
@Service
public class WSPluginBackendRetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginBackendRetryService.class);

    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    protected JMSExtService jmsExtService;

    protected Queue wsPluginSendQueue;

    public WSPluginBackendRetryService(WSBackendMessageLogDao wsBackendMessageLogDao,
                                       JMSExtService jmsExtService,
                                       @Qualifier("wsPluginSendQueue") Queue wsPluginSendQueue) {
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
        this.jmsExtService = jmsExtService;
        this.wsPluginSendQueue = wsPluginSendQueue;
    }

    public List<WSBackendMessageLogEntity> getMessagesNotAlreadyScheduled() {
        List<WSBackendMessageLogEntity> result = new ArrayList<>();

        final List<WSBackendMessageLogEntity> messageIdsToSend = wsBackendMessageLogDao.findRetryMessages();
        if (CollectionUtils.isEmpty(messageIdsToSend)) {
            LOG.trace("No backend message found to be resend");
            return result;
        }
        LOG.trace("Found [{}] backend messages to be send.", messageIdsToSend.size());

        return messageIdsToSend;
    }

    @Transactional
    public void sendWaitingForRetry() {
        try {
            final List<WSBackendMessageLogEntity> messagesNotAlreadyQueued = getMessagesNotAlreadyScheduled();

            for (final WSBackendMessageLogEntity backendMessage : messagesNotAlreadyQueued) {
                sendToQueue(backendMessage);
            }
        } catch (Exception e) {
            LOG.error("Error while sending notifications.", e);
        }
    }

    private void sendToQueue(WSBackendMessageLogEntity backendMessage) {
        final JmsMessageDTO jmsMessage = JMSMessageDTOBuilder.
                create()
                .property(MessageConstants.MESSAGE_ID, backendMessage.getMessageId())
                .property(WSSendMessageListener.ID, backendMessage.getEntityId())
                .property(WSSendMessageListener.TYPE, backendMessage.getType().name())
                .build();
        jmsExtService.sendMessageToQueue(jmsMessage, wsPluginSendQueue);
    }

    @Transactional
    public void send(String messageId, String recipient, WSPluginDispatchRule rule, WSBackendMessageType messageType) {
        WSBackendMessageLogEntity backendMessage = getWsBackendMessageLogEntity(messageType, messageId, recipient, rule);
        WSBackendMessageLogEntity persistedBackendMessage = wsBackendMessageLogDao.createEntity(backendMessage);
        sendToQueue(persistedBackendMessage);
    }

    protected WSBackendMessageLogEntity getWsBackendMessageLogEntity(WSBackendMessageType messageType, String messageId, String recipient, WSPluginDispatchRule rule) {
        WSBackendMessageLogEntity wsBackendMessageLogEntity = new WSBackendMessageLogEntity();
        wsBackendMessageLogEntity.setMessageId(messageId);
        wsBackendMessageLogEntity.setRuleName(rule.getRuleName());
        wsBackendMessageLogEntity.setFinalRecipient(recipient);
        wsBackendMessageLogEntity.setType(messageType);
        wsBackendMessageLogEntity.setSendAttempts(0);
        wsBackendMessageLogEntity.setSendAttemptsMax(rule.getRetryCount());
        return wsBackendMessageLogEntity;
    }
}
