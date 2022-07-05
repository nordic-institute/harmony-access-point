package eu.domibus.core.plugin.handler;

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
import org.springframework.beans.factory.annotation.Autowired;
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

//    @Autowired
//    protected AuthUtils authUtils;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private ErrorLogService errorLogService;

//    @Autowired
//    protected UserMessageSecurityService userMessageSecurityService;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);
        final UserMessage userMessage = userMessageService.getByMessageId(messageId);

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
        LOG.info("Browsing message with id [{}]", messageId);

        UserMessage userMessage = userMessageService.getByMessageId(messageId);

//        checkMessageAuthorization(userMessage);

        return messagingService.getSubmission(userMessage);
    }


    @Override
    public Submission browseMessage(final Long messageEntityId) {
        LOG.info("Browsing message with entity id [{}]", messageEntityId);

        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

//        checkMessageAuthorization(userMessage);

        return messagingService.getSubmission(userMessage);

    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
//        try {
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
//        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
//            LOG.debug(e.getMessage());
//            return eu.domibus.common.MessageStatus.NOT_FOUND;
//        }
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final Long messageEntityId) {
//        try {
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageEntityId);
//        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
//            LOG.debug(e.getMessage());
//            return eu.domibus.common.MessageStatus.NOT_FOUND;
//        }
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageEntityId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }


    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
//        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
        return errorLogService.getErrors(messageId);
    }

    protected Submission getSubmission(final UserMessage userMessage) {
        final UserMessageLog messageLog = userMessageLogService.findById(userMessage.getEntityId());

        publishDownloadEvent(userMessage.getMessageId());

        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", userMessage.getMessageId());
            return messagingService.getSubmission(userMessage);
        }

//        checkMessageAuthorization(userMessage);

        userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);

        return messagingService.getSubmission(userMessage);
    }

    /**
     * Publishes a download event to be caught in case of transaction rollback
     *
     * @param messageId message id of the message that is being downloaded
     */
    protected void publishDownloadEvent(String messageId) {
        UserMessageDownloadEvent downloadEvent = new UserMessageDownloadEvent();
        downloadEvent.setMessageId(messageId);
        applicationEventPublisher.publishEvent(downloadEvent);
    }

    //    protected void checkMessageAuthorization(UserMessage userMessage) {
//        if (!authUtils.isUnsecureLoginAllowed()) {
//            authUtils.hasUserOrAdminRole();
//        }
//
//        String originalUser = authUtils.getOriginalUserWithUnsecureLoginAllowed();
//        String displayUser = originalUser == null ? "super user" : originalUser;
//        LOG.debug("Authorized as [{}]", displayUser);
//
//        // Authorization check
//        userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
//    }
}
