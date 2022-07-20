package eu.domibus.core.plugin.handler;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.usermessage.UserMessageDownloadEvent;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service used for retrieving messages (split from DatabaseMessageHandler)
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MessageRetrieverImpl implements MessageRetriever {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetrieverImpl.class);

    protected final UserMessageDefaultService userMessageService;

    private final MessagingService messagingService;

    private final UserMessageLogDefaultService userMessageLogService;

    private final ErrorLogService errorLogService;

    protected final ApplicationEventPublisher applicationEventPublisher;

    public MessageRetrieverImpl(UserMessageDefaultService userMessageService, MessagingService messagingService, UserMessageLogDefaultService userMessageLogService,
                                ErrorLogService errorLogService, ApplicationEventPublisher applicationEventPublisher) {
        this.userMessageService = userMessageService;
        this.messagingService = messagingService;
        this.userMessageLogService = userMessageLogService;
        this.errorLogService = errorLogService;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);
        final UserMessage userMessage = userMessageService.getByMessageId(messageId, MSHRole.RECEIVING);

        return getSubmission(userMessage);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final Long messageEntityId) throws MessageNotFoundException {
        LOG.info("Downloading message with entity id [{}]", messageEntityId);
        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

        return getSubmission(userMessage);
    }

    @Override
    public Submission browseMessage(String messageId) {
        try {
            return browseMessage(messageId, eu.domibus.common.MSHRole.RECEIVING);
        } catch (eu.domibus.api.messaging.MessageNotFoundException ex) {
            return browseMessage(messageId, eu.domibus.common.MSHRole.SENDING);
        }
    }

    @Override
    public Submission browseMessage(String messageId, eu.domibus.common.MSHRole mshRole) throws eu.domibus.api.messaging.MessageNotFoundException {
        LOG.info("Browsing message with id [{}] and role [{}]", messageId, mshRole);

        MSHRole role = MSHRole.valueOf(mshRole.name());
        UserMessage userMessage = userMessageService.getByMessageId(messageId, role);

        return messagingService.getSubmission(userMessage);
    }

    @Override
    public Submission browseMessage(final Long messageEntityId) {
        LOG.info("Browsing message with entity id [{}]", messageEntityId);

        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

        return messagingService.getSubmission(userMessage);
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
        //try both
        eu.domibus.common.MessageStatus status = getStatus(messageId, eu.domibus.common.MSHRole.RECEIVING);
        if (status != eu.domibus.common.MessageStatus.NOT_FOUND) {
            return status;
        }
        return getStatus(messageId, eu.domibus.common.MSHRole.SENDING);
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageId, role);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final Long messageEntityId) {
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageEntityId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        List<? extends ErrorResult> errors = getErrorsForMessage(messageId, eu.domibus.common.MSHRole.RECEIVING);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }
        return getErrorsForMessage(messageId, eu.domibus.common.MSHRole.SENDING);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());
        return errorLogService.getErrors(messageId, role);
    }

    protected Submission getSubmission(final UserMessage userMessage) {
        final UserMessageLog messageLog = userMessageLogService.findById(userMessage.getEntityId());

        publishDownloadEvent(userMessage.getMessageId(), userMessage.getMshRole().getRole());

        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", userMessage.getMessageId());
            return messagingService.getSubmission(userMessage);
        }

        userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);

        return messagingService.getSubmission(userMessage);
    }

    /**
     * Publishes a download event to be caught in case of transaction rollback
     *
     * @param messageId message id of the message that is being downloaded
     * @param role
     */
    protected void publishDownloadEvent(String messageId, MSHRole role) {
        UserMessageDownloadEvent downloadEvent = new UserMessageDownloadEvent();
        downloadEvent.setMessageId(messageId);
        downloadEvent.setMshRole(role.name());
        applicationEventPublisher.publishEvent(downloadEvent);
    }

}
