//package eu.domibus.core.plugin.handler;
//
//import eu.domibus.api.message.UserMessageSecurityService;
//import eu.domibus.api.message.validation.UserMessageValidatorSpiService;
//import eu.domibus.api.model.MessageStatus;
//import eu.domibus.api.model.PartInfo;
//import eu.domibus.api.model.UserMessage;
//import eu.domibus.api.model.UserMessageLog;
//import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
//import eu.domibus.api.payload.PartInfoService;
//import eu.domibus.api.security.AuthUtils;
//import eu.domibus.api.usermessage.UserMessageDownloadEvent;
//import eu.domibus.common.ErrorResult;
//import eu.domibus.core.error.ErrorLogService;
//import eu.domibus.core.message.*;
//import eu.domibus.core.message.dictionary.MpcDictionaryService;
//import eu.domibus.core.message.splitandjoin.SplitAndJoinHelper;
//import eu.domibus.core.pmode.PModeDefaultService;
//import eu.domibus.logging.DomibusLogger;
//import eu.domibus.logging.DomibusLoggerFactory;
//import eu.domibus.logging.MDCKey;
//import eu.domibus.messaging.MessageConstants;
//import eu.domibus.messaging.MessageNotFoundException;
//import eu.domibus.messaging.MessagingProcessingException;
//import eu.domibus.plugin.Submission;
//import eu.domibus.plugin.handler.MessagePuller;
//import eu.domibus.plugin.handler.MessageRetriever;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * This class is responsible for handling the plugins requests for all the operations exposed.
// * During submit, it manages the user authentication and the AS4 message's validation, compression and saving.
// * During download, it manages the user authentication and the AS4 message's reading, data clearing and status update.
// *
// * @author Christian Koch, Stefan Mueller, Federico Martini, Ioana Dragusanu
// * @author Cosmin Baciu
// * @since 3.0
// */
//@Service
//public class DatabaseMessageHandler implements MessageRetriever, MessagePuller {
//
//    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandler.class);
//
//    @Autowired
//    protected AuthUtils authUtils;
//
//    @Autowired
//    protected UserMessageDefaultService userMessageService;
//
//    @Autowired
//    protected SplitAndJoinHelper splitAndJoinHelper;
//
//    @Autowired
//    protected PModeDefaultService pModeDefaultService;
//
//    @Autowired
//    private MessagingService messagingService;
//
//    @Autowired
//    private UserMessageLogDefaultService userMessageLogService;
//
//    @Autowired
//    private ErrorLogService errorLogService;
//
//    @Autowired
//    private MessageExchangeService messageExchangeService;
//
//    @Autowired
//    protected MessageFragmentDao messageFragmentDao;
//
//    @Autowired
//    protected MpcDictionaryService mpcDictionaryService;
//
//    @Autowired
//    protected UserMessageHandlerServiceImpl userMessageHandlerService;
//
//    @Autowired
//    protected UserMessageValidatorSpiService userMessageValidatorSpiService;
//
//    @Autowired
//    protected UserMessageSecurityService userMessageSecurityService;
//
//    @Autowired
//    protected PartInfoService partInfoService;
//
//    @Autowired
//    protected ApplicationEventPublisher applicationEventPublisher;
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
//    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
//        LOG.info("Downloading message with id [{}]", messageId);
//        final UserMessage userMessage = userMessageService.getByMessageId(messageId);
//        final UserMessageLog messageLog = userMessageLogService.findById(userMessage.getEntityId());
//
//        return getSubmission(userMessage, messageLog);
//    }
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
//    public Submission downloadMessage(final Long messageEntityId) throws MessageNotFoundException {
//        LOG.info("Downloading message with entity id [{}]", messageEntityId);
//        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
//        final UserMessageLog messageLog = userMessageLogService.findById(messageEntityId);
//        publishDownloadEvent(userMessage.getMessageId());
//        return getSubmission(userMessage, messageLog);
//    }
//
//    /**
//     * Publishes a download event to be caught in case of transaction rollback
//     *
//     * @param messageId message id of the message that is being downloaded
//     */
//    protected void publishDownloadEvent(String messageId) {
//        UserMessageDownloadEvent downloadEvent = new UserMessageDownloadEvent();
//        downloadEvent.setMessageId(messageId);
//        applicationEventPublisher.publishEvent(downloadEvent);
//    }
//
//    protected Submission getSubmission(final UserMessage userMessage, final UserMessageLog messageLog) {
//        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
//            LOG.debug("Message [{}] is already downloaded", userMessage.getMessageId());
//            return messagingService.getSubmission(userMessage);
//        }
//        checkMessageAuthorization(userMessage);
//
//
//        userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
//
//        return messagingService.getSubmission(userMessage);
//    }
//
//    @Override
//    public Submission browseMessage(String messageId) {
//        LOG.info("Browsing message with id [{}]", messageId);
//
//        UserMessage userMessage = userMessageService.getByMessageId(messageId);
//
//        checkMessageAuthorization(userMessage);
//        return messagingService.getSubmission(userMessage);
//    }
//
//
//    @Override
//    public Submission browseMessage(final Long messageEntityId) {
//        LOG.info("Browsing message with entity id [{}]", messageEntityId);
//
//        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
//
//        checkMessageAuthorization(userMessage);
//        return messagingService.getSubmission(userMessage);
//
//    }
//
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
//
//
//    @Override
//    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
//        try {
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
//        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
//            LOG.debug(e.getMessage());
//            return eu.domibus.common.MessageStatus.NOT_FOUND;
//        }
//        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageId);
//        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
//    }
//
//    @Override
//    public eu.domibus.common.MessageStatus getStatus(final Long messageEntityId) {
//        try {
//            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageEntityId);
//        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
//            LOG.debug(e.getMessage());
//            return eu.domibus.common.MessageStatus.NOT_FOUND;
//        }
//        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageEntityId);
//        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
//    }
//
//
//    @Override
//    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
//        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
//        return errorLogService.getErrors(messageId);
//    }
//
//    @Override
//    @Transactional
//    public void initiatePull(String mpc) {
//        messageExchangeService.initiatePullRequest(mpc);
//    }
//
//}
