package eu.domibus.core.plugin.handler;

import eu.domibus.api.model.*;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service used for submitting messages (split from MessageSubmitterImpl)
 *
 * @author Ion Perpegel
 * @since 5.1
 */
@Service
public class MessageSubmitterHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSubmitterHelper.class);

    protected final UserMessageDefaultService userMessageService;

    protected final PModeDefaultService pModeDefaultService;

    private final MessagingService messagingService;

    private final UserMessageLogDefaultService userMessageLogService;

    private final PullMessageService pullMessageService;

    public MessageSubmitterHelper(UserMessageDefaultService userMessageService,
                                  PModeDefaultService pModeDefaultService, MessagingService messagingService,
                                  UserMessageLogDefaultService userMessageLogService,
                                  PullMessageService pullMessageService) {
        this.userMessageService = userMessageService;
        this.pModeDefaultService = pModeDefaultService;
        this.messagingService = messagingService;
        this.userMessageLogService = userMessageLogService;
        this.pullMessageService = pullMessageService;
    }

    @Transactional
    @Timer(clazz = MessageSubmitterHelper.class, value = "persistSentMessage")
    @Counter(clazz = MessageSubmitterHelper.class, value = "persistSentMessage")
    public void persistSentMessage(UserMessage userMessage, MessageStatus messageStatus, List<PartInfo> partInfos, String pModeKey, LegConfiguration legConfiguration, final String backendName) {
        messagingService.saveUserMessageAndPayloads(userMessage, partInfos);

        final boolean sourceMessage = userMessage.isSourceMessage();
        final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration),
                backendName);
        if (!sourceMessage) {
            prepareForPushOrPull(userMessage, userMessageLog, pModeKey, messageStatus);
        }
    }

    public void prepareForPushOrPull(UserMessage userMessage, UserMessageLog userMessageLog, String pModeKey, MessageStatus messageStatus) {
        if (MessageStatus.READY_TO_PULL != messageStatus) {
            // Sends message to the proper queue if not a message to be pulled.
            userMessageService.scheduleSending(userMessage, userMessageLog);
        } else {
            LOG.debug("[submit]:Message:[{}] add lock", userMessage.getMessageId());
            pullMessageService.addPullMessageLock(userMessage, userMessage.getPartyInfo().getToParty(), pModeKey, userMessageLog);
        }
    }

    public int getMaxAttempts(LegConfiguration legConfiguration) {
        return (legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount()) + 1; // counting retries after the first send attempt
    }

}
