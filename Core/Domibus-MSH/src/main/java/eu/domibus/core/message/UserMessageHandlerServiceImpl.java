package eu.domibus.core.message;

import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageHeaderType;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.model.splitandjoin.MessageHeaderEntity;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.PartPropertyDictionaryService;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.receipt.AS4ReceiptService;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.plugin.validation.SubmissionValidatorService;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.plugin.exception.PluginMessageReceiveException;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

import static eu.domibus.core.message.UserMessageContextKeyProvider.BACKEND_FILTER;
import static eu.domibus.core.message.UserMessageContextKeyProvider.USER_MESSAGE;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * @author Thomas Dussart
 * @author Catalin Enache
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class UserMessageHandlerServiceImpl implements UserMessageHandlerService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageHandlerServiceImpl.class);
    public static final String HASH_SIGN = "#";

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected XMLUtil xmlUtil;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    protected PModeDefaultService pModeDefaultService;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    protected RoutingService routingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Autowired
    protected AS4ReceiptService as4ReceiptService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected PayloadFileStorageProvider storageProvider;

    @Autowired
    protected MessagePropertyValidator messagePropertyValidator;

    @Autowired
    protected PartInfoServiceImpl partInfoService;

    @Autowired
    protected PartPropertyDictionaryService partPropertyDictionaryService;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected MessageFragmentDao messageFragmentDao;

    @Autowired
    protected UserMessagePersistenceService userMessagePersistenceService;

    @Autowired
    protected SubmissionValidatorService submissionValidatorService;

    @Autowired
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Override
    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    public SOAPMessage handleNewUserMessage(final LegConfiguration legConfiguration, String pmodeKey, final SOAPMessage request, final UserMessage userMessage, Ebms3MessageFragmentType ebms3MessageFragmentType, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException {
        //check if the message is sent to the same Domibus instance
        final boolean selfSendingFlag = pModeProvider.checkSelfSending(pmodeKey);

        final SOAPMessage responseMessage = as4ReceiptService.generateReceipt(
                request,
                userMessage,
                legConfiguration.getReliability().getReplyPattern(),
                legConfiguration.getReliability().isNonRepudiation(),
                false,
                selfSendingFlag);

        SignalMessageResult signalMessageResult = null;
        try {
            signalMessageResult = as4ReceiptService.generateResponse(responseMessage, selfSendingFlag);
        } catch (final SOAPException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIPT_FAILURE);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0201)
                    .message("Could not generate Receipt. Check security header and non-repudiation settings")
                    .refToMessageId(userMessage.getMessageId())
                    .cause(e)
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        try {
            handleIncomingMessage(legConfiguration, pmodeKey, request, userMessage, ebms3MessageFragmentType, partInfoList, false, testMessage, signalMessageResult);
            return responseMessage;
        } catch (DataIntegrityViolationException e) {
            LOG.warn("Message is a duplicate", e);
        }

        userMessageContextKeyProvider.setKeyOnTheCurrentMessage(UserMessage.USER_MESSAGE_DUPLICATE_KEY, "true");
        final boolean duplicateDetectionActive = legConfiguration.getReceptionAwareness().getDuplicateDetection();

        String errorMessage = "Duplicate message";
        if (duplicateDetectionActive) {
            return as4ReceiptService.generateReceipt(
                    request,
                    userMessage,
                    legConfiguration.getReliability().getReplyPattern(),
                    legConfiguration.getReliability().isNonRepudiation(),
                    true,
                    selfSendingFlag);

        }
        throw EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                .message(errorMessage)
                .refToMessageId(userMessage.getMessageId())
                .mshRole(MSHRole.RECEIVING)
                .build();
    }

    @Transactional
    @Override
    public SOAPMessage handleNewSourceUserMessage(final LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, UserMessage userMessage, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException {
        //check if the message is sent to the same Domibus instance
        final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(userMessage);

        handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, partInfoList, messageExists, testMessage);

        return null;
    }

    protected void handleIncomingSourceMessage(
            final LegConfiguration legConfiguration,
            String pmodeKey,
            final SOAPMessage request,
            final UserMessage userMessage,
            List<PartInfo> partInfoList,
            boolean messageExists,
            boolean testMessage) throws IOException, TransformerException, EbMS3Exception {
        soapUtil.logMessage(request);

        String messageId = userMessage.getMessageId();
        partInfoService.checkPartInfoCharset(userMessage, partInfoList);
        messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);

        LOG.debug("Message duplication status:{}", messageExists);
        if (messageExists) {
            LOG.debug("No handling required: message already exists");
            return;
        }
        LOG.debug("Handling incoming SourceMessage [{}]", messageId);

        if (testMessage) {
            // ping messages are only stored and not notified to the plugins
            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, null, null, userMessage, partInfoList, null);
            LOG.debug("Test source message saved: [{}]", messageInfoId);
        } else {
            final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
            String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

            try {
                submissionValidatorService.validateSubmission(userMessage, partInfoList, backendName);
            } catch (SubmissionValidationException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                        .message(e.getMessage())
                        .refToMessageId(messageId)
                        .cause(e)
                        .build();
            }
            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, partInfoList, null);
            LOG.debug("Source message saved: [{}]", messageInfoId);

            try {
                backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
            } catch (PluginMessageReceiveException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PLUGIN_RECEIVE_FAILED, backendName);
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(e.getEbMS3ErrorCode())
                        .message(e.getMessage())
                        .refToMessageId(messageId)
                        .cause(e)
                        .build();
            }
        }
    }

    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "handleIncomingMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "handleIncomingMessage")
    protected void handleIncomingMessage(
            final LegConfiguration legConfiguration,
            String pmodeKey,
            final SOAPMessage request,
            final UserMessage userMessage,
            Ebms3MessageFragmentType ebms3MessageFragmentType,
            List<PartInfo> partInfoList,
            boolean messageExists,
            boolean testMessage,
            SignalMessageResult signalMessageResult)
            throws IOException, TransformerException, EbMS3Exception {
        soapUtil.logMessage(request);

        String messageId = userMessage.getMessageId();
        partInfoService.checkPartInfoCharset(userMessage, partInfoList);
        messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);

        if (messageExists) {
            LOG.info("Message [{}] already exists", userMessage.getMessageId());
            return;
        }


        final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
        String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

        // we add this objects for use in out Interceptor to notify when reply is sent; these values must be set before throwing any exception so that they can be available in the interceptor
        userMessageContextKeyProvider.setObjectOnTheCurrentMessage(BACKEND_FILTER, matchingBackendFilter);
        userMessageContextKeyProvider.setObjectOnTheCurrentMessage(USER_MESSAGE, userMessage);

        try {
            submissionValidatorService.validateSubmission(userMessage, partInfoList, backendName);
        } catch (SubmissionValidationException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .message(e.getMessage())
                    .refToMessageId(messageId)
                    .cause(e)
                    .build();
        }

        persistReceivedMessage(request, legConfiguration, pmodeKey, userMessage, partInfoList, ebms3MessageFragmentType, backendName, signalMessageResult);

        try {
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
        } catch (PluginMessageReceiveException e) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PLUGIN_RECEIVE_FAILED, backendName);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(e.getEbMS3ErrorCode())
                    .message(e.getMessage())
                    .refToMessageId(messageId)
                    .cause(e)
                    .build();
        }

        if (ebms3MessageFragmentType != null) {
            LOG.debug("Received UserMessage fragment");

            splitAndJoinService.incrementReceivedFragments(ebms3MessageFragmentType.getGroupId(), backendName);
        }
    }

    /**
     * This method persists incoming messages into the database (and handles decompression before)
     *
     * @param request          the message to persist
     * @param legConfiguration processing information for the message
     */
    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "persistReceivedMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "persistReceivedMessage")
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    protected String persistReceivedMessage(
            final SOAPMessage request,
            final LegConfiguration legConfiguration,
            final String pmodeKey,
            final UserMessage userMessage,
            List<PartInfo> partInfoList,
            Ebms3MessageFragmentType ebms3MessageFragmentType,
            final String backendName,
            SignalMessageResult signalMessageResult)
            throws EbMS3Exception {

        //add messageId to MDC map
        if (StringUtils.isNotBlank(userMessage.getMessageId())) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, userMessage.getMessageId());
        }
        LOG.info("Persisting received message");

        compressionService.handleDecompression(userMessage, partInfoList, legConfiguration);

        final String messageId = saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, partInfoList, signalMessageResult);

        if (ebms3MessageFragmentType != null) {
            handleMessageFragment(userMessage, ebms3MessageFragmentType, legConfiguration);
        }
        return messageId;
    }

    /**
     * Persists the incoming SourceMessage
     */
    protected String persistReceivedSourceMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, Ebms3MessageFragmentType ebms3MessageFragmentType, final String backendName, UserMessage userMessage, List<PartInfo> partInfoList, SignalMessageResult signalMessageResult) throws EbMS3Exception {
        LOG.info("Persisting received SourceMessage");
        userMessage.setSourceMessage(true);

        return saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, partInfoList, signalMessageResult);
    }

    protected String saveReceivedMessage(SOAPMessage request, LegConfiguration legConfiguration, String pmodeKey, Ebms3MessageFragmentType ebms3MessageFragmentType, String backendName, UserMessage userMessage, List<PartInfo> partInfoList, SignalMessageResult signalMessageResult) throws EbMS3Exception {
        //skip payload and property profile validations for message fragments
        if (ebms3MessageFragmentType == null) {
            try {
                payloadProfileValidator.validate(userMessage, partInfoList, pmodeKey);
                propertyProfileValidator.validate(userMessage, pmodeKey);
            } catch (EbMS3Exception e) {
                e.setMshRole(MSHRole.RECEIVING);
                throw e;
            }
        }

        try {
            messagingService.storeMessagePayloads(userMessage, partInfoList, MSHRole.RECEIVING, legConfiguration, backendName);
        } catch (CompressionException exc) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0303)
                    .message("Could not persist message" + exc.getMessage())
                    .refToMessageId(userMessage.getMessageId())
                    .cause(exc)
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        } catch (InvalidPayloadSizeException e) {
            if (storageProvider.isPayloadsPersistenceFileSystemConfigured()) {
                partInfoService.clearFileSystemPayloads(partInfoList);
            }
            LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, legConfiguration.getPayloadProfile().getMaxSize(), legConfiguration.getPayloadProfile().getName());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message(e.getMessage())
                    .refToMessageId(userMessage.getMessageId())
                    .cause(e)
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        Party to = pModeProvider.getReceiverParty(pmodeKey);
        Validate.notNull(to, "Responder party was not found");

        NotificationStatus notificationStatus = (legConfiguration.getErrorHandling() != null && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED;
        LOG.debug("NotificationStatus [{}]", notificationStatus);

        UserMessageRaw userMessageRaw = null;
        try {
            userMessageRaw = nonRepudiationService.createUserMessageRaw(request);
        } catch (TransformerException e) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .message(e.getMessage())
                    .refToMessageId(userMessage.getMessageId())
                    .cause(e)
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        userMessagePersistenceService.saveIncomingMessage(userMessage, partInfoList, notificationStatus, backendName, userMessageRaw, signalMessageResult);

        return userMessage.getMessageId();
    }

    protected void handleMessageFragment(UserMessage userMessage, Ebms3MessageFragmentType ebms3MessageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(ebms3MessageFragmentType.getGroupId());

        if (messageGroupEntity == null) {
            LOG.debug("Creating messageGroupEntity");

            messageGroupEntity = new MessageGroupEntity();
            MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
            final Ebms3MessageHeaderType messageHeader = ebms3MessageFragmentType.getMessageHeader();
            messageHeaderEntity.setStart(messageHeader.getStart());
            messageHeaderEntity.setBoundary(messageHeader.getBoundary());

            MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.RECEIVING);
            messageGroupEntity.setMshRole(role);
            messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);
            messageGroupEntity.setSoapAction(ebms3MessageFragmentType.getAction());
            messageGroupEntity.setCompressionAlgorithm(ebms3MessageFragmentType.getCompressionAlgorithm());
            messageGroupEntity.setMessageSize(ebms3MessageFragmentType.getMessageSize());
            messageGroupEntity.setCompressedMessageSize(ebms3MessageFragmentType.getCompressedMessageSize());
            messageGroupEntity.setGroupId(ebms3MessageFragmentType.getGroupId());
            messageGroupEntity.setFragmentCount(ebms3MessageFragmentType.getFragmentCount());
            messageGroupDao.create(messageGroupEntity);
        }

        validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);

        MessageFragmentEntity messageFragmentEntity = new MessageFragmentEntity();
        messageFragmentEntity.setUserMessage(userMessage);
        messageFragmentEntity.setGroup(messageGroupEntity);
        messageFragmentEntity.setFragmentNumber(ebms3MessageFragmentType.getFragmentNum());
        messageFragmentDao.create(messageFragmentEntity);


    }

    protected void validateUserMessageFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, Ebms3MessageFragmentType ebms3MessageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        if (legConfiguration.getSplitting() == null) {
            LOG.error("No splitting configuration found on leg [{}]", legConfiguration.getName());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                    .message("No splitting configuration found")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        if (storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature works only with payload storage configured on the file system");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                    .message("SplitAndJoin feature needs payload storage on the file system")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        final String groupId = ebms3MessageFragmentType.getGroupId();
        if (messageGroupEntity == null) {
            LOG.warn("Could not validate UserMessage fragment [[{}] for group [{}]: messageGroupEntity is null", userMessage.getMessageId(), groupId);
            return;
        }
        if (isTrue(messageGroupEntity.getExpired())) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0051)
                    .message("More time than Pmode[].Splitting.JoinInterval has passed since the first fragment was received but not all other fragments are received")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
        if (isTrue(messageGroupEntity.getRejected())) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0040)
                    .message("A fragment is received that relates to a group that was previously rejected")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
        final Long fragmentCount = messageGroupEntity.getFragmentCount();
        if (fragmentCount != null && ebms3MessageFragmentType.getFragmentCount() != null && ebms3MessageFragmentType.getFragmentCount() > fragmentCount) {
            LOG.error("An incoming message fragment has a a value greater than the known FragmentCount for group [{}]", groupId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0048)
                    .message("An incoming message fragment has a a value greater than the known FragmentCount")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
    }

    /**
     * If message with same messageId is already in the database return <code>true</code> else <code>false</code>
     *
     * @param userMessage the message
     * @return result of duplicate handle
     */
    protected Boolean checkDuplicate(final UserMessage userMessage) {
        LOG.debug("Checking for duplicate messages");
        return userMessageLogDao.findByMessageId(userMessage.getMessageId(), MSHRole.RECEIVING) != null;
    }
}
