package eu.domibus.core.plugin.handler;

import com.google.common.collect.Sets;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.exception.MessagingExceptionFactory;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.pull.PartyExtractor;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.transformer.SubmissionAS4Transformer;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.ebms3.common.model.UserMessage;
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
    private static final String MESSAGE_WITH_ID_STR = "Message with id [";
    private static final String WAS_NOT_FOUND_STR = "] was not found";
    private static final String ERROR_SUBMITTING_THE_MESSAGE_STR = "Error submitting the message [";
    private static final String TO_STR = "] to [";
    static final String USER_MESSAGE_IS_NULL = "UserMessage is null";

    private final ObjectFactory ebMS3Of = new ObjectFactory();


    @Autowired
    private SubmissionAS4Transformer transformer;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private SignalMessageLogDao signalMessageLogDao;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);

        Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        if (messaging == null) {
            throw new MessageNotFoundException(MESSAGE_WITH_ID_STR + messageId + WAS_NOT_FOUND_STR);
        }

        final UserMessageLog messageLog = userMessageLogDao.findByMessageId(messageId);
        UserMessage userMessage = messaging.getUserMessage();
        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", messageId);
            return transformer.transformFromMessaging(userMessage);
        }

        checkMessageAuthorization(userMessage, messageLog);

        boolean shouldDeleteDownloadedMessage = shouldDeleteDownloadedMessage(userMessage);
        if (shouldDeleteDownloadedMessage) {
            messagingDao.clearPayloadData(userMessage);

            // Sets the message log status to DELETED
            userMessageLogService.setMessageAsDeleted(userMessage, messageLog);
            // Sets the log status to deleted also for the signal messages (if present).
            userMessageLogService.setSignalMessageAsDeleted(messaging.getSignalMessage());
        } else {
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }
        return transformer.transformFromMessaging(userMessage);
    }

    protected boolean shouldDeleteDownloadedMessage(UserMessage userMessage) {
        // Deleting the message and signal message if the retention download is zero and the payload is not stored on the file system.
        return (userMessage != null && 0 == pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc()) && !userMessage.isPayloadOnFileSystem());
    }

    @Override
    public Submission browseMessage(String messageId) throws MessageNotFoundException {
        LOG.info("Browsing message with id [{}]", messageId);

        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            throw new MessageNotFoundException(MESSAGE_WITH_ID_STR + messageId + WAS_NOT_FOUND_STR);
        }

        UserMessageLog userMessageLog = userMessageLogDao.findByMessageId(messageId);

        checkMessageAuthorization(userMessage, userMessageLog);

        return transformer.transformFromMessaging(userMessage);
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
        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        List<String> recipients = new ArrayList<>();
        recipients.add(MessageConstants.ORIGINAL_SENDER);
        recipients.add(MessageConstants.FINAL_RECIPIENT);
        validateOriginalUser(userMessage, originalUser, recipients);
    }

    @Override
    public MessageStatus getStatus(final String messageId) {
        validateAccessToStatusAndErrors(messageId);
        return userMessageLogDao.getMessageStatus(messageId);
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        validateAccessToStatusAndErrors(messageId);
        return errorLogDao.getErrorsForMessage(messageId);
    }

    //TODO refactor this method in order to reuse existing code from the method submit
    @Transactional
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public String submitMessageFragment(UserMessage userMessage, String backendName) throws MessagingProcessingException {
        if (userMessage == null) {
            LOG.warn(USER_MESSAGE_IS_NULL);
            throw new MessageNotFoundException(USER_MESSAGE_IS_NULL);
        }

        // MessageInfo is always initialized in the get method
        MessageInfo messageInfo = userMessage.getMessageInfo();
        String messageId = messageInfo.getMessageId();

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

            Messaging message = ebMS3Of.createMessaging();
            message.setUserMessage(userMessage);

            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();


            Party to = messageValidations(userMessage, pModeKey, backendName);

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);

            fillMpc(userMessage, legConfiguration, to);

            try {
                messagingService.storeMessage(message, MSHRole.SENDING, legConfiguration, backendName);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, userMessage.getMessageInfo().getMessageId());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            MessageStatus messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
            final UserMessageLog userMessageLog = userMessageLogService.save(messageId, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                    MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(),
                    backendName, to.getEndpoint(), userMessage.getCollaborationInfo().getService().getValue(), userMessage.getCollaborationInfo().getAction(), null, true);
            prepareForPushOrPull(userMessageLog, pModeKey, to, messageStatus);


            uiReplicationSignalService.userMessageSubmitted(userMessage.getMessageInfo().getMessageId());

            LOG.info("Message fragment submitted");
            return userMessage.getMessageInfo().getMessageId();

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageInfo().getMessageId() + TO_STR + backendName + "]", ebms3Ex);
            errorLogDao.create(new ErrorLogEntry(ebms3Ex));
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageInfo().getMessageId() + TO_STR + backendName + "]" + p.getMessage());
            errorLogDao.create(new ErrorLogEntry(MSHRole.SENDING, userMessage.getMessageInfo().getMessageId(), ErrorCode.EBMS_0010, p.getMessage()));
            throw new PModeMismatchException(p.getMessage(), p);
        }
    }

    private void prepareForPushOrPull(UserMessageLog userMessageLog, String pModeKey, Party to, MessageStatus messageStatus) {
        if (MessageStatus.READY_TO_PULL != messageStatus) {
            // Sends message to the proper queue if not a message to be pulled.
            userMessageService.scheduleSending(userMessageLog);
        } else {
            LOG.debug("[submit]:Message:[{}] add lock", userMessageLog.getMessageId());
            pullMessageService.addPullMessageLock(new PartyExtractor(to), pModeKey, userMessageLog);
        }
    }


    @Override
    @Transactional
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public String submit(final Submission messageData, final String backendName) throws MessagingProcessingException {
        if (StringUtils.isNotEmpty(messageData.getMessageId())) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageData.getMessageId());
        }
        LOG.debug("Preparing to submit message");
        if (!authUtils.isUnsecureLoginAllowed()) {
            authUtils.hasUserOrAdminRole();
        }

        String originalUser = authUtils.getOriginalUserFromSecurityContext();
        String displayUser = originalUser == null ? "super user" : originalUser;
        LOG.debug("Authorized as [{}]", displayUser);

        UserMessage userMessage = transformer.transformFromSubmission(messageData);

        if (userMessage == null) {
            LOG.warn(USER_MESSAGE_IS_NULL);
            throw new MessageNotFoundException(USER_MESSAGE_IS_NULL);
        }

        validateOriginalUser(userMessage, originalUser, MessageConstants.ORIGINAL_SENDER);

        try {
            // MessageInfo is always initialized in the get method
            MessageInfo messageInfo = userMessage.getMessageInfo();
            String messageId = messageInfo.getMessageId();
            if (messageId == null) {
                messageId = messageIdGenerator.generateMessageId();
                messageInfo.setMessageId(messageId);
            } else {
                backendMessageValidator.validateMessageId(messageId);
                userMessage.getMessageInfo().setMessageId(messageId);
            }
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageInfo.getMessageId());

            String refToMessageId = messageInfo.getRefToMessageId();
            if (refToMessageId != null) {
                backendMessageValidator.validateRefToMessageId(refToMessageId);
            }
            // handle if the messageId is unique. This should only fail if the ID is set from the outside
            if (!MessageStatus.NOT_FOUND.equals(userMessageLogDao.getMessageStatus(messageId))) {
                throw new DuplicateMessageException(MESSAGE_WITH_ID_STR + messageId + "] already exists. Message identifiers must be unique");
            }

            Messaging message = ebMS3Of.createMessaging();
            message.setUserMessage(userMessage);

            MessageExchangeConfiguration userMessageExchangeConfiguration;

            Party to = null;
            MessageStatus messageStatus = null;
            if (messageExchangeService.forcePullOnMpc(userMessage)) {
                // UserMesages submited with the optional mpc attribute are
                // meant for pulling (if the configuration property is enabled)
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, true);
                to = createNewParty(userMessage.getMpc());
                messageStatus = MessageStatus.READY_TO_PULL;
            } else {
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            }
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();

            if (to == null) {
                to = messageValidations(userMessage, pModeKey, backendName);
            }

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            if (userMessage.getMpc() == null) {
                fillMpc(userMessage, legConfiguration, to);
            }

            payloadProfileValidator.validate(message, pModeKey);
            propertyProfileValidator.validate(message, pModeKey);

            final boolean splitAndJoin = splitAndJoinService.mayUseSplitAndJoin(legConfiguration);
            userMessage.setSplitAndJoin(splitAndJoin);

            if (splitAndJoin && storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
                LOG.error("SplitAndJoin feature needs payload storage on the file system");
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "SplitAndJoin feature needs payload storage on the file system", userMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            try {
                messagingService.storeMessage(message, MSHRole.SENDING, legConfiguration, backendName);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, userMessage.getMessageInfo().getMessageId());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }  catch (InvalidPayloadSizeException e) {
                if (storageProvider.isPayloadsPersistenceFileSystemConfigured()) {
                    messagingDao.clearFileSystemPayloads(userMessage);
                }
                LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, e.getMessage());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getMessage(), userMessage.getMessageInfo().getMessageId(), e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            if (messageStatus == null) {
                messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
            }
            final boolean sourceMessage = userMessage.isSourceMessage();
            final UserMessageLog userMessageLog = userMessageLogService.save(messageId, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                    MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(),
                    backendName, to.getEndpoint(), messageData.getService(), messageData.getAction(), sourceMessage, null);

            if (!sourceMessage) {
                prepareForPushOrPull(userMessageLog, pModeKey, to, messageStatus);
            }

            uiReplicationSignalService.userMessageSubmitted(userMessage.getMessageInfo().getMessageId());

            LOG.info("Message submitted");
            return userMessage.getMessageInfo().getMessageId();

        } catch (EbMS3Exception ebms3Ex) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageInfo().getMessageId() + TO_STR + backendName + "]", ebms3Ex);
            errorLogDao.create(new ErrorLogEntry(ebms3Ex));
            throw MessagingExceptionFactory.transform(ebms3Ex);
        } catch (PModeException p) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageInfo().getMessageId() + TO_STR + backendName + "]" + p.getMessage(), p);
            errorLogDao.create(new ErrorLogEntry(MSHRole.SENDING, userMessage.getMessageInfo().getMessageId(), ErrorCode.EBMS_0010, p.getMessage()));
            throw new PModeMismatchException(p.getMessage(), p);
        }
    }

    @Override
    @Transactional
    public void initiatePull(String mpc) {
        messageExchangeService.initiatePullRequest(mpc);
    }

    protected Party messageValidations(UserMessage userMessage, String pModeKey, String backendName) throws EbMS3Exception, MessagingProcessingException {
        try {
            Party from = pModeProvider.getSenderParty(pModeKey);
            Party to = pModeProvider.getReceiverParty(pModeKey);
            backendMessageValidator.validateParties(from, to);

            Party gatewayParty = pModeProvider.getGatewayParty();
            backendMessageValidator.validateInitiatorParty(gatewayParty, from);
            backendMessageValidator.validateResponderParty(gatewayParty, to);

            backendMessageValidator.validatePayloads(userMessage.getPayloadInfo());

            return to;
        } catch (IllegalArgumentException runTimEx) {
            LOG.error(ERROR_SUBMITTING_THE_MESSAGE_STR + userMessage.getMessageInfo().getMessageId() + TO_STR + backendName + "]", runTimEx);
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
        userMessage.setMpc(mpc);
    }

    protected Party createNewParty(String mpc) {
        if (mpc == null) {
            return null;
        }
        Party party = new Party();
        Identifier identifier = new Identifier();
        identifier.setPartyId(messageExchangeService.extractInitiator(mpc));
        party.setIdentifiers(Sets.newHashSet(identifier));
        party.setName(messageExchangeService.extractInitiator(mpc));

        return party;
    }

}
