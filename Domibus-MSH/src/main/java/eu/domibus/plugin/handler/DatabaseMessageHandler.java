package eu.domibus.plugin.handler;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Sets;
import eu.domibus.api.metrics.MetricsHelper;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.exception.MessagingExceptionFactory;
import eu.domibus.common.metrics.Counter;
import eu.domibus.common.metrics.Timer;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Mpc;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.BackendMessageValidator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.UserMessageServiceHelper;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.*;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.domibus.common.metrics.MetricNames.SUBMITTED_MESSAGES;

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
    private MetricRegistry metricRegistry;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Submission downloadMessage(final String messageId) throws MessageNotFoundException {
        com.codahale.metrics.Counter downloadMessageCounter = MetricsHelper.getMetricRegistry().counter(MetricRegistry.name(DatabaseMessageHandler.class,"downloadMessage.counter"));
        try {
            downloadMessageCounter.inc();
            LOG.info("Downloading message with id [{}]", messageId);

            com.codahale.metrics.Timer.Context findMessageTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "findMessageById.timer")).time();
            Messaging messaging = messagingDao.findMessageByMessageId(messageId);
            if (messaging == null) {
                throw new MessageNotFoundException(MESSAGE_WITH_ID_STR + messageId + WAS_NOT_FOUND_STR);
            }
            findMessageTimer.stop();

            com.codahale.metrics.Timer.Context findUserMessageLogTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "findUserMessageLogById.timer")).time();
            final UserMessageLog messageLog = userMessageLogDao.findByMessageId(messageId);
            findUserMessageLogTimer.stop();

            com.codahale.metrics.Timer.Context checkMessageAuthorizationTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "checkMessageAuthorization.timer")).time();
            UserMessage userMessage = messaging.getUserMessage();
            checkMessageAuthorization(userMessage, messageLog);
            checkMessageAuthorizationTimer.stop();

            com.codahale.metrics.Timer.Context shouldDeleteTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "shouldDelete.timer")).time();
            boolean shouldDeleteDownloadedMessage = shouldDeleteDownloadedMessage(userMessage);
            shouldDeleteTimer.stop();

            if (shouldDeleteDownloadedMessage) {
                com.codahale.metrics.Timer.Context clearPayloadTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "clearPayload.timer")).time();
                messagingDao.clearPayloadData(userMessage);
                clearPayloadTimer.stop();

                com.codahale.metrics.Timer.Context setAsDeletedTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "setAsDeleted.timer")).time();
                // Sets the message log status to DELETED
                userMessageLogService.setMessageAsDeleted(userMessage, messageLog);
                // Sets the log status to deleted also for the signal messages (if present).
                setAsDeletedTimer.stop();

                com.codahale.metrics.Timer.Context setSignalAsDeletedTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "setSignalAsDeleted.timer")).time();
                SignalMessage signalMessage = messaging.getSignalMessage();
                if (signalMessage != null) {
                    String signalMessageId = signalMessage.getMessageInfo().getMessageId();
                    userMessageLogService.setSignalMessageAsDeleted(signalMessageId);
                    LOG.debug("SignalMessage [{}] was set as DELETED.", signalMessageId);
                }
                setSignalAsDeletedTimer.stop();
            } else {
                com.codahale.metrics.Timer.Context setMessageAsDownloadedimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "setMessageAsDownloaded.timer")).time();
                userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
                setMessageAsDownloadedimer.stop();
            }
            com.codahale.metrics.Timer.Context transformFromMessagingTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "transformFromMessaging.timer")).time();
            Submission submission = transformer.transformFromMessaging(userMessage);
            transformFromMessagingTimer.stop();
            return submission;
        }finally {
            downloadMessageCounter.dec();
        }
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
    @Transactional(propagation = Propagation.SUPPORTS)
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
            messagingService.persistSubmittedMessageFragment(backendName, userMessage, messageId, message, userMessageExchangeConfiguration, to, pModeKey, legConfiguration);


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


    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Timer(value = SUBMITTED_MESSAGES)
    @Counter(SUBMITTED_MESSAGES)
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
                com.codahale.metrics.Timer.Context findTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "findUserMessageExchangeContext.timer")).time();
                userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
                findTimer.stop();
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


            com.codahale.metrics.Timer.Context storeMessageTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "storeMessage.timer")).time();
            try {
                messagingService.storeMessage(message, MSHRole.SENDING, legConfiguration, backendName);
            } catch (CompressionException exc) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, userMessage.getMessageInfo().getMessageId());
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }
            storeMessageTimer.stop();

            com.codahale.metrics.Timer.Context persistSubmittedMessageTimer = MetricsHelper.getMetricRegistry().timer(MetricRegistry.name(DatabaseMessageHandler.class, "persistSubmittedMessage")).time();
            messagingService.persistSubmittedMessage(messageData, backendName, userMessage, messageId, message, userMessageExchangeConfiguration, to, messageStatus, pModeKey, legConfiguration);
            persistSubmittedMessageTimer.stop();

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

    private Party messageValidations(UserMessage userMessage, String pModeKey, String backendName) throws EbMS3Exception, MessagingProcessingException {
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
