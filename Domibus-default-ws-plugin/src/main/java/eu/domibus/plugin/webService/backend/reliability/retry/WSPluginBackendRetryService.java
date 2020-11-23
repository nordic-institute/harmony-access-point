package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Francois Gautier
 * @since 5.0
 */
@Service
public class WSPluginBackendRetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginBackendRetryService.class);

    private final WSPluginMessageSender wsPluginMessageSender;

    private final WSBackendMessageLogDao wsBackendMessageLogDao;

    public WSPluginBackendRetryService(WSPluginMessageSender wsPluginMessageSender,
                                       WSBackendMessageLogDao wsBackendMessageLogDao) {
        this.wsPluginMessageSender = wsPluginMessageSender;
        this.wsBackendMessageLogDao = wsBackendMessageLogDao;
    }

    public List<WSBackendMessageLogEntity> getMessagesNotAlreadyScheduled() {
        List<WSBackendMessageLogEntity> result = new ArrayList<>();

        final List<WSBackendMessageLogEntity> messageIdsToSend = wsBackendMessageLogDao.findRetryMessages();
        if (messageIdsToSend.isEmpty()) {
            LOG.trace("No backend message found to be resend");
            return result;
        }
        LOG.trace("Found [{}] backend messages to be send.", messageIdsToSend.size());

        return messageIdsToSend;
    }

    @Transactional
    public void sendNotifications() {
        try {
            final List<WSBackendMessageLogEntity> messagesNotAlreadyQueued = getMessagesNotAlreadyScheduled();

            for (final WSBackendMessageLogEntity backendMessage : messagesNotAlreadyQueued) {
                wsPluginMessageSender.sendNotification(backendMessage);
            }
        } catch (Exception e) {
            LOG.error("Error while enqueueing messages.", e);
        }
    }

    @Transactional
    public void sendNotification(String messageId, String recipient, WSPluginDispatchRule rule) {
        try {
            wsPluginMessageSender.sendNotification(
                    wsBackendMessageLogDao.createEntity(
                            getWsBackendMessageLogEntity(messageId, recipient, rule)));
        } catch (Exception e) {
            LOG.error("Try catch to be removed with the use of jms queue: JIRA EDELIVERY-7478", e);
        }
    }

    protected WSBackendMessageLogEntity getWsBackendMessageLogEntity(String messageId, String recipient, WSPluginDispatchRule rule) {
        WSBackendMessageLogEntity wsBackendMessageLogEntity = new WSBackendMessageLogEntity();
        wsBackendMessageLogEntity.setMessageId(messageId);
        wsBackendMessageLogEntity.setRuleName(rule.getRuleName());
        wsBackendMessageLogEntity.setFinalRecipient(recipient);
        wsBackendMessageLogEntity.setType(WSBackendMessageType.SEND_SUCCESS);
        wsBackendMessageLogEntity.setSendAttempts(0);
        wsBackendMessageLogEntity.setSendAttemptsMax(rule.getRetryCount());
        return wsBackendMessageLogEntity;
    }
}
