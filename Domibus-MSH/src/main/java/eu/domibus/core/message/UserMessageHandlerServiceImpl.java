package eu.domibus.core.message;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.*;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.receipt.AS4ReceiptService;
import eu.domibus.core.message.splitandjoin.*;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.payload.persistence.InvalidPayloadSizeException;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.xml.XMLUtilImpl;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import eu.domibus.ebms3.common.model.mf.MessageHeaderType;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.attachment.AttachmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

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
    private PModeProvider pModeProvider;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Autowired
    protected RoutingService routingService;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private PayloadProfileValidator payloadProfileValidator;

    @Autowired
    private PropertyProfileValidator propertyProfileValidator;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    protected UIReplicationSignalService uiReplicationSignalService;

    @Autowired
    protected RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected UserMessageService userMessageService;

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
    protected MessagingDao messagingDao;

    @Autowired
    protected MessagePropertyValidator messagePropertyValidator;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    public SOAPMessage handleNewUserMessage(final LegConfiguration legConfiguration, String pmodeKey, final SOAPMessage request, final Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, SOAPException {
        //check if the message is sent to the same Domibus instance
        final boolean selfSendingFlag = checkSelfSending(pmodeKey);
        final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);

        handleIncomingMessage(legConfiguration, pmodeKey, request, messaging, selfSendingFlag, messageExists, testMessage);

        return as4ReceiptService.generateReceipt(
                request,
                messaging,
                legConfiguration.getReliability().getReplyPattern(),
                legConfiguration.getReliability().isNonRepudiation(),
                messageExists,
                selfSendingFlag);
    }

    @Override
    public SOAPMessage handleNewSourceUserMessage(final LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, Messaging messaging, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        //check if the message is sent to the same Domibus instance
        final boolean selfSendingFlag = checkSelfSending(pmodeKey);
        final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(messaging);

        handleIncomingSourceMessage(legConfiguration, pmodeKey, request, messaging, selfSendingFlag, messageExists, testMessage);

        return null;
    }

    protected void handleIncomingSourceMessage(
            final LegConfiguration legConfiguration,
            String pmodeKey,
            final SOAPMessage request,
            final Messaging messaging,
            boolean selfSending,
            boolean messageExists,
            boolean testMessage) throws IOException, TransformerException, EbMS3Exception {
        soapUtil.logMessage(request);

        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        checkCharset(messaging);
        messagePropertyValidator.validate(messaging, MSHRole.RECEIVING);

        LOG.debug("Message duplication status:{}", messageExists);
        if (messageExists) {
            LOG.debug("No handling required: message already exists");
            return;
        }
        LOG.debug("Handling incoming SourceMessage [{}]", messageId);

        if (testMessage) {
            // ping messages are only stored and not notified to the plugins
            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, messaging, null, null);
            LOG.debug("Test source message saved: [{}]", messageInfoId);
        } else {
            final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(messaging.getUserMessage());
            String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, messaging, null, backendName);
            LOG.debug("Source message saved: [{}]", messageInfoId);

            try {
                backendNotificationService.notifyMessageReceived(matchingBackendFilter, messaging.getUserMessage());
            } catch (SubmissionValidationException e) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
            }
        }
    }

    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "handleIncomingMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "handleIncomingMessage")
    protected void handleIncomingMessage(
            final LegConfiguration legConfiguration,
            String pmodeKey,
            final SOAPMessage request,
            final Messaging messaging,
            boolean selfSending,
            boolean messageExists,
            boolean testMessage)
            throws IOException, TransformerException, EbMS3Exception, SOAPException {
        soapUtil.logMessage(request);

        if (selfSending) {
                /* we add a defined suffix in order to assure DB integrity - messageId uniqueness
                basically we are generating another messageId for Signal Message on receiver side
                */
            messaging.getUserMessage().getMessageInfo().setMessageId(messaging.getUserMessage().getMessageInfo().getMessageId() + SELF_SENDING_SUFFIX);
        }

        String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        checkCharset(messaging);
        messagePropertyValidator.validate(messaging, MSHRole.RECEIVING);

        LOG.debug("Message duplication status:{}", messageExists);
        if (!messageExists) {
            if (testMessage) {
                // ping messages are only stored and not notified to the plugins
                persistReceivedMessage(request, legConfiguration, pmodeKey, messaging, null, null);
            } else {
                final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(messaging.getUserMessage());
                String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

                MessageFragmentType messageFragmentType = messageUtil.getMessageFragment(request);
                persistReceivedMessage(request, legConfiguration, pmodeKey, messaging, messageFragmentType, backendName);

                try {
                    backendNotificationService.notifyMessageReceived(matchingBackendFilter, messaging.getUserMessage());
                } catch (SubmissionValidationException e) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
                }

                if (messageFragmentType != null) {
                    LOG.debug("Received UserMessage fragment");

                    splitAndJoinService.incrementReceivedFragments(messageFragmentType.getGroupId(), backendName);
                }
            }
        }
    }


    /**
     * It will check if the messages are sent to the same Domibus instance
     *
     * @param pmodeKey pmode key
     * @return boolean true if there is the same AP
     */
    @Override
    public Boolean checkSelfSending(String pmodeKey) {
        final Party receiver = pModeProvider.getReceiverParty(pmodeKey);
        final Party sender = pModeProvider.getSenderParty(pmodeKey);

        //check endpoint
        return StringUtils.trimToEmpty(receiver.getEndpoint()).equalsIgnoreCase(StringUtils.trimToEmpty(sender.getEndpoint()));
    }

    /**
     * Required for AS4_TA_12
     *
     * @param messaging the UserMessage received
     * @throws EbMS3Exception if an attachment with an invalid charset is received
     */
    protected void checkCharset(final Messaging messaging) throws EbMS3Exception {
        LOG.debug("Checking charset for attachments");
        final PayloadInfo payloadInfo = messaging.getUserMessage().getPayloadInfo();
        if (payloadInfo == null) {
            LOG.debug("No partInfo found");
            return;
        }

        for (final PartInfo partInfo : payloadInfo.getPartInfo()) {
            if (partInfo.getPartProperties() == null || partInfo.getPartProperties().getProperties() == null) {
                continue;
            }
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (Property.CHARSET.equalsIgnoreCase(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, property.getValue(), messaging.getUserMessage().getMessageInfo().getMessageId());
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, property.getValue() + " is not a valid Charset", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean checkTestMessage(final UserMessage message) {
        return checkTestMessage(message.getCollaborationInfo().getService().getValue(), message.getCollaborationInfo().getAction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean checkTestMessage(final String service, final String action) {
        LOG.debug("Checking if the user message represented by the service [{}] and the action [{}] is a test message", service, action);

        return Ebms3Constants.TEST_SERVICE.equalsIgnoreCase(service) && Ebms3Constants.TEST_ACTION.equalsIgnoreCase(action);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean checkTestMessage(final LegConfiguration legConfiguration) {
        if (legConfiguration == null) {
            LOG.debug("No leg configuration found");
            return false;
        }

        return checkTestMessage(legConfiguration.getService().getValue(), legConfiguration.getAction().getValue());
    }

    /**
     * This method persists incoming messages into the database (and handles decompression before)
     *
     * @param request          the message to persist
     * @param legConfiguration processing information for the message
     */
    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "persistReceivedMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "persistReceivedMessage")
    protected String persistReceivedMessage(
            final SOAPMessage request,
            final LegConfiguration legConfiguration,
            final String pmodeKey,
            final Messaging messaging,
            MessageFragmentType messageFragmentType,
            final String backendName)
            throws SOAPException, TransformerException, EbMS3Exception {
        LOG.info("Persisting received message");
        UserMessage userMessage = messaging.getUserMessage();

        if (messageFragmentType != null) {
            handleMessageFragment(messaging.getUserMessage(), messageFragmentType, legConfiguration);
        }

        handlePayloads(request, userMessage);

        boolean compressed = compressionService.handleDecompression(userMessage, legConfiguration);
        LOG.debug("Compression for message with id: {} applied: {}", userMessage.getMessageInfo().getMessageId(), compressed);
        return saveReceivedMessage(request, legConfiguration, pmodeKey, messaging, messageFragmentType, backendName, userMessage);
    }

    /**
     * Persists the incoming SourceMessage
     */
    protected String persistReceivedSourceMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, final Messaging messaging, MessageFragmentType messageFragmentType, final String backendName) throws EbMS3Exception {
        LOG.info("Persisting received SourceMessage");
        UserMessage userMessage = messaging.getUserMessage();
        userMessage.setSplitAndJoin(true);

        return saveReceivedMessage(request, legConfiguration, pmodeKey, messaging, messageFragmentType, backendName, userMessage);
    }

    protected String saveReceivedMessage(SOAPMessage request, LegConfiguration legConfiguration, String pmodeKey, Messaging messaging, MessageFragmentType messageFragmentType, String backendName, UserMessage userMessage) throws EbMS3Exception {
        //skip payload and property profile validations for message fragments
        if (messageFragmentType == null) {
            try {
                payloadProfileValidator.validate(messaging, pmodeKey);
                propertyProfileValidator.validate(messaging, pmodeKey);
            } catch (EbMS3Exception e) {
                e.setMshRole(MSHRole.RECEIVING);
                throw e;
            }
        }

        try {
            messagingService.storeMessage(messaging, MSHRole.RECEIVING, legConfiguration, backendName);
        } catch (CompressionException exc) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), userMessage.getMessageInfo().getMessageId(), exc);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        } catch (InvalidPayloadSizeException e) {
            if (storageProvider.isPayloadsPersistenceFileSystemConfigured()) {
                messagingDao.clearFileSystemPayloads(userMessage);
            }
            LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, legConfiguration.getPayloadProfile().getMaxSize(), legConfiguration.getPayloadProfile().getName());
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getMessage(), userMessage.getMessageInfo().getMessageId(), e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        Party to = pModeProvider.getReceiverParty(pmodeKey);
        Validate.notNull(to, "Responder party was not found");

        NotificationStatus notificationStatus = (legConfiguration.getErrorHandling() != null && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED;
        LOG.debug("NotificationStatus [{}]", notificationStatus);

        userMessageLogService.save(
                userMessage.getMessageInfo().getMessageId(),
                MessageStatus.RECEIVED.toString(),
                notificationStatus.toString(),
                MSHRole.RECEIVING.toString(),
                0,
                StringUtils.isEmpty(userMessage.getMpc()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpc(),
                backendName,
                to.getEndpoint(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getAction(),
                userMessage.isSourceMessage(),
                userMessage.isUserMessageFragment());

        uiReplicationSignalService.userMessageReceived(userMessage.getMessageInfo().getMessageId());

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PERSISTED);

        nonRepudiationService.saveRequest(request, userMessage);

        return userMessage.getMessageInfo().getMessageId();
    }


    protected void handleMessageFragment(UserMessage userMessage, MessageFragmentType messageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        userMessage.setSplitAndJoin(true);
        MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(messageFragmentType.getGroupId());

        if (messageGroupEntity == null) {
            LOG.debug("Creating messageGroupEntity");

            messageGroupEntity = new MessageGroupEntity();
            MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
            final MessageHeaderType messageHeader = messageFragmentType.getMessageHeader();
            messageHeaderEntity.setStart(messageHeader.getStart());
            messageHeaderEntity.setBoundary(messageHeader.getBoundary());
            messageGroupEntity.setMshRole(MSHRole.RECEIVING);
            messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);
            messageGroupEntity.setSoapAction(messageFragmentType.getAction());
            messageGroupEntity.setCompressionAlgorithm(messageFragmentType.getCompressionAlgorithm());
            messageGroupEntity.setMessageSize(messageFragmentType.getMessageSize());
            messageGroupEntity.setCompressedMessageSize(messageFragmentType.getCompressedMessageSize());
            messageGroupEntity.setGroupId(messageFragmentType.getGroupId());
            messageGroupEntity.setFragmentCount(messageFragmentType.getFragmentCount());
            messageGroupDao.create(messageGroupEntity);
        }

        validateUserMessageFragment(userMessage, messageGroupEntity, messageFragmentType, legConfiguration);

        MessageFragmentEntity messageFragmentEntity = new MessageFragmentEntity();
        messageFragmentEntity.setGroupId(messageFragmentType.getGroupId());
        messageFragmentEntity.setFragmentNumber(messageFragmentType.getFragmentNum());
        userMessage.setMessageFragment(messageFragmentEntity);

        addPartInfoFromFragment(userMessage, messageFragmentType);
    }

    protected void validateUserMessageFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, MessageFragmentType messageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        if (legConfiguration.getSplitting() == null) {
            LOG.error("No splitting configuration found on leg [{}]", legConfiguration.getName());
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "No splitting configuration found", userMessage.getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        if (storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature works only with payload storage configured on the file system");
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "SplitAndJoin feature needs payload storage on the file system", userMessage.getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        final String groupId = messageFragmentType.getGroupId();
        if (messageGroupEntity == null) {
            LOG.warn("Could not validate UserMessage fragment [[{}] for group [{}]: messageGroupEntity is null", userMessage.getMessageInfo().getMessageId(), groupId);
            return;
        }
        if (isTrue(messageGroupEntity.getExpired())) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0051, "More time than Pmode[].Splitting.JoinInterval has passed since the first fragment was received but not all other fragments are received", userMessage.getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
        if (isTrue(messageGroupEntity.getRejected())) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0040, "A fragment is received that relates to a group that was previously rejected", userMessage.getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
        final Long fragmentCount = messageGroupEntity.getFragmentCount();
        if (fragmentCount != null && messageFragmentType.getFragmentCount() != null && messageFragmentType.getFragmentCount() > fragmentCount) {
            LOG.error("An incoming message fragment has a a value greater than the known FragmentCount for group [{}]", groupId);
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0048, "An incoming message fragment has a a value greater than the known FragmentCount", userMessage.getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
    }

    protected void addPartInfoFromFragment(UserMessage userMessage, final MessageFragmentType messageFragment) {
        if (messageFragment == null) {
            LOG.debug("No message fragment found");
            return;
        }
        PartInfo partInfo = new PartInfo();
        partInfo.setHref(messageFragment.getHref());
        PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);

    }

    /**
     * If message with same messageId is already in the database return <code>true</code> else <code>false</code>
     *
     * @param messaging the message
     * @return result of duplicate handle
     */
    protected Boolean checkDuplicate(final Messaging messaging) {
        LOG.debug("Checking for duplicate messages");
        return userMessageLogDao.findByMessageId(messaging.getUserMessage().getMessageInfo().getMessageId(), MSHRole.RECEIVING) != null;
    }

    @Override
    public void handlePayloads(SOAPMessage request, UserMessage userMessage)
            throws EbMS3Exception, SOAPException, TransformerException {
        LOG.debug("Start handling payloads");

        if (userMessage.getPayloadInfo() == null) {
            LOG.debug("Finished handling payload - no payload");
            return;
        }

        boolean bodyloadFound = false;
        for (final PartInfo partInfo : userMessage.getPayloadInfo().getPartInfo()) {
            final String cid = partInfo.getHref();
            LOG.debug("looking for attachment with cid: {}", cid);
            boolean payloadFound = false;
            if (isBodyloadCid(cid)) {
                if (bodyloadFound) {
                    LOG.businessError(DomibusMessageCode.BUS_MULTIPLE_PART_INFO_REFERENCING_SOAP_BODY);
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "More than one Partinfo referencing the soap body found", userMessage.getMessageInfo().getMessageId(), null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
                LOG.info("Using soap body payload");
                bodyloadFound = true;
                payloadFound = true;
                partInfo.setInBody(true);
                Node bodyContent = getChildElement(request);
                LOG.debug("Soap BodyContent when handling payloads: [{}]", bodyContent);
                partInfo.setPayloadDatahandler(getDataHandler(bodyContent));
            }
            @SuppressWarnings("unchecked") final Iterator<AttachmentPart> attachmentIterator = request.getAttachments();
            AttachmentPart attachmentPart;
            while (attachmentIterator.hasNext() && !payloadFound) {

                attachmentPart = attachmentIterator.next();
                //remove square brackets from cid for further processing
                attachmentPart.setContentId(AttachmentUtil.cleanContentId(attachmentPart.getContentId()));
                LOG.debug("comparing with: " + attachmentPart.getContentId());
                if (attachmentPart.getContentId().equals(AttachmentUtil.cleanContentId(cid))) {
                    partInfo.setPayloadDatahandler(attachmentPart.getDataHandler());
                    partInfo.setInBody(false);
                    payloadFound = true;
                }
            }
            if (!payloadFound) {
                LOG.businessError(DomibusMessageCode.BUS_MESSAGE_ATTACHMENT_NOT_FOUND, cid);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0011, "No Attachment found for cid: " + cid + " of message: " + userMessage.getMessageInfo().getMessageId(), userMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
        LOG.debug("Finished handling payloads");
    }

    protected Node getChildElement(SOAPMessage request) throws SOAPException {
        if (request.getSOAPBody().hasChildNodes()) {
            return ((Node) request.getSOAPBody().getChildElements().next());
        }
        return null;
    }

    private boolean isBodyloadCid(String cid) {
        return cid == null || cid.isEmpty() || cid.startsWith(HASH_SIGN);
    }

    protected DataHandler getDataHandler(Node bodyContent) throws TransformerException {
        final Source source = new DOMSource(bodyContent);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Result result = new StreamResult(out);
        final Transformer transformer = XMLUtilImpl.getTransformerFactory().newTransformer();
        transformer.transform(source, result);
        return new DataHandler(new ByteArrayDataSource(out.toByteArray(), "text/xml"));
    }

    protected String getFinalRecipientName(UserMessage userMessage) {
        for (Property property : userMessage.getMessageProperties().getProperty()) {
            if (property.getName() != null && property.getName().equalsIgnoreCase(MessageConstants.FINAL_RECIPIENT)) {
                return property.getValue();
            }
        }
        return null;
    }


    @Override
    public ErrorResult createErrorResult(EbMS3Exception ebm3Exception) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setMshRole(MSHRole.RECEIVING);
        result.setMessageInErrorId(ebm3Exception.getRefToMessageId());
        try {
            result.setErrorCode(ebm3Exception.getErrorCodeObject());
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not find error code for [" + ebm3Exception.getErrorCode() + "]");
        }
        result.setErrorDetail(ebm3Exception.getErrorDetail());
        return result;
    }


}
