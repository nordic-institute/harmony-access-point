package eu.domibus.core.plugin.handler;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.validation.UserMessageValidatorSpiService;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.exception.MessagingExceptionFactory;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.dictionary.MpcDictionaryService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.splitandjoin.SplitAndJoinConfigurationService;
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
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.ProcessingType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.domibus.logging.DomibusMessageCode.MANDATORY_MESSAGE_HEADER_METADATA_MISSING;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Service used for submitting messages (split from DatabaseMessageHandler)
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MessageSubmitterImpl implements MessageSubmitter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSubmitterImpl.class);

    private static final String USER_MESSAGE_IS_NULL = "UserMessage is null";
    private static final String ERROR_SUBMITTING_THE_MESSAGE_STR = "Error submitting the message [";
    private static final String TO_STR = "] to [";

    protected final AuthUtils authUtils;

    protected final UserMessageDefaultService userMessageService;

    protected final SplitAndJoinConfigurationService splitAndJoinConfigurationService;

    protected final PModeDefaultService pModeDefaultService;

    private final SubmissionAS4Transformer transformer;

    private final MessagingService messagingService;

    private final UserMessageLogDefaultService userMessageLogService;

    private final PayloadFileStorageProvider storageProvider;

    private final ErrorLogService errorLogService;

    private final PModeProvider pModeProvider;

    private final MessageIdGenerator messageIdGenerator;

    private final BackendMessageValidator backendMessageValidator;

    private final MessageExchangeService messageExchangeService;

    private final PullMessageService pullMessageService;

    protected final MessageFragmentDao messageFragmentDao;

    protected final MpcDictionaryService mpcDictionaryService;

    protected final UserMessageHandlerServiceImpl userMessageHandlerService;

    protected final UserMessageValidatorSpiService userMessageValidatorSpiService;

    protected final UserMessageSecurityService userMessageSecurityService;

    protected final PartInfoService partInfoService;

    public MessageSubmitterImpl(AuthUtils authUtils, UserMessageDefaultService userMessageService, SplitAndJoinConfigurationService splitAndJoinConfigurationService,
                                PModeDefaultService pModeDefaultService, SubmissionAS4Transformer transformer, MessagingService messagingService,
                                UserMessageLogDefaultService userMessageLogService, PayloadFileStorageProvider storageProvider, ErrorLogService errorLogService,
                                PModeProvider pModeProvider, MessageIdGenerator messageIdGenerator, BackendMessageValidator backendMessageValidator,
                                MessageExchangeService messageExchangeService, PullMessageService pullMessageService, MessageFragmentDao messageFragmentDao,
                                MpcDictionaryService mpcDictionaryService, UserMessageHandlerServiceImpl userMessageHandlerService,
                                UserMessageValidatorSpiService userMessageValidatorSpiService, UserMessageSecurityService userMessageSecurityService, PartInfoService partInfoService) {
        this.authUtils = authUtils;
        this.userMessageService = userMessageService;
        this.splitAndJoinConfigurationService = splitAndJoinConfigurationService;
        this.pModeDefaultService = pModeDefaultService;
        this.transformer = transformer;
        this.messagingService = messagingService;
        this.userMessageLogService = userMessageLogService;
        this.storageProvider = storageProvider;
        this.errorLogService = errorLogService;
        this.pModeProvider = pModeProvider;
        this.messageIdGenerator = messageIdGenerator;
        this.backendMessageValidator = backendMessageValidator;
        this.messageExchangeService = messageExchangeService;
        this.pullMessageService = pullMessageService;
        this.messageFragmentDao = messageFragmentDao;
        this.mpcDictionaryService = mpcDictionaryService;
        this.userMessageHandlerService = userMessageHandlerService;
        this.userMessageValidatorSpiService = userMessageValidatorSpiService;
        this.userMessageSecurityService = userMessageSecurityService;
        this.partInfoService = partInfoService;
    }

    @MDCKey(value = {DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID}, cleanOnStart = true)
    @Timer(clazz = MessageSubmitterImpl.class, value = "submit")
    @Counter(clazz = MessageSubmitterImpl.class, value = "submit")
    public String submit(final Submission submission, final String backendName) throws MessagingProcessingException {
        if (StringUtils.isNotEmpty(submission.getMessageId())) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, submission.getMessageId());
            LOG.debug("Add message ID to LOG MDC [{}]", submission.getMessageId());
        }
        LOG.debug("Preparing to submit message");
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserWithUnsecureLoginAllowed();
        String displayUser = (originalUser == null) ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        String messageId = null;
        try {
            backendMessageValidator.validateSubmissionSending(submission);

            UserMessage userMessage = transformer.transformFromSubmission(submission);

            if (userMessage == null) {
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage");
                throw new MessageNotFoundException(USER_MESSAGE_IS_NULL);
            }
            List<PartInfo> partInfos = transformer.generatePartInfoList(submission);

            userMessageValidatorSpiService.validate(userMessage, partInfos);

            populateMessageIdIfNotPresent(userMessage);
            messageId = userMessage.getMessageId();
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

            userMessageSecurityService.validateUserAccessWithUnsecureLoginAllowed(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);


            MessageExchangeConfiguration userMessageExchangeConfiguration;
            Party to = null;
            MessageStatus messageStatus = null;
            if (messageExchangeService.forcePullOnMpc(userMessage)) {
                submission.setProcessingType(ProcessingType.PULL);
                // UserMesages submitted with the optional mpc attribute are
                // meant for pulling (if the configuration property is enabled)
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true, submission.getProcessingType());
                to = createNewParty(userMessage.getMpcValue());
                messageStatus = MessageStatus.READY_TO_PULL;
            } else {
                //To be removed with the old ws plugin.
                checkSubmissionFromOldWSPlugin(submission, userMessage);
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, false, submission.getProcessingType());
                final MessageStatusEntity messageStatusEntity = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration, submission.getProcessingType());
                messageStatus = messageStatusEntity.getMessageStatus();
            }
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
            if (to == null) {
                //TODO validation should not return a business value
                to = messageValidations(userMessage, partInfos, pModeKey, backendName);
            }

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            if (userMessage.getMpc() == null) {
                fillMpc(userMessage, legConfiguration, to);
            }

            backendMessageValidator.validatePayloadProfile(userMessage, partInfos, pModeKey);
            backendMessageValidator.validatePropertyProfile(userMessage, pModeKey);

            final boolean splitAndJoin = splitAndJoinConfigurationService.mayUseSplitAndJoin(legConfiguration);
            userMessage.setSourceMessage(splitAndJoin);

            if (splitAndJoin && storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
                LOG.error("SplitAndJoin feature needs payload storage on the file system");
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                        .message("SplitAndJoin feature needs payload storage on the file system")
                        .refToMessageId(userMessage.getMessageId())
                        .mshRole(MSHRole.SENDING)
                        .build();
            }

            saveMessage(backendName, messageId, userMessage, partInfos, messageStatus, pModeKey, legConfiguration);
            LOG.info("Message with id: [{}] submitted", messageId);
            return messageId;
        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ebms3Ex);
            errorLogService.createErrorLog(ebms3Ex, MSHRole.SENDING, null);
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]" + p.getMessage(), p);
            errorLogService.createErrorLog(messageId, ErrorCode.EBMS_0004, p.getMessage(), MSHRole.SENDING, null);
            throw new PModeMismatchException(p.getMessage(), p);
        } catch (ConfigurationException ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ex);
            errorLogService.createErrorLog(messageId, ErrorCode.EBMS_0004, ex.getMessage(), MSHRole.SENDING, null);
            throw MessagingExceptionFactory.transform(ex, ErrorCode.EBMS_0004);
        }
    }

    @Transactional
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public String submitMessageFragment(UserMessage userMessage, MessageFragmentEntity messageFragmentEntity, PartInfo partInfo, String backendName) throws MessagingProcessingException {
        if (userMessage == null) {
            LOG.warn(USER_MESSAGE_IS_NULL);
            throw new MessageNotFoundException(USER_MESSAGE_IS_NULL);
        }

        String messageId = userMessage.getMessageId();

        if (StringUtils.isEmpty(messageId)) {
            throw new MessagingProcessingException("Message fragment id is empty");
        }
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        LOG.debug("Preparing to submit message fragment");

        try {
            // handle if the messageId is unique.
            backendMessageValidator.validateMessageIsUnique(messageId);

            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();

            List<PartInfo> partInfos = new ArrayList<>();
            partInfos.add(partInfo);

            Party to = messageValidations(userMessage, partInfos, pModeKey, backendName);
            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            fillMpc(userMessage, legConfiguration, to);

            saveMessageFragment(userMessage, messageFragmentEntity, backendName, messageId, partInfos, legConfiguration);
            MessageStatusEntity messageStatus = messageExchangeService.getMessageStatusForPush();
            final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageStatus.getMessageStatus().toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                    MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration),
                    backendName);
            prepareForPushOrPull(userMessage, userMessageLog, pModeKey, messageStatus.getMessageStatus());

            LOG.info("Message fragment submitted");
            return messageId;

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ebms3Ex);
            errorLogService.createErrorLog(ebms3Ex, MSHRole.SENDING, userMessage);
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]" + p.getMessage());
            errorLogService.createErrorLog(messageId, ErrorCode.EBMS_0010, p.getMessage(), MSHRole.SENDING, userMessage);
            throw new PModeMismatchException(p.getMessage(), p);
        }
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

    private void saveMessage(String backendName, String messageId, UserMessage userMessage, List<PartInfo> partInfos, MessageStatus messageStatus, String pModeKey, LegConfiguration legConfiguration) throws EbMS3Exception {
        try {
            messagingService.storeMessagePayloads(userMessage, partInfos, MSHRole.SENDING, legConfiguration, backendName);

            persistSentMessage(userMessage, messageStatus, partInfos, pModeKey, legConfiguration, backendName);
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


    @Timer(clazz = MessageSubmitterImpl.class, value = "persistSentMessage")
    @Counter(clazz = MessageSubmitterImpl.class, value = "persistSentMessage")
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


}
