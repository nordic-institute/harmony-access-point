package eu.domibus.core.ebms3.sender;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;


/**
 * Entrypoint for sending AS4 messages to C3. Contains common validation and rescheduling logic
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class MessageSenderService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderService.class);

    private static final Set<MessageStatus> ALLOWED_STATUSES_FOR_SENDING = EnumSet.of(MessageStatus.SEND_ENQUEUED, MessageStatus.WAITING_FOR_RETRY);
    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private UserMessageDefaultService userMessageService;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessageSenderFactory messageSenderFactory;

    @Autowired
    protected ReliabilityService reliabilityService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    public void sendUserMessage(final String messageId, int retryCount, boolean isSplitAndJoin) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        MessageStatus messageStatus = getMessageStatus(userMessageLog);

        if (MessageStatus.NOT_FOUND == messageStatus) {
            if (retryCount < MAX_RETRY_COUNT) {
                userMessageService.scheduleSending(messageId, retryCount + 1, isSplitAndJoin);
                LOG.warn("MessageStatus NOT_FOUND, retry count is [{}] -> reschedule sending", retryCount);
                return;
            }
            LOG.warn("Message [{}] has a status [{}] for [{}] times and will not be sent", messageId, MessageStatus.NOT_FOUND, retryCount);
            return;
        }

        if (!ALLOWED_STATUSES_FOR_SENDING.contains(messageStatus)) {
            LOG.warn("Message [{}] has a status [{}] which is not allowed for sending. Only the statuses [{}] are allowed", messageId, messageStatus, ALLOWED_STATUSES_FOR_SENDING);
            return;
        }

        final Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        final UserMessage userMessage = messaging.getUserMessage();
        final MessageSender messageSender = messageSenderFactory.getMessageSender(userMessage);
        final Boolean testMessage = userMessageHandlerService.checkTestMessage(userMessage);


        LOG.businessInfo(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_INITIATION : DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION,
                userMessage.getFromFirstPartyId(), userMessage.getToFirstPartyId());

        messageSender.sendMessage(messaging, userMessageLog);
    }

    protected MessageStatus getMessageStatus(final UserMessageLog userMessageLog) {
        if (userMessageLog == null) {
            return MessageStatus.NOT_FOUND;
        }
        return userMessageLog.getMessageStatus();
    }

}
