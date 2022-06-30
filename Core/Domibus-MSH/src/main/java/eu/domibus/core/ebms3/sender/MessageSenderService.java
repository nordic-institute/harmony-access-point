package eu.domibus.core.ebms3.sender;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.*;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
    private UserMessageDao userMessageDao;

    @Autowired
    UserMessageLogDao userMessageLogDao;

    @Autowired
    MessageSenderFactory messageSenderFactory;

    @Autowired
    protected ReliabilityService reliabilityService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected PModeProvider pModeProvider;

    @Timer(clazz = MessageSenderService.class,value ="sendUserMessage" )
    @Counter(clazz = MessageSenderService.class,value ="sendUserMessage" )
    public void sendUserMessage(final String messageId, Long messageEntityId, int retryCount) {
        final UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(messageEntityId);
        MessageStatus messageStatus = getMessageStatus(userMessageLog);

        if (MessageStatus.NOT_FOUND == messageStatus) {
            if (retryCount < MAX_RETRY_COUNT) {
                userMessageService.scheduleSending(userMessageLog, retryCount + 1);
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

        final UserMessage userMessage = userMessageDao.findByEntityId(messageEntityId);
        final MessageSender messageSender = messageSenderFactory.getMessageSender(userMessage);
        final Boolean testMessage = userMessageHandlerService.checkTestMessage(userMessage);

        LOG.businessInfo(testMessage ? DomibusMessageCode.BUS_TEST_MESSAGE_SEND_INITIATION : DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION,
                userMessage.getPartyInfo().getFromParty(), userMessage.getPartyInfo().getToParty());


        if (MessageStatus.WAITING_FOR_RETRY == messageStatus) {
            // check if the destination is available. If not, then don't do the retry (see EDELIVERY-9563, EDELIVERY-9084)
            String destinationConnetivityStatus = reliabilityService.getPartyState(userMessage.getPartyInfo().getToParty());
            if (!"SUCCESS".equals(destinationConnetivityStatus)) {
                LOG.warn("Retry attempt for message [{}] skipped because destination party [{}] is not yet reachable. Calculating the next attempt ...", messageId, userMessage.getPartyInfo().getToParty());
                MessageAttempt attempt = new MessageAttempt();
                attempt.setError("Destination party not reachable");
                attempt.setStatus(MessageAttemptStatus.ERROR);
                attempt.setMessageId(messageId);
                attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
                LegConfiguration legConfiguration = null;
                try {
                    String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
                    legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
                    reliabilityService.handleReliability(userMessage, userMessageLog, ReliabilityChecker.CheckResult.SEND_FAIL,
                            null, null, null,
                            legConfiguration, attempt); // recalculate next attempt for retry
                } catch (EbMS3Exception e) {
                    // TODO Razvan
                }
                return; //skip the retry altogether
            }
        }
        messageSender.sendMessage(userMessage, userMessageLog);
    }

    protected MessageStatus getMessageStatus(final UserMessageLog userMessageLog) {
        if (userMessageLog == null) {
            return MessageStatus.NOT_FOUND;
        }
        return userMessageLog.getMessageStatus();
    }

}
