package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.ebms3.model.*;
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
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
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
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.MessagePropertyValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.*;

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
    protected MessagePropertyValidator messagePropertyValidator;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Autowired
    protected MessagePropertyDao messagePropertyDao;

    @Autowired
    protected PartPropertyDao partPropertyDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Timer(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    @Counter(clazz = UserMessageHandlerServiceImpl.class, value = "handleNewUserMessage")
    public SOAPMessage handleNewUserMessage(final LegConfiguration legConfiguration, String pmodeKey, final SOAPMessage request, final UserMessage userMessage, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, SOAPException {
        //check if the message is sent to the same Domibus instance
        final boolean selfSendingFlag = checkSelfSending(pmodeKey);
        final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(userMessage);

        handleIncomingMessage(legConfiguration, pmodeKey, request, userMessage, partInfoList, selfSendingFlag, messageExists, testMessage);

        return as4ReceiptService.generateReceipt(
                request,
                userMessage,
                legConfiguration.getReliability().getReplyPattern(),
                legConfiguration.getReliability().isNonRepudiation(),
                messageExists,
                selfSendingFlag);
    }

    @Override
    public SOAPMessage handleNewSourceUserMessage(final LegConfiguration legConfiguration, String pmodeKey, SOAPMessage request, UserMessage userMessage, List<PartInfo> partInfoList, boolean testMessage) throws EbMS3Exception, TransformerException, IOException, JAXBException, SOAPException {
        //check if the message is sent to the same Domibus instance
        final boolean selfSendingFlag = checkSelfSending(pmodeKey);
        final boolean messageExists = legConfiguration.getReceptionAwareness().getDuplicateDetection() && this.checkDuplicate(userMessage);

        handleIncomingSourceMessage(legConfiguration, pmodeKey, request, userMessage, partInfoList, selfSendingFlag, messageExists, testMessage);

        return null;
    }

    protected void handleIncomingSourceMessage(
            final LegConfiguration legConfiguration,
            String pmodeKey,
            final SOAPMessage request,
            final UserMessage userMessage,
            List<PartInfo> partInfoList,
            boolean selfSending,
            boolean messageExists,
            boolean testMessage) throws IOException, TransformerException, EbMS3Exception {
        soapUtil.logMessage(request);

        String messageId = userMessage.getMessageId();
        checkPartInfoCharset(userMessage, partInfoList);
        messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);

        LOG.debug("Message duplication status:{}", messageExists);
        if (messageExists) {
            LOG.debug("No handling required: message already exists");
            return;
        }
        LOG.debug("Handling incoming SourceMessage [{}]", messageId);

        if (testMessage) {
            // ping messages are only stored and not notified to the plugins
            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, null, null, userMessage, partInfoList);
            LOG.debug("Test source message saved: [{}]", messageInfoId);
        } else {
            final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
            String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

            String messageInfoId = persistReceivedSourceMessage(request, legConfiguration, pmodeKey, null, backendName, userMessage, partInfoList);
            LOG.debug("Source message saved: [{}]", messageInfoId);

            try {
                backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
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
            final UserMessage userMessage,
            List<PartInfo> partInfoList,
            boolean selfSending,
            boolean messageExists,
            boolean testMessage)
            throws IOException, TransformerException, EbMS3Exception, SOAPException {
        soapUtil.logMessage(request);

        if (selfSending) {
                /* we add a defined suffix in order to assure DB integrity - messageId uniqueness
                basically we are generating another messageId for Signal Message on receiver side
                */
            userMessage.setMessageId(userMessage.getMessageId() + SELF_SENDING_SUFFIX);
        }

        String messageId = userMessage.getMessageId();
        checkPartInfoCharset(userMessage, partInfoList);
        messagePropertyValidator.validate(userMessage, MSHRole.RECEIVING);

        LOG.debug("Message duplication status:{}", messageExists);
        if (!messageExists) {
            if (testMessage) {
                // ping messages are only stored and not notified to the plugins
                persistReceivedMessage(request, legConfiguration, pmodeKey, userMessage, null, null, null);
            } else {
                final BackendFilter matchingBackendFilter = routingService.getMatchingBackendFilter(userMessage);
                String backendName = (matchingBackendFilter != null ? matchingBackendFilter.getBackendName() : null);

                Ebms3MessageFragmentType ebms3MessageFragmentType = messageUtil.getMessageFragment(request);
                persistReceivedMessage(request, legConfiguration, pmodeKey, userMessage, partInfoList, ebms3MessageFragmentType, backendName);

                try {
                    backendNotificationService.notifyMessageReceived(matchingBackendFilter, userMessage);
                } catch (SubmissionValidationException e) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
                    throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
                }

                if (ebms3MessageFragmentType != null) {
                    LOG.debug("Received UserMessage fragment");

                    splitAndJoinService.incrementReceivedFragments(ebms3MessageFragmentType.getGroupId(), backendName);
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
     * @param userMessage the UserMessage received
     * @throws EbMS3Exception if an attachment with an invalid charset is received
     */
    protected void checkPartInfoCharset(final UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
        LOG.debug("Checking charset for attachments");
        if (partInfoList == null) {
            LOG.debug("No partInfo found");
            return;
        }

        for (final PartInfo partInfo : partInfoList) {
            if (partInfo.getPartProperties() == null) {
                continue;
            }
            for (final Property property : partInfo.getPartProperties()) {
                if (Property.CHARSET.equalsIgnoreCase(property.getName()) && !Property.CHARSET_PATTERN.matcher(property.getValue()).matches()) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_CHARSET_INVALID, property.getValue(), userMessage.getMessageId());
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, property.getValue() + " is not a valid Charset", userMessage.getMessageId(), null);
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
        return checkTestMessage(message.getService().getValue(), message.getActionValue());
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
            final UserMessage userMessage,
            List<PartInfo> partInfoList,
            Ebms3MessageFragmentType ebms3MessageFragmentType,
            final String backendName)
            throws SOAPException, TransformerException, EbMS3Exception {
        LOG.info("Persisting received message");

        if (ebms3MessageFragmentType != null) {
            handleMessageFragment(userMessage, ebms3MessageFragmentType, legConfiguration);
        }

        boolean compressed = compressionService.handleDecompression(userMessage, partInfoList, legConfiguration);
        LOG.debug("Compression for message with id: {} applied: {}", userMessage.getMessageId(), compressed);
        return saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, partInfoList);
    }

    /**
     * Persists the incoming SourceMessage
     */
    protected String persistReceivedSourceMessage(final SOAPMessage request, final LegConfiguration legConfiguration, final String pmodeKey, Ebms3MessageFragmentType ebms3MessageFragmentType, final String backendName, UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
        LOG.info("Persisting received SourceMessage");
        userMessage.setSourceMessage(true);

        return saveReceivedMessage(request, legConfiguration, pmodeKey, ebms3MessageFragmentType, backendName, userMessage, partInfoList);
    }

    protected String saveReceivedMessage(SOAPMessage request, LegConfiguration legConfiguration, String pmodeKey, Ebms3MessageFragmentType ebms3MessageFragmentType, String backendName, UserMessage userMessage, List<PartInfo> partInfoList) throws EbMS3Exception {
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
            messagingService.storeMessage(userMessage, partInfoList, MSHRole.RECEIVING, legConfiguration, backendName);
        } catch (CompressionException exc) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "Could not persist message" + exc.getMessage(), userMessage.getMessageId(), exc);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        } catch (InvalidPayloadSizeException e) {
            if (storageProvider.isPayloadsPersistenceFileSystemConfigured()) {
                partInfoDao.clearFileSystemPayloads(partInfoList);
            }
            LOG.businessError(DomibusMessageCode.BUS_PAYLOAD_INVALID_SIZE, legConfiguration.getPayloadProfile().getMaxSize(), legConfiguration.getPayloadProfile().getName());
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getMessage(), userMessage.getMessageId(), e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        Party to = pModeProvider.getReceiverParty(pmodeKey);
        Validate.notNull(to, "Responder party was not found");

        NotificationStatus notificationStatus = (legConfiguration.getErrorHandling() != null && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) ? NotificationStatus.REQUIRED : NotificationStatus.NOT_REQUIRED;
        LOG.debug("NotificationStatus [{}]", notificationStatus);

        userMessageLogService.save(
                userMessage,
                MessageStatus.RECEIVED.toString(),
                notificationStatus.toString(),
                MSHRole.RECEIVING.toString(),
                0,
                StringUtils.isEmpty(userMessage.getMpcValue()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpcValue(),
                backendName,
                to.getEndpoint(),
                userMessage.getService().getValue(),
                userMessage.getActionValue(),
                userMessage.isSourceMessage(),
                userMessage.isMessageFragment());

        uiReplicationSignalService.userMessageReceived(userMessage.getMessageId());

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PERSISTED);

        nonRepudiationService.saveRequest(request, userMessage);

        return userMessage.getMessageId();
    }


    protected void handleMessageFragment(UserMessage userMessage, Ebms3MessageFragmentType ebms3MessageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        userMessage.setMessageFragment(true);
        MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(ebms3MessageFragmentType.getGroupId());

        if (messageGroupEntity == null) {
            LOG.debug("Creating messageGroupEntity");

            messageGroupEntity = new MessageGroupEntity();
            MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
            final Ebms3MessageHeaderType messageHeader = ebms3MessageFragmentType.getMessageHeader();
            messageHeaderEntity.setStart(messageHeader.getStart());
            messageHeaderEntity.setBoundary(messageHeader.getBoundary());

            MSHRoleEntity role = mshRoleDao.findByRole(MSHRole.RECEIVING);
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

        addPartInfoFromFragment(userMessage, ebms3MessageFragmentType);
    }

    protected void validateUserMessageFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, Ebms3MessageFragmentType ebms3MessageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        if (legConfiguration.getSplitting() == null) {
            LOG.error("No splitting configuration found on leg [{}]", legConfiguration.getName());
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "No splitting configuration found", userMessage.getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        if (storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature works only with payload storage configured on the file system");
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0002, "SplitAndJoin feature needs payload storage on the file system", userMessage.getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }

        final String groupId = ebms3MessageFragmentType.getGroupId();
        if (messageGroupEntity == null) {
            LOG.warn("Could not validate UserMessage fragment [[{}] for group [{}]: messageGroupEntity is null", userMessage.getMessageId(), groupId);
            return;
        }
        if (isTrue(messageGroupEntity.getExpired())) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0051, "More time than Pmode[].Splitting.JoinInterval has passed since the first fragment was received but not all other fragments are received", userMessage.getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
        if (isTrue(messageGroupEntity.getRejected())) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0040, "A fragment is received that relates to a group that was previously rejected", userMessage.getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
        final Long fragmentCount = messageGroupEntity.getFragmentCount();
        if (fragmentCount != null && ebms3MessageFragmentType.getFragmentCount() != null && ebms3MessageFragmentType.getFragmentCount() > fragmentCount) {
            LOG.error("An incoming message fragment has a a value greater than the known FragmentCount for group [{}]", groupId);
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0048, "An incoming message fragment has a a value greater than the known FragmentCount", userMessage.getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw ex;
        }
    }

    protected void addPartInfoFromFragment(UserMessage userMessage, final Ebms3MessageFragmentType messageFragment) {
        if (messageFragment == null) {
            LOG.debug("No message fragment found");
            return;
        }
        PartInfo partInfo = new PartInfo();
        partInfo.setHref(messageFragment.getHref());
        partInfo.setUserMessage(userMessage);

        partInfoDao.create(partInfo);

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

    @Override
    public List<PartInfo> handlePayloads(SOAPMessage request, Ebms3Messaging ebms3Messaging)
            throws EbMS3Exception, SOAPException, TransformerException {
        LOG.debug("Start handling payloads");

        final String messageId = ebms3Messaging.getUserMessage().getMessageInfo().getMessageId();

        List<PartInfo> result = new ArrayList<>();

        boolean bodyloadFound = false;
        for (final Ebms3PartInfo ebms3PartInfo : ebms3Messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            PartInfo partInfo = convert(ebms3PartInfo);

            final String cid = ebms3PartInfo.getHref();
            LOG.debug("looking for attachment with cid: {}", cid);
            boolean payloadFound = false;
            if (isBodyloadCid(cid)) {
                if (bodyloadFound) {
                    LOG.businessError(DomibusMessageCode.BUS_MULTIPLE_PART_INFO_REFERENCING_SOAP_BODY);
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "More than one Partinfo referencing the soap body found", messageId, null);
                    ex.setMshRole(MSHRole.RECEIVING);
                    throw ex;
                }
                LOG.info("Using soap body payload");
                bodyloadFound = true;
                payloadFound = true;
                Node bodyContent = getChildElement(request);
                LOG.debug("Soap BodyContent when handling payloads: [{}]", bodyContent);

                partInfo.setPayloadDatahandler(getDataHandler(bodyContent));
                partInfo.setInBody(true);
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
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0011, "No Attachment found for cid: " + cid + " of message: " + messageId, messageId, null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
        }
        LOG.debug("Finished handling payloads");

        return result;
    }

    protected PartInfo convert(Ebms3PartInfo ebms3PartInfo) {
        PartInfo result = new PartInfo();

        final Ebms3Description ebms3PartInfoDescription = ebms3PartInfo.getDescription();
        if (ebms3PartInfoDescription != null) {
            Description description = new Description();
            description.setValue(ebms3PartInfoDescription.getValue());
            description.setLang(ebms3PartInfoDescription.getLang());
            result.setDescription(description);
        }
        result.setHref(ebms3PartInfo.getHref());

        final Ebms3PartProperties ebms3PartInfoPartProperties = ebms3PartInfo.getPartProperties();
        final Set<Ebms3Property> ebms3Properties = ebms3PartInfoPartProperties.getProperty();
        if(ebms3PartInfoPartProperties != null || CollectionUtils.isNotEmpty(ebms3Properties)) {
            Set<PartProperty> partProperties = new HashSet<>();

            for (Ebms3Property ebms3Property : ebms3Properties) {
                final PartProperty property = partPropertyDao.findPropertyByName(ebms3Property.getName());
                if(property != null) {
                    partProperties.add(property);
                }
            }

            result.setPartProperties(partProperties);
        }

        return result;
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
        final Transformer transformer = xmlUtil.getTransformerFactory().newTransformer();
        transformer.transform(source, result);
        return new DataHandler(new ByteArrayDataSource(out.toByteArray(), "text/xml"));
    }

    protected String getFinalRecipientName(UserMessage userMessage) {
        for (Property property : userMessage.getMessageProperties()) {
            if (property.getName() != null && property.getName().equalsIgnoreCase(MessageConstants.FINAL_RECIPIENT)) {
                return property.getValue();
            }
        }
        return null;
    }


    @Override
    public ErrorResult createErrorResult(EbMS3Exception ebm3Exception) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setMshRole(eu.domibus.common.MSHRole.RECEIVING);
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
