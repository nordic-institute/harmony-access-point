package eu.domibus.core.plugin.handler;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.exception.MessagingExceptionFactory;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.*;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.logging.DomibusMessageCode.MANDATORY_MESSAGE_HEADER_METADATA_MISSING;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This class is responsible of handling the plugins requests for all the operations exposed.
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
    private static final String MESSAGE_WITH_ID_STR = "Message with id [";
    private static final String WAS_NOT_FOUND_STR = "] was not found";
    private static final String ERROR_SUBMITTING_THE_MESSAGE_STR = "Error submitting the message [";
    private static final String TO_STR = "] to [";

    @Autowired
    protected AuthUtils authUtils;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected PModeDefaultService pModeDefaultService;

    @Autowired
    protected UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    protected MessagePropertyValidator messagePropertyValidator;

    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private SignalMessageDao signalMessageDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private PayloadFileStorageProvider storageProvider;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Autowired
    private BackendMessageValidator backendMessageValidator;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Autowired
    protected MessageFragmentDao messageFragmentDao;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected MessageDictionaryService messageDictionaryService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);

        final UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(MESSAGE_WITH_ID_STR + messageId + WAS_NOT_FOUND_STR);
        }

        final UserMessageLog messageLog = userMessageLogDao.findByMessageId(messageId);
        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", messageId);
            List<PartInfo> partInfoList = partInfoDao.findPartInfoByUserMessageEntityId(messageLog.getEntityId());
            return transformer.transformFromMessaging(userMessage, partInfoList);
        }

        checkMessageAuthorization(userMessage, messageLog);

        List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
        boolean shouldDeleteDownloadedMessage = shouldDeleteDownloadedMessage(userMessage, partInfos);
        if (shouldDeleteDownloadedMessage) {
            partInfoDao.clearPayloadData(userMessage.getEntityId());

            // Sets the message log status to DELETED
            userMessageLogService.setMessageAsDeleted(userMessage, messageLog);
            // Sets the log status to deleted also for the signal messages (if present).

            final SignalMessage signalMessage = signalMessageDao.read(userMessage.getEntityId());
            userMessageLogService.setSignalMessageAsDeleted(signalMessage);
        } else {
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }
        return transformer.transformFromMessaging(userMessage, partInfos);
    }

    protected boolean shouldDeleteDownloadedMessage(UserMessage userMessage, List<PartInfo> partInfos) {
        // Deleting the message and signal message if the retention download is zero and the payload is not stored on the file system.
        return (userMessage != null && 0 == pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpcValue()) && !isPayloadOnFileSystem(partInfos));
    }

    protected boolean isPayloadOnFileSystem(List<PartInfo> partInfos) {
        for (PartInfo partInfo : partInfos) {
            if (StringUtils.isNotEmpty(partInfo.getFileName()))
                return true;
        }
        return false;
    }

    @Override
    public Submission browseMessage(String messageId) throws MessageNotFoundException {
        LOG.info("Browsing message with id [{}]", messageId);

        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(MESSAGE_WITH_ID_STR + messageId + WAS_NOT_FOUND_STR);
        }

        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);
        checkMessageAuthorization(userMessage, userMessageLog);

        List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
        return transformer.transformFromMessaging(userMessage, partInfos);
    }

    protected void checkMessageAuthorization(UserMessage userMessage, UserMessageLog userMessageLog) throws MessageNotFoundException {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        String displayUser = originalUser == null ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        // Authorization check
        validateOriginalUser(userMessage, originalUser, MessageConstants.FINAL_RECIPIENT);
    }

    protected void validateOriginalUser(UserMessage userMessage, String authOriginalUser, List<String> recipients) {
        if (authOriginalUser != null) {
            LOG.debug("OriginalUser is [{}]", authOriginalUser);
            /* check the message belongs to the authenticated user */
            boolean found = false;
            for (String recipient : recipients) {
                String originalUser = userMessageServiceHelper.getOriginalUser(userMessage, recipient);
                if (originalUser != null && originalUser.equalsIgnoreCase(authOriginalUser)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOG.debug("User [{}] is trying to submit/access a message having as final recipients: [{}]", authOriginalUser, recipients);
                throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + authOriginalUser + "]");
            }
        }
    }

    protected void validateOriginalUser(UserMessage userMessage, String authOriginalUser, String recipient) {
        if (authOriginalUser != null) {
            LOG.debug("OriginalUser is [{}]", authOriginalUser);
            /* check the message belongs to the authenticated user */
            String originalUser = userMessageServiceHelper.getOriginalUser(userMessage, recipient);
            if (originalUser != null && !originalUser.equalsIgnoreCase(authOriginalUser)) {
                LOG.debug("User [{}] is trying to submit/access a message having as final recipient: [{}]", authOriginalUser, originalUser);
                throw new AccessDeniedException("You are not allowed to handle this message. You are authorized as [" + authOriginalUser + "]");
            }
        }
    }

    protected void validateAccessToStatusAndErrors(String messageId) {
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        // check if user can get the status/errors of that message (only admin or original users are authorized to do that)
        UserMessage userMessage = userMessageDao.findByMessageId(messageId);
        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        List<String> recipients = new ArrayList<>();
        recipients.add(MessageConstants.ORIGINAL_SENDER);
        recipients.add(MessageConstants.FINAL_RECIPIENT);
        validateOriginalUser(userMessage, originalUser, recipients);
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
        validateAccessToStatusAndErrors(messageId);
        MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        validateAccessToStatusAndErrors(messageId);
        List<ErrorLogEntry> errorsForMessage = errorLogDao.getErrorsForMessage(messageId);
        return errorsForMessage.stream().map(errorLogEntry -> convert(errorLogEntry)).collect(Collectors.toList());
    }

    protected ErrorResultImpl convert(ErrorLogEntry errorLogEntry) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setErrorCode(errorLogEntry.getErrorCode());
        result.setErrorDetail(errorLogEntry.getErrorDetail());
        result.setMessageInErrorId(errorLogEntry.getMessageInErrorId());
        result.setMshRole(eu.domibus.common.MSHRole.valueOf(errorLogEntry.getMshRole().name()));
        result.setNotified(errorLogEntry.getNotified());
        result.setTimestamp(errorLogEntry.getTimestamp());

        return result;
    }

    //TODO refactor this method in order to reuse existing code from the method submit
    @Transactional
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
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
            if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
                throw new DuplicateMessageException(MESSAGE_WITH_ID_STR + messageId + "] already exists. Message identifiers must be unique");
            }

            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();

            List<PartInfo> partInfos = new ArrayList<>();
            partInfos.add(partInfo);

            Party to = messageValidations(userMessage, partInfos, pModeKey, backendName);
            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            fillMpc(userMessage, legConfiguration, to);

            try {
                messagingService.storeMessagePayloads(userMessage, partInfos, MSHRole.SENDING, legConfiguration, backendName);
                messagingService.saveUserMessageAndPayloads(userMessage, partInfos);
                messageFragmentEntity.setUserMessage(userMessage);
                messageFragmentDao.create(messageFragmentEntity);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, messageId);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), messageId, exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            MessageStatusEntity messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
            final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageStatus.getMessageStatus().toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                    MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), userMessage.getMpcValue(),
                    backendName, to.getEndpoint(), userMessage.getService().getValue(), userMessage.getActionValue(), null, true);
            prepareForPushOrPull(userMessage, userMessageLog, pModeKey, messageStatus.getMessageStatus());


            uiReplicationSignalService.userMessageSubmitted(messageId);

            LOG.info("Message fragment submitted");
            return messageId;

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ebms3Ex);
            final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
            errorLogDao.create(new ErrorLogEntry(ebms3Ex, sendingRole));
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]" + p.getMessage());
            final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
            errorLogDao.create(new ErrorLogEntry(sendingRole, messageId, ErrorCode.EBMS_0010, p.getMessage()));
            throw new PModeMismatchException(p.getMessage(), p);
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
    @Transactional
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(clazz = DatabaseMessageHandler.class, value = "submit")
    @Counter(clazz = DatabaseMessageHandler.class, value = "submit")
    public String submit(final Submission submission, final String backendName) throws MessagingProcessingException {
        if (StringUtils.isNotEmpty(submission.getMessageId())) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, submission.getMessageId());
        }
        LOG.debug("Preparing to submit message");
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        String displayUser = (originalUser == null) ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        String messageId = null;
        try {
            backendMessageValidator.validateUserMessageForPmodeMatch(submission, MSHRole.SENDING);
            messagePropertyValidator.validate(submission, MSHRole.SENDING);

            UserMessage userMessage = transformer.transformFromSubmission(submission);

            if (userMessage == null) {
                LOG.businessError(MANDATORY_MESSAGE_HEADER_METADATA_MISSING, "UserMessage");
                throw new MessageNotFoundException(USER_MESSAGE_IS_NULL);
            }
            List<PartInfo> partInfos = transformer.generatePartInfoList(submission);

            populateMessageIdIfNotPresent(userMessage);
            messageId = userMessage.getMessageId();
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

            validateOriginalUser(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);

            MessageExchangeConfiguration userMessageExchangeConfiguration;
            Party to = null;
            MessageStatus messageStatus = null;
            if (messageExchangeService.forcePullOnMpc(userMessage)) {
                // UserMesages submited with the optional mpc attribute are
                // meant for pulling (if the configuration property is enabled)
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                to = createNewParty(userMessage.getMpcValue());
                messageStatus = MessageStatus.READY_TO_PULL;
            } else {
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
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

            payloadProfileValidator.validate(userMessage, partInfos, pModeKey);
            propertyProfileValidator.validate(userMessage, pModeKey);

            final boolean splitAndJoin = splitAndJoinService.mayUseSplitAndJoin(legConfiguration);
            userMessage.setSourceMessage(splitAndJoin);

            if (splitAndJoin && storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
                LOG.error("SplitAndJoin feature needs payload storage on the file system");
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "SplitAndJoin feature needs payload storage on the file system", userMessage.getMessageId(), null);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            try {
                messagingService.storeMessagePayloads(userMessage, partInfos, MSHRole.SENDING, legConfiguration, backendName);
                messagingService.saveUserMessageAndPayloads(userMessage, partInfos);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, messageId);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), messageId, exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            } catch (InvalidPayloadSizeException e) {
                if (storageProvider.isPayloadsPersistenceFileSystemConfigured() && !e.isPayloadSavedAsync()) {
                    //in case of Split&Join async payloads saving - PartInfo.getFileName will not point
                    //to internal storage folder so we will not delete them
                    partInfoDao.clearFileSystemPayloads(partInfos);
                }
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, legConfiguration.getPayloadProfile().getMaxSize(), legConfiguration.getPayloadProfile().getName());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getMessage(), messageId, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            if (messageStatus == null) {
                final MessageStatusEntity messageStatusEntity = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
                messageStatus = messageStatusEntity.getMessageStatus();
            }
            final boolean sourceMessage = userMessage.isSourceMessage();
            final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                    MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), userMessage.getMpcValue(),
                    backendName, to.getEndpoint(), submission.getService(), submission.getAction(), sourceMessage, null);

            if (!sourceMessage) {
                prepareForPushOrPull(userMessage, userMessageLog, pModeKey, messageStatus);
            }

            uiReplicationSignalService.userMessageSubmitted(messageId);
            LOG.info("Message with id: [{}] submitted", messageId);
            return messageId;

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ebms3Ex);
            final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
            errorLogDao.create(new ErrorLogEntry(ebms3Ex, sendingRole));
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]" + p.getMessage(), p);
            final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
            errorLogDao.create(new ErrorLogEntry(sendingRole, messageId, ErrorCode.EBMS_0010, p.getMessage()));
            throw new PModeMismatchException(p.getMessage(), p);
        } catch (ConfigurationException ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + messageId + TO_STR + backendName + "]", ex);
            final MSHRoleEntity sendingRole = mshRoleDao.findOrCreate(MSHRole.SENDING);
            errorLogDao.create(new ErrorLogEntry(sendingRole, messageId, ErrorCode.EBMS_0004, ex.getMessage()));
            throw MessagingExceptionFactory.transform(ex, ErrorCode.EBMS_0004);
        }
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
        MpcEntity mpcObject = messageDictionaryService.findOrCreateMpc(mpc);
        userMessage.setMpc(mpcObject);
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
