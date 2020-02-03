package eu.domibus.common.services.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.nonrepudiation.NonRepudiationService;
import eu.domibus.core.payload.persistence.PayloadPersistence;
import eu.domibus.core.payload.persistence.PayloadPersistenceProvider;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.PModeDefaultService;
import eu.domibus.core.pull.PartyExtractor;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD;

/**
 * @author Ioana Dragusanu
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessagingServiceImpl implements MessagingService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingServiceImpl.class);

    public static final String MIME_TYPE_APPLICATION_UNKNOWN = "application/unknown";
    public static final String PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD = DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD;

    protected static Long BYTES_IN_MB = 1048576L;


    @Autowired
    protected MessagingDao messagingDao;

    @Autowired
    protected PayloadPersistenceProvider payloadPersistenceProvider;

    @Autowired
    protected PayloadFileStorageProvider storageProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserMessageDefaultService userMessageService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    protected MessageExchangeService messageExchangeService;

    @Autowired
    protected PullMessageService pullMessageService;

    @Autowired
    protected PModeDefaultService pModeDefaultService;

    @Autowired
    protected AS4ReceiptService as4ReceiptService;

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    private MetricRegistry metricRegistry;

    @Override
    public void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException {
        if (messaging == null || messaging.getUserMessage() == null) {
            return;
        }

        if (MSHRole.SENDING == mshRole && messaging.getUserMessage().isSourceMessage()) {
            final Domain currentDomain = domainContextProvider.getCurrentDomain();

            if (scheduleSourceMessagePayloads(messaging, currentDomain)) {
                //stores the payloads asynchronously
                domainTaskExecutor.submitLongRunningTask(
                        () -> {
                            LOG.debug("Scheduling the SourceMessage saving");
                            storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);
                        },
                        () -> splitAndJoinService.setSourceMessageAsFailed(messaging.getUserMessage()),
                        currentDomain);
            } else {
                //stores the payloads synchronously
                storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);
            }
        } else {
            storePayloads(messaging, mshRole, legConfiguration, backendName);
        }
        setPayloadsContentType(messaging);
    }

    @Override
    @Transactional
    public void persistReceivedMessage(Messaging messaging, Messaging responseMessaging, BackendFilter matchingBackendFilter, UserMessage userMessage, String backendName, Party to, NotificationStatus notificationStatus, String rawXMLMessage) throws EbMS3Exception {
        LOG.debug("Saving Messaging");

        userMessageLogService.save(
                userMessage,
                userMessage.getMessageInfo().getMessageId(),
                MessageStatus.RECEIVED.toString(),
                notificationStatus.toString(),
                MSHRole.RECEIVING.toString(),
                0,
                StringUtils.isEmpty(userMessage.getMpc()) ? Ebms3Constants.DEFAULT_MPC : userMessage.getMpc(),
                backendName,
                to.getEndpoint(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getAction(), userMessage.isSourceMessage(), userMessage.isUserMessageFragment(), false);

        //one to one mapping
        SignalMessage signalMessage = responseMessaging.getSignalMessage();
        signalMessage.getReceipt().setSignalMessage(signalMessage);
        signalMessage.setMessaging(messaging);
        userMessage.setMessaging(messaging);

        as4ReceiptService.saveResponse(userMessage, signalMessage);

        Timer.Context timeContext = metricRegistry.timer(MetricRegistry.name(MessagingServiceImpl.class, "messagingDao.create")).time();
        messaging.setSignalMessage(signalMessage);
        messagingDao.create(messaging);
        timeContext.stop();

        try {
            Timer.Context notifyMessageReceived = metricRegistry.timer(MetricRegistry.name(UserMessageHandlerServiceImpl.class, "notifyMessageReceived")).time();
            backendNotificationService.notifyMessageReceived(matchingBackendFilter, messaging.getUserMessage());
            notifyMessageReceived.stop();
        } catch (SubmissionValidationException e) {
            String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_VALIDATION_FAILED, messageId);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, e.getMessage(), messageId, e);
        }


        nonRepudiationService.saveRequest(rawXMLMessage, userMessage);
    }

    @Override
    @Transactional
    public void persistSubmittedMessage(Submission messageData, String backendName, UserMessage userMessage, String messageId, Messaging message, MessageExchangeConfiguration userMessageExchangeConfiguration, Party to, MessageStatus messageStatus, String pModeKey, LegConfiguration legConfiguration) {
        LOG.debug("Saving Messaging");
        com.codahale.metrics.Timer.Context timeContext = metricRegistry.timer(MetricRegistry.name(MessagingServiceImpl.class, "messagingDao.create")).time();

        //one to one mapping
        userMessage.setMessaging(message);

        messagingDao.create(message);
        timeContext.stop();

        if (messageStatus == null) {
            messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
        }
        final boolean sourceMessage = userMessage.isSourceMessage();

        if (!sourceMessage) {
            if (MessageStatus.READY_TO_PULL != messageStatus) {
                // Sends message to the proper queue if not a message to be pulled.

                final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageId, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                        MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(),
                        backendName, to.getEndpoint(), messageData.getService(), messageData.getAction(), sourceMessage, null, true);

                userMessageService.scheduleSendingToQueue(messageId, false);
            } else {
                final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageId, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                        MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(),
                        backendName, to.getEndpoint(), messageData.getService(), messageData.getAction(), sourceMessage, null, false);
                LOG.debug("[submit]:Message:[{}] add lock", userMessageLog.getMessageId());
                pullMessageService.addPullMessageLock(new PartyExtractor(to), pModeKey, userMessageLog);
            }
        }
    }

    @Override
    @Transactional
    public void persistSubmittedMessageFragment(String backendName, UserMessage userMessage, String messageId, Messaging message, MessageExchangeConfiguration userMessageExchangeConfiguration, Party to, String pModeKey, LegConfiguration legConfiguration) {
        LOG.debug("Saving Messaging");
        com.codahale.metrics.Timer.Context timeContext = metricRegistry.timer(MetricRegistry.name(MessagingServiceImpl.class, "messagingDao.create")).time();
        messagingDao.create(message);
        timeContext.stop();

        MessageStatus messageStatus = messageExchangeService.getMessageStatus(userMessageExchangeConfiguration);
        final UserMessageLog userMessageLog = userMessageLogService.save(userMessage, messageId, messageStatus.toString(), pModeDefaultService.getNotificationStatus(legConfiguration).toString(),
                MSHRole.SENDING.toString(), getMaxAttempts(legConfiguration), message.getUserMessage().getMpc(),
                backendName, to.getEndpoint(), userMessage.getCollaborationInfo().getService().getValue(), userMessage.getCollaborationInfo().getAction(), null, true, true);
        prepareForPushOrPull(userMessageLog, pModeKey, to, messageStatus);
    }

    protected int getMaxAttempts(LegConfiguration legConfiguration) {
        return (legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount()) + 1; // counting retries after the first send attempt
    }

    protected void prepareForPushOrPull(UserMessageLog userMessageLog, String pModeKey, Party to, MessageStatus messageStatus) {
        if (MessageStatus.READY_TO_PULL != messageStatus) {
            // Sends message to the proper queue if not a message to be pulled.
            userMessageService.scheduleSending(userMessageLog);
        } else {
            LOG.debug("[submit]:Message:[{}] add lock", userMessageLog.getMessageId());
            pullMessageService.addPullMessageLock(new PartyExtractor(to), pModeKey, userMessageLog);
        }
    }

    protected boolean scheduleSourceMessagePayloads(Messaging messaging, final Domain domain) {
        final PayloadInfo payloadInfo = messaging.getUserMessage().getPayloadInfo();
        final List<PartInfo> partInfos = payloadInfo.getPartInfo();
        if (payloadInfo == null || partInfos == null || partInfos.isEmpty()) {
            LOG.debug("SourceMessages does not have any payloads");
            return false;
        }

        long totalPayloadLength = 0;
        for (PartInfo partInfo : partInfos) {
            totalPayloadLength += partInfo.getLength();
        }
        LOG.debug("SourceMessage payloads totalPayloadLength(bytes) [{}]", totalPayloadLength);

        final Long payloadsScheduleThresholdMB = domibusPropertyProvider.getLongDomainProperty(domain, PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
        LOG.debug("Using configured payloadsScheduleThresholdMB [{}]", payloadsScheduleThresholdMB);

        final Long payloadsScheduleThresholdBytes = payloadsScheduleThresholdMB * BYTES_IN_MB;
        if (totalPayloadLength > payloadsScheduleThresholdBytes) {
            LOG.debug("The SourceMessage payloads will be scheduled for saving");
            return true;
        }
        return false;

    }

    protected void storeSourceMessagePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName) {
        LOG.debug("Saving the SourceMessage payloads");

        storePayloads(messaging, mshRole, legConfiguration, backendName);

        final String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        LOG.debug("Scheduling the SourceMessage sending");
        userMessageService.scheduleSourceMessageSending(messageId);
    }

    protected void setPayloadsContentType(Messaging messaging) {
        if (messaging.getUserMessage().getPayloadInfo() == null || messaging.getUserMessage().getPayloadInfo().getPartInfo() == null) {
            return;
        }
        for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            setContentType(partInfo);
        }
    }

    @Override
    public void storePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName) {
        if (messaging.getUserMessage().getPayloadInfo() == null || messaging.getUserMessage().getPayloadInfo().getPartInfo() == null) {
            LOG.debug("No payloads to store");
            return;
        }

        LOG.debug("Storing payloads");

        for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            storePayload(messaging, mshRole, legConfiguration, backendName, partInfo);
        }
        LOG.debug("Finished storing payloads");
    }

    protected void storePayload(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName, PartInfo partInfo) {
        try {
            if (MSHRole.RECEIVING.equals(mshRole)) {
                storeIncomingPayload(partInfo, messaging.getUserMessage());
            } else {
                storeOutgoingPayload(partInfo, messaging.getUserMessage(), legConfiguration, backendName);
            }
        } catch (IOException | EbMS3Exception exc) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, partInfo.getHref());
            throw new CompressionException("Could not store binary data for message " + exc.getMessage(), exc);
        }
    }

    protected void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage) throws IOException {
        Timer.Context timeContext = metricRegistry.timer(MetricRegistry.name(MessagingServiceImpl.class, "storeIncomingPayload")).time();

        final PayloadPersistence payloadPersistence = payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
        payloadPersistence.storeIncomingPayload(partInfo, userMessage);
        timeContext.stop();

        // Log Payload size
        String messageId = userMessage.getMessageInfo().getMessageId();
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
    }

    protected void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        Timer.Context timeContext = metricRegistry.timer(MetricRegistry.name(MessagingServiceImpl.class, "storeOutgoingPayload")).time();

        final PayloadPersistence payloadPersistence = payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
        payloadPersistence.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);
        timeContext.stop();

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SENDING_PAYLOAD_SIZE, partInfo.getHref(), userMessage.getMessageInfo().getMessageId(), partInfo.getLength());

        final boolean hasCompressionProperty = hasCompressionProperty(partInfo);
        if (hasCompressionProperty) {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION, partInfo.getHref());
        }
    }

    protected void setContentType(PartInfo partInfo) {
        String contentType = partInfo.getPayloadDatahandler().getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = MIME_TYPE_APPLICATION_UNKNOWN;
        }
        LOG.debug("Setting the payload [{}] content type to [{}]", partInfo.getHref(), contentType);
        partInfo.setMime(contentType);
    }

    protected boolean hasCompressionProperty(PartInfo partInfo) {
        if (partInfo.getPartProperties() == null) {
            return false;
        }

        for (final Property property : partInfo.getPartProperties().getProperties()) {
            if (property.getName().equalsIgnoreCase(CompressionService.COMPRESSION_PROPERTY_KEY)
                    && property.getValue().equalsIgnoreCase(CompressionService.COMPRESSION_PROPERTY_VALUE)) {
                return true;
            }
        }

        return false;
    }
}
