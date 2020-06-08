package eu.domibus.core.message;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.compression.CompressionException;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.payload.persistence.PayloadPersistence;
import eu.domibus.core.payload.persistence.PayloadPersistenceHelper;
import eu.domibus.core.payload.persistence.PayloadPersistenceProvider;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD;

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
    protected UserMessageService userMessageService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected PayloadPersistenceHelper payloadPersistenceHelper;

    @Override
    public void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException {
        if (messaging == null || messaging.getUserMessage() == null) {
            return;
        }

        if (MSHRole.SENDING == mshRole && messaging.getUserMessage().isSourceMessage()) {
            final Domain currentDomain = domainContextProvider.getCurrentDomain();

            if (scheduleSourceMessagePayloads(messaging)) {
                validatePayloadSizeBeforeSchedulingSave(legConfiguration, messaging);

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
        LOG.debug("Saving Messaging");
        setPayloadsContentType(messaging);
        messagingDao.create(messaging);
    }

    protected boolean scheduleSourceMessagePayloads(Messaging messaging) {
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

        final Long payloadsScheduleThresholdMB = domibusPropertyProvider.getLongProperty(PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
        LOG.debug("Using configured payloadsScheduleThresholdMB [{}]", payloadsScheduleThresholdMB);

        final Long payloadsScheduleThresholdBytes = payloadsScheduleThresholdMB * BYTES_IN_MB;
        if (totalPayloadLength > payloadsScheduleThresholdBytes) {
            LOG.debug("The SourceMessage payloads will be scheduled for saving");
            return true;
        }
        return false;

    }

    protected void validatePayloadSizeBeforeSchedulingSave(LegConfiguration legConfiguration, Messaging messaging) {
        final PayloadInfo payloadInfo = messaging.getUserMessage().getPayloadInfo();
        final List<PartInfo> partInfos = payloadInfo.getPartInfo();

        for (PartInfo partInfo : partInfos) {
            payloadPersistenceHelper.validatePayloadSize(legConfiguration, partInfo.getLength());
        }
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
                storeIncomingPayload(partInfo, messaging.getUserMessage(), legConfiguration);
            } else {
                storeOutgoingPayload(partInfo, messaging.getUserMessage(), legConfiguration, backendName);
            }
        } catch (IOException | EbMS3Exception exc) {
            LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, partInfo.getHref());
            throw new CompressionException("Could not store binary data for message " + exc.getMessage(), exc);
        }
    }

    protected void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration) throws IOException {
        final PayloadPersistence payloadPersistence = payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
        payloadPersistence.storeIncomingPayload(partInfo, userMessage, legConfiguration);

        // Log Payload size
        String messageId = userMessage.getMessageInfo().getMessageId();
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
    }

    protected void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        final PayloadPersistence payloadPersistence = payloadPersistenceProvider.getPayloadPersistence(partInfo, userMessage);
        payloadPersistence.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);

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
