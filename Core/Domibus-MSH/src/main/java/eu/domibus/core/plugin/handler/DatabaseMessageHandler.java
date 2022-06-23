package eu.domibus.core.plugin.handler;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.validation.UserMessageValidatorSpiService;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageDownloadEvent;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.exception.MessagingExceptionFactory;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.dictionary.MpcDictionaryService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.splitandjoin.SplitAndJoinHelper;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class is responsible for handling the plugins requests for all the operations exposed.
 * During submit, it manages the user authentication and the AS4 message's validation, compression and saving.
 * During download, it manages the user authentication and the AS4 message's reading, data clearing and status update.
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini, Ioana Dragusanu
 * @author Cosmin Baciu
 * @since 3.0
 */
@Service
public class DatabaseMessageHandler implements MessageSubmitter, MessageRetriever, MessagePuller {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandler.class);
    private static final String USER_MESSAGE_IS_NULL = "UserMessage is null";
    private static final String ERROR_SUBMITTING_THE_MESSAGE_STR = "Error submitting the message [";
    private static final String TO_STR = "] to [";

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Autowired
    protected SplitAndJoinHelper splitAndJoinHelper;

    @Autowired
    protected PModeDefaultService pModeDefaultService;

    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private PayloadFileStorageProvider storageProvider;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private BackendMessageValidator backendMessageValidator;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    protected MessageFragmentDao messageFragmentDao;

    @Autowired
    protected MpcDictionaryService mpcDictionaryService;

    @Autowired
    protected UserMessageHandlerServiceImpl userMessageHandlerService;

    @Autowired
    protected UserMessageValidatorSpiService userMessageValidatorSpiService;

    @Autowired
    protected UserMessageSecurityService userMessageSecurityService;

    @Autowired
    protected PartInfoService partInfoService;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    MessageSubmitterService messageSubmitter;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);
        final UserMessage userMessage = userMessageService.getByMessageId(messageId);
        final UserMessageLog messageLog = userMessageLogService.findById(userMessage.getEntityId());

        return getSubmission(userMessage, messageLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final Long messageEntityId) throws MessageNotFoundException {
        LOG.info("Downloading message with entity id [{}]", messageEntityId);
        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);
        final UserMessageLog messageLog = userMessageLogService.findById(messageEntityId);
        publishDownloadEvent(userMessage.getMessageId());
        return getSubmission(userMessage, messageLog);
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

    protected Submission getSubmission(final UserMessage userMessage, final UserMessageLog messageLog) {
        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", userMessage.getMessageId());
            return messagingService.getSubmission(userMessage);
        }
        checkMessageAuthorization(userMessage);


        userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);

        return messagingService.getSubmission(userMessage);
    }

    @Override
    public Submission browseMessage(String messageId) {
        LOG.info("Browsing message with id [{}]", messageId);

        UserMessage userMessage = userMessageService.getByMessageId(messageId);

        checkMessageAuthorization(userMessage);
        return messagingService.getSubmission(userMessage);
    }


    @Override
    public Submission browseMessage(final Long messageEntityId) {
        LOG.info("Browsing message with entity id [{}]", messageEntityId);

        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

        checkMessageAuthorization(userMessage);
        return messagingService.getSubmission(userMessage);

    }

    protected void checkMessageAuthorization(UserMessage userMessage) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserWithUnsecureLoginAllowed();
        String displayUser = originalUser == null ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        // Authorization check
        userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
    }


    @Override
    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final Long messageEntityId) {
        try {
            userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageEntityId);
        } catch (eu.domibus.api.messaging.MessageNotFoundException e) {
            LOG.debug(e.getMessage());
            return eu.domibus.common.MessageStatus.NOT_FOUND;
        }
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageEntityId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }


    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        userMessageSecurityService.checkMessageAuthorizationWithUnsecureLoginAllowed(messageId);
        return errorLogService.getErrors(messageId);
    }

    @Transactional
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public String submitMessageFragment(UserMessage userMessage, MessageFragmentEntity messageFragmentEntity, PartInfo partInfo, String backendName) throws MessagingProcessingException {
        return messageSubmitter.submitMessageFragment(userMessage, messageFragmentEntity, partInfo, backendName);
    }

    private void saveMessageFragment(UserMessage userMessage, MessageFragmentEntity messageFragmentEntity, String backendName, String messageId, List<PartInfo> partInfos, LegConfiguration legConfiguration) throws EbMS3Exception {
        try {
            messagingService.storeMessagePayloads(userMessage, partInfos, MSHRole.SENDING, legConfiguration, backendName);
            messagingService.saveUserMessageAndPayloads(userMessage, partInfos);
            messageFragmentEntity.setUserMessage(userMessage);
            messageFragmentDao.create(messageFragmentEntity);
        } catch (CompressionException exc) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, messageId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0003)
                    .message(exc.getMessage())
                    .refToMessageId(messageId)
                    .cause(exc)
                    .mshRole(MSHRole.SENDING)
                    .build();
        }
    }

    private void prepareForPushOrPull(UserMessage userMessage, UserMessageLog userMessageLog, String pModeKey, MessageStatus messageStatus) {
        if (MessageStatus.READY_TO_PULL != messageStatus) {
            // Sends message to the proper queue if not a message to be pulled.
            userMessageService.scheduleSending(userMessage, userMessageLog);
        } else {
            LOG.debug("[submit]:Message:[{}] add lock", userMessage.getMessageId());
            pullMessageService.addPullMessageLock(userMessage, userMessage.getPartyInfo().getToParty(), pModeKey, userMessageLog);
        }
    }

    @Override
    @MDCKey(value = {DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID}, cleanOnStart = true)
    @Timer(clazz = DatabaseMessageHandler.class, value = "submit")
    @Counter(clazz = DatabaseMessageHandler.class, value = "submit")
    public String submit(final Submission submission, final String backendName) throws MessagingProcessingException {
        return messageSubmitter.submit(submission, backendName);
    }

    private void saveMessage(String backendName, String messageId, UserMessage userMessage, List<PartInfo> partInfos, MessageStatus messageStatus, String pModeKey, LegConfiguration legConfiguration) throws EbMS3Exception {
        try {
            messagingService.storeMessagePayloads(userMessage, partInfos, MSHRole.SENDING, legConfiguration, backendName);

            userMessageHandlerService.persistSentMessage(userMessage, messageStatus, partInfos, pModeKey, legConfiguration, backendName);
        } catch (CompressionException exc) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, messageId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0303)
                    .message(exc.getMessage())
                    .refToMessageId(messageId)
                    .cause(exc)
                    .mshRole(MSHRole.SENDING)
                    .build();
        } catch (InvalidPayloadSizeException e) {
            if (storageProvider.isPayloadsPersistenceFileSystemConfigured() && !e.isPayloadSavedAsync()) {
                //in case of Split&Join async payloads saving - PartInfo.getFileName will not point
                //to internal storage folder so we will not delete them
                partInfoService.clearFileSystemPayloads(partInfos);
            }
            LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, legConfiguration.getPayloadProfile().getMaxSize(), legConfiguration.getPayloadProfile().getName());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message(e.getMessage())
                    .refToMessageId(messageId)
                    .cause(e)
                    .mshRole(MSHRole.SENDING)
                    .build();
        }
    }

    /**
     * This method is a temporary method for the time of the old ws plugin lifecycle. It will find the processing type that is not submitted by the
     * old WS plugin.
     * see EDELIVERY-8610
     */
    private void checkSubmissionFromOldWSPlugin(final Submission submission, final UserMessage userMessage) throws EbMS3Exception {
        if (submission.getProcessingType() != null) {
            return;
        }
        LOG.debug("Submission processing type is empty,  checking processing type from PMODE");
        ProcessingType processingType;
        try {
            processingType = ProcessingType.PULL;
            setSubmissionProcessingType(submission, userMessage, processingType);
        } catch (EbMS3Exception e) {
            try {
                processingType = ProcessingType.PUSH;
                setSubmissionProcessingType(submission, userMessage, processingType);
            } catch (EbMS3Exception ex) {
                LOG.error("No processing type found from PMODE for the Submission", ex);
                throw ex;
            }
        }
    }

    private void setSubmissionProcessingType(Submission submission, UserMessage userMessage, ProcessingType processingType) throws EbMS3Exception {
        pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, processingType);
        submission.setProcessingType(processingType);
        LOG.debug("Processing type is:[{}]", processingType);
    }

    private void populateMessageIdIfNotPresent(UserMessage userMessage) {
        if (userMessage == null) {
            return;
        }
        if (isBlank(userMessage.getMessageId())) {
            userMessage.setMessageId(messageIdGenerator.generateMessageId());
            LOG.debug("Generated MessageId: [{}]", userMessage.getMessageId());
        }
    }

    @Override
    @Transactional
    public void initiatePull(String mpc) {
        messageExchangeService.initiatePullRequest(mpc);
    }

    protected Party messageValidations(UserMessage userMessage, List<PartInfo> partInfos, String pModeKey, String backendName) throws EbMS3Exception, MessagingProcessingException {
        try {
            Party from = pModeProvider.getSenderParty(pModeKey);
            Party to = pModeProvider.getReceiverParty(pModeKey);
            backendMessageValidator.validateParties(from, to);

            Party gatewayParty = pModeProvider.getGatewayParty();
            backendMessageValidator.validateInitiatorParty(gatewayParty, from);
            backendMessageValidator.validateResponderParty(gatewayParty, to);

            backendMessageValidator.validatePayloads(partInfos);

            return to;
        } catch (IllegalArgumentException runTimEx) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageId() + TO_STR + backendName + "]", runTimEx);
            throw MessagingExceptionFactory.transform(runTimEx, ErrorCode.EBMS_0003);
        }
    }

    private int getMaxAttempts(LegConfiguration legConfiguration) {
        return (legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount()) + 1; // counting retries after the first send attempt
    }

    private void fillMpc(UserMessage userMessage, LegConfiguration legConfiguration, Party to) {
        final Map<Party, Mpc> mpcMap = legConfiguration.getPartyMpcMap();
        String mpc = Ebms3Constants.DEFAULT_MPC;
        if (legConfiguration.getDefaultMpc() != null) {
            mpc = legConfiguration.getDefaultMpc().getQualifiedName();
        }
        if (mpcMap != null && mpcMap.containsKey(to)) {
            mpc = mpcMap.get(to).getQualifiedName();
        }
        MpcEntity mpcEntity = mpcDictionaryService.findOrCreateMpc(StringUtils.isBlank(mpc) ? Ebms3Constants.DEFAULT_MPC : mpc);
        userMessage.setMpc(mpcEntity);
    }

    protected Party createNewParty(String mpc) {
        if (mpc == null) {
            return null;
        }
        List<Identifier> identifiers = new ArrayList<>();
        Party party = new Party();
        Identifier identifier = new Identifier();
        identifiers.add(identifier);
        party.setIdentifiers(identifiers);
        identifier.setPartyId(messageExchangeService.extractInitiator(mpc));
        party.setIdentifiers(identifiers);
        party.setName(messageExchangeService.extractInitiator(mpc));

        return party;
    }

}
