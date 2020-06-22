package eu.domibus.plugin.webService.impl;

import eu.domibus.common.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageAcknowledgeExtException;
import eu.domibus.ext.services.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.entity.WSMessageLog;
import eu.domibus.plugin.webService.generated.*;
import eu.domibus.plugin.webService.generated.ErrorCode;
import eu.domibus.plugin.webService.generated.MessageStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ValidExternallyBoundObject")
@javax.jws.WebService(
        serviceName = "BackendService_1_1",
        portName = "BACKEND_PORT",
        targetNamespace = "http://org.ecodex.backend/1_1/",
        endpointInterface = "eu.domibus.plugin.webService.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendWebServiceImpl extends AbstractBackendConnector<Messaging, UserMessage> implements BackendInterface {

    public static final String MESSAGE_SUBMISSION_FAILED = "Message submission failed";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendWebServiceImpl.class);

    public static final eu.domibus.plugin.webService.generated.ObjectFactory WEBSERVICE_OF = new eu.domibus.plugin.webService.generated.ObjectFactory();

    private static final ObjectFactory EBMS_OBJECT_FACTORY = new ObjectFactory();

    private static final String MIME_TYPE = "MimeType";

    private static final String MESSAGE_ID_EMPTY = "Message ID is empty";

    private static final String MESSAGE_NOT_FOUND_ID = "Message not found, id [";

    protected static final String PROP_LIST_PENDING_MESSAGES_MAXCOUNT = "domibus.listPendingMessages.maxCount";

    @Autowired
    private StubDtoTransformer defaultTransformer;

    @Autowired
    private MessageAcknowledgeExtService messageAcknowledgeExtService;

    @Autowired
    protected BackendWebServiceExceptionFactory backendWebServiceExceptionFactory;

    @Autowired
    protected WSMessageLogDao wsMessageLogDao;

    @Autowired
    protected DomainExtService domainExtService;

    @Autowired
    private DomainContextExtService domainContextExtService;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    AuthenticationExtService authenticationExtService;

    public BackendWebServiceImpl(final String name) {
        super(name);
    }

    /**
     * Add support for large files using DataHandler instead of byte[]
     *
     * @param submitRequest
     * @param ebMSHeaderInfo
     * @return {@link SubmitResponse} object
     * @throws SubmitMessageFault
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 1200) // 20 minutes
    public SubmitResponse submitMessage(SubmitRequest submitRequest, Messaging ebMSHeaderInfo) throws SubmitMessageFault {
        LOG.debug("Received message");

        addPartInfos(submitRequest, ebMSHeaderInfo);
        if (ebMSHeaderInfo.getUserMessage().getMessageInfo() == null) {
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setTimestamp(LocalDateTime.now());
            ebMSHeaderInfo.getUserMessage().setMessageInfo(messageInfo);
        } else {
            final String submittedMessageId = ebMSHeaderInfo.getUserMessage().getMessageInfo().getMessageId();
            if (StringUtils.isNotEmpty(submittedMessageId)) {
                //if there is a submitted messageId we trim it
                LOG.debug("Submitted messageId=[{}]", submittedMessageId);
                String trimmedMessageId = messageExtService.cleanMessageIdentifier(submittedMessageId);
                ebMSHeaderInfo.getUserMessage().getMessageInfo().setMessageId(trimmedMessageId);
            }
        }

        final String messageId;
        try {
            messageId = this.submit(ebMSHeaderInfo);
        } catch (final MessagingProcessingException mpEx) {
            LOG.error(MESSAGE_SUBMISSION_FAILED, mpEx);
            throw new SubmitMessageFault(MESSAGE_SUBMISSION_FAILED, generateFaultDetail(mpEx));
        }
        LOG.info("Received message from backend with messageID [{}]", messageId);
        final SubmitResponse response = WEBSERVICE_OF.createSubmitResponse();
        response.getMessageID().add(messageId);
        return response;
    }

    @Override
    public void deliverMessage(final DeliverMessageEvent event) {
        LOG.info("Deliver message: [{}]", event);
        WSMessageLog wsMessageLog = new WSMessageLog(event.getMessageId(), event.getFinalRecipient());
        wsMessageLogDao.create(wsMessageLog);
    }

    @Override
    public void messageReceiveFailed(final MessageReceiveFailureEvent event) {
        LOG.info("Message receive failed [{}]", event);
    }

    @Override
    public void messageStatusChanged(final MessageStatusChangeEvent event) {
        LOG.info("Message status changed [{}]", event);
    }

    @Override
    public void messageSendFailed(final MessageSendFailedEvent event) {
        LOG.info("Message send failed [{}]", event);
    }

    @Override
    public void messageSendSuccess(final MessageSendSuccessEvent event) {
        LOG.info("Message send success [{}]", event.getMessageId());
    }

    private void addPartInfos(SubmitRequest submitRequest, Messaging ebMSHeaderInfo) throws SubmitMessageFault {

        if (getPayloadInfo(ebMSHeaderInfo) == null) {
            return;
        }

        validateSubmitRequest(submitRequest, ebMSHeaderInfo);

        List<PartInfo> partInfoList = getPartInfo(ebMSHeaderInfo);
        List<ExtendedPartInfo> partInfosToAdd = new ArrayList<>();

        for (Iterator<PartInfo> i = partInfoList.iterator(); i.hasNext(); ) {
            ExtendedPartInfo extendedPartInfo = new ExtendedPartInfo(i.next());
            partInfosToAdd.add(extendedPartInfo);
            i.remove();

            boolean foundPayload = false;
            final String href = extendedPartInfo.getHref();
            LOG.debug("Looking for payload: {}", href);
            for (final LargePayloadType payload : submitRequest.getPayload()) {
                LOG.debug("comparing with payload id: " + payload.getPayloadId());
                if (StringUtils.equalsIgnoreCase(payload.getPayloadId(), href)) {
                    this.copyPartProperties(payload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(false);
                    LOG.debug("sendMessage - payload Content Type: " + payload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(payload.getValue());
                    foundPayload = true;
                    break;
                }
            }

            if (!foundPayload) {
                final LargePayloadType bodyload = submitRequest.getBodyload();
                if (bodyload == null) {
                    // in this case the payload referenced in the partInfo was neither an external payload nor a bodyload
                    throw new SubmitMessageFault("No Payload or Bodyload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
                // It can only be in body load, href MAY be null!
                if (href == null && bodyload.getPayloadId() == null || href != null && StringUtils.equalsIgnoreCase(href, bodyload.getPayloadId())) {
                    this.copyPartProperties(bodyload.getContentType(), extendedPartInfo);
                    extendedPartInfo.setInBody(true);
                    LOG.debug("sendMessage - bodyload Content Type: " + bodyload.getContentType());
                    extendedPartInfo.setPayloadDatahandler(bodyload.getValue());
                } else {
                    throw new SubmitMessageFault("No payload found for PartInfo with href: " + extendedPartInfo.getHref(), generateDefaultFaultDetail(extendedPartInfo.getHref()));
                }
            }
        }
        partInfoList.addAll(partInfosToAdd);
    }

    protected void validateSubmitRequest(SubmitRequest submitRequest, Messaging ebMSHeaderInfo) throws SubmitMessageFault {
        for (final LargePayloadType payload : submitRequest.getPayload()) {
            if (StringUtils.isBlank(payload.getPayloadId())) {
                throw new SubmitMessageFault("Invalid request", generateDefaultFaultDetail("Attribute 'payloadId' of the 'payload' element must not be empty"));
            }
        }
        final LargePayloadType bodyload = submitRequest.getBodyload();
        if (bodyload != null && StringUtils.isNotBlank(bodyload.getPayloadId())) {
            throw new SubmitMessageFault("Invalid request", generateDefaultFaultDetail("Attribute 'payloadId' must not appear on element 'bodyload'"));
        }
    }

    private FaultDetail generateFaultDetail(MessagingProcessingException mpEx) {
        FaultDetail fd = WEBSERVICE_OF.createFaultDetail();
        fd.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
        fd.setMessage(mpEx.getMessage());
        return fd;
    }

    private FaultDetail generateDefaultFaultDetail(String message) {
        FaultDetail fd = WEBSERVICE_OF.createFaultDetail();
        fd.setCode(ErrorCode.EBMS_0004.name());
        fd.setMessage(message);
        return fd;
    }

    private void copyPartProperties(final String payloadContentType, final ExtendedPartInfo partInfo) {
        final PartProperties partProperties = new PartProperties();
        Property prop;

        // add all partproperties WEBSERVICE_OF the backend message
        if (partInfo.getPartProperties() != null) {
            for (final Property property : partInfo.getPartProperties().getProperty()) {
                prop = new Property();

                prop.setName(property.getName());
                prop.setValue(property.getValue());
                partProperties.getProperty().add(prop);
            }
        }

        boolean mimeTypePropFound = false;
        for (final Property property : partProperties.getProperty()) {
            if (MIME_TYPE.equals(property.getName())) {
                mimeTypePropFound = true;
                break;
            }
        }
        // in case there was no property with name {@value Property.MIME_TYPE} and xmime:contentType attribute was set noinspection SuspiciousMethodCalls
        if (!mimeTypePropFound && payloadContentType != null) {
            prop = new Property();
            prop.setName(MIME_TYPE);
            prop.setValue(payloadContentType);
            partProperties.getProperty().add(prop);
        }
        partInfo.setPartProperties(partProperties);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 300) // 5 minutes
    public ListPendingMessagesResponse listPendingMessages(final Object listPendingMessagesRequest) {
        DomainDTO domainDTO = domainContextExtService.getCurrentDomainSafely();
        LOG.info("ListPendingMessages for domain [{}]", domainDTO);

        final ListPendingMessagesResponse response = WEBSERVICE_OF.createListPendingMessagesResponse();
        final Collection<String> ids;
        final int intMaxPendingMessagesRetrieveCount = new Integer(domibusPropertyExtService.getProperty(PROP_LIST_PENDING_MESSAGES_MAXCOUNT));
        LOG.debug("maxPendingMessagesRetrieveCount [{}]", intMaxPendingMessagesRetrieveCount);

        String originalUser = null;
        if (!authenticationExtService.isUnsecureLoginAllowed()) {
            originalUser = authenticationExtService.getOriginalUser();
            LOG.info("Original user is [{}]", originalUser);
        }

        List<WSMessageLog> pending;
        if (originalUser != null) {
            pending = wsMessageLogDao.findAllByFinalRecipient(intMaxPendingMessagesRetrieveCount, originalUser);
        } else {
            pending = wsMessageLogDao.findAll(intMaxPendingMessagesRetrieveCount);
        }

        ids = pending.stream()
                .map(WSMessageLog::getMessageId).collect(Collectors.toList());
        response.getMessageID().addAll(ids);
        return response;
    }

    /**
     * Add support for large files using DataHandler instead of byte[]
     *
     * @param retrieveMessageRequest
     * @param retrieveMessageResponse
     * @param ebMSHeaderInfo
     * @throws RetrieveMessageFault
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 300, rollbackFor = RetrieveMessageFault.class)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void retrieveMessage(RetrieveMessageRequest retrieveMessageRequest, Holder<RetrieveMessageResponse> retrieveMessageResponse,
                                Holder<Messaging> ebMSHeaderInfo) throws RetrieveMessageFault {
        UserMessage userMessage;
        boolean isMessageIdNotEmpty = StringUtils.isNotEmpty(retrieveMessageRequest.getMessageID());

        if (!isMessageIdNotEmpty) {
            LOG.error(MESSAGE_ID_EMPTY);
            throw new RetrieveMessageFault(MESSAGE_ID_EMPTY, backendWebServiceExceptionFactory.createFault("MessageId is empty"));
        }

        String trimmedMessageId = messageExtService.cleanMessageIdentifier(retrieveMessageRequest.getMessageID());
        WSMessageLog wsMessageLog = wsMessageLogDao.findByMessageId(trimmedMessageId);
        if (wsMessageLog == null) {
            LOG.businessError(DomibusMessageCode.BUS_MSG_NOT_FOUND, trimmedMessageId);
            throw new RetrieveMessageFault(MESSAGE_NOT_FOUND_ID + trimmedMessageId + "]", backendWebServiceExceptionFactory.createFault("No message with id [" + trimmedMessageId + "] pending for download"));
        }

        try {
            userMessage = downloadMessage(trimmedMessageId, null);
        } catch (final MessageNotFoundException mnfEx) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]", mnfEx);
            }
            LOG.error(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]");
            throw new RetrieveMessageFault(MESSAGE_NOT_FOUND_ID + trimmedMessageId + "]", backendWebServiceExceptionFactory.createDownloadMessageFault(mnfEx));
        }

        if (userMessage == null) {
            LOG.error(MESSAGE_NOT_FOUND_ID + retrieveMessageRequest.getMessageID() + "]");
            throw new RetrieveMessageFault(MESSAGE_NOT_FOUND_ID + trimmedMessageId + "]", backendWebServiceExceptionFactory.createFault("UserMessage not found"));
        }

        // To avoid blocking errors during the Header's response validation
        if (StringUtils.isEmpty(userMessage.getCollaborationInfo().getAgreementRef().getValue())) {
            userMessage.getCollaborationInfo().setAgreementRef(null);
        }
        Messaging messaging = EBMS_OBJECT_FACTORY.createMessaging();
        messaging.setUserMessage(userMessage);
        ebMSHeaderInfo.value = messaging;
        retrieveMessageResponse.value = WEBSERVICE_OF.createRetrieveMessageResponse();

        fillInfoPartsForLargeFiles(retrieveMessageResponse, messaging);

        try {
            messageAcknowledgeExtService.acknowledgeMessageDelivered(trimmedMessageId, new Timestamp(System.currentTimeMillis()));
        } catch (AuthenticationExtException | MessageAcknowledgeExtException e) {
            //if an error occurs related to the message acknowledgement do not block the download message operation
            LOG.error("Error acknowledging message [" + retrieveMessageRequest.getMessageID() + "]", e);
        }

        // remove downloaded message from the plugin table containing the pending messages
        wsMessageLogDao.delete(wsMessageLog);
    }

    private void fillInfoPartsForLargeFiles(Holder<RetrieveMessageResponse> retrieveMessageResponse, Messaging messaging) {
        if (getPayloadInfo(messaging) == null || CollectionUtils.isEmpty(getPartInfo(messaging))) {
            LOG.info("No payload found for message [{}]", messaging.getUserMessage().getMessageInfo().getMessageId());
            return;
        }

        for (final PartInfo partInfo : getPartInfo(messaging)) {
            ExtendedPartInfo extPartInfo = (ExtendedPartInfo) partInfo;
            LargePayloadType payloadType = WEBSERVICE_OF.createLargePayloadType();
            if (extPartInfo.getPayloadDatahandler() != null) {
                LOG.debug("payloadDatahandler Content Type: " + extPartInfo.getPayloadDatahandler().getContentType());
                payloadType.setValue(extPartInfo.getPayloadDatahandler());
            }
            if (extPartInfo.isInBody()) {
                retrieveMessageResponse.value.setBodyload(payloadType);
            } else {
                payloadType.setPayloadId(partInfo.getHref());
                retrieveMessageResponse.value.getPayload().add(payloadType);
            }
        }
    }

    private PayloadInfo getPayloadInfo(Messaging messaging) {
        if (messaging.getUserMessage() == null) {
            return null;
        }
        return messaging.getUserMessage().getPayloadInfo();
    }

    private List<PartInfo> getPartInfo(Messaging messaging) {
        PayloadInfo payloadInfo = getPayloadInfo(messaging);
        if (payloadInfo == null) {
            return new ArrayList<>();
        }
        return payloadInfo.getPartInfo();
    }


    @Override
    public MessageStatus getStatus(final StatusRequest statusRequest) throws StatusFault {
        boolean isMessageIdNotEmpty = StringUtils.isNotEmpty(statusRequest.getMessageID());

        if (!isMessageIdNotEmpty) {
            LOG.error(MESSAGE_ID_EMPTY);
            throw new StatusFault(MESSAGE_ID_EMPTY, backendWebServiceExceptionFactory.createFault("MessageId is empty"));
        }
        String trimmedMessageId = messageExtService.cleanMessageIdentifier(statusRequest.getMessageID());
        return defaultTransformer.transformFromMessageStatus(messageRetriever.getStatus(trimmedMessageId));
    }

    @Override
    public ErrorResultImplArray getMessageErrors(final GetErrorsRequest messageErrorsRequest) {
        return defaultTransformer.transformFromErrorResults(messageRetriever.getErrorsForMessage(messageErrorsRequest.getMessageID()));
    }

    @Override
    public MessageSubmissionTransformer<Messaging> getMessageSubmissionTransformer() {
        return this.defaultTransformer;
    }

    @Override
    public MessageRetrievalTransformer<UserMessage> getMessageRetrievalTransformer() {
        return this.defaultTransformer;
    }

}
