package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.*;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.*;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.*;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.jms.spi.InternalJMSConstants.RETENTION_MESSAGE_QUEUE;
import static eu.domibus.messaging.MessageConstants.*;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 *
 * @author Cosmin Baciu, Ioana Dragusanu
 * @since 3.0
 */
@Service
public class MessageRetentionDefaultService implements MessageRetentionService {

    public static final String DELETE_TYPE = "DELETE_TYPE";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionDefaultService.class);
    public static final String NO_MESSAGE_TO_BE_SCHEDULED_FOR_DELETION = "No message to be scheduled for deletion";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Autowired
    private JMSManager jmsManager;

    @Autowired
    @Qualifier(RETENTION_MESSAGE_QUEUE)
    private Queue retentionMessageQueue;

    @Autowired
    private UserMessageDefaultService userMessageDefaultService;

    @Autowired
    private UserMessageLogDefaultService userMessageLogService;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    @Autowired
    protected PartInfoService partInfoService;

    @Autowired
    private BackendNotificationService backendNotificationService;

    @Override
    public boolean handlesDeletionStrategy(String retentionStrategy) {
        return DeletionStrategy.DEFAULT == DeletionStrategy.valueOf(retentionStrategy);
    }

    @Transactional
    @Override
    public void deleteAllMessages() {
        final List<UserMessageLogDto> allMessages = userMessageLogDao.getAllMessages();
        userMessageDefaultService.deleteMessages(allMessages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionDefaultService.class, value = "retention_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionDefaultService.class, value = "retention_deleteExpiredMessages")
    public void deleteExpiredMessages() {

        LOG.debug("Calling MessageRetentionDefaultService.deleteExpiredMessages");
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        final Integer expiredSentMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
        final Integer expiredPayloadDeletedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_PAYLOAD_DELETED_MAX_DELETE);

        final boolean eArchiveIsActive = BooleanUtils.isTrue(domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE));

        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit, eArchiveIsActive);
        }
    }

    public void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit, Integer expiredSentMessagesLimit, Integer expiredPayloadDeletedMessagesLimit, boolean eArchiveIsActive) {
        LOG.debug("Deleting expired messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]" +
                " and expiredNotDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit, eArchiveIsActive);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit, eArchiveIsActive);
        deleteExpiredSentMessages(mpc, expiredSentMessagesLimit, eArchiveIsActive);
        deleteExpiredPayloadDeletedMessages(mpc, expiredPayloadDeletedMessagesLimit, eArchiveIsActive);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer deleteMessagesLimit, boolean eArchiveIsActive) {
        final int messageRetentionMinutes = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        // If messageRetentionMinutes is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionMinutes < 0) {
            LOG.trace("Retention of downloaded messages is not active.");
            return;
        }

        int metadataRetentionOffset = pModeProvider.getMetadataRetentionOffsetByMpcURI(mpc);
        LOG.debug("Deleting expired downloaded messages for MPC [{}] using deleteMessagesLimit [{}], messageRetentionMinutes [{}], metadataRetentionOffset [{}]",
                mpc, deleteMessagesLimit, messageRetentionMinutes, metadataRetentionOffset);
        Date messageRetentionDate = DateUtils.addMinutes(new Date(), messageRetentionMinutes * -1);
        List<UserMessageLogDto> messagesToClean = userMessageLogDao.getDownloadedUserMessagesOlderThan(messageRetentionDate,
                mpc, deleteMessagesLimit, eArchiveIsActive);
        if (pModeProvider.isDeleteMessageMetadataByMpcURI(mpc) && metadataRetentionOffset == 0) {
            deleteMessageMetadataAndPayload(mpc, messagesToClean);
            return;
        }
        deleteMessagePayload(messagesToClean);
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer deleteMessagesLimit, boolean eArchiveIsActive) {
        final int messageRetentionMinutes = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionMinutes < 0) {// if -1 the messages will be kept indefinitely and if 0, although it makes no sense, is legal
            LOG.trace("Retention of not downloaded messages is not active.");
            return;
        }

        int metadataRetentionOffset = pModeProvider.getMetadataRetentionOffsetByMpcURI(mpc);
        LOG.debug("Deleting expired not-downloaded messages for MPC [{}] using deleteMessagesLimit [{}], messageRetentionMinutes [{}], metadataRetentionOffset [{}]",
                mpc, deleteMessagesLimit, messageRetentionMinutes, metadataRetentionOffset);
        Date payloadRetentionLimit = DateUtils.addMinutes(new Date(), messageRetentionMinutes * -1);
        List<UserMessageLogDto> messagesToClean = userMessageLogDao.getUndownloadedUserMessagesOlderThan(payloadRetentionLimit,
                mpc, deleteMessagesLimit, eArchiveIsActive);
        if (pModeProvider.isDeleteMessageMetadataByMpcURI(mpc) && metadataRetentionOffset == 0) {
            deleteMessageMetadataAndPayload(mpc, messagesToClean);
            return;
        }
        deleteMessagePayload(messagesToClean);
    }

    protected void deleteExpiredSentMessages(String mpc, Integer deleteMessagesLimit, boolean eArchiveIsActive) {
        final int messageRetentionMinutes = pModeProvider.getRetentionSentByMpcURI(mpc);

        if (messageRetentionMinutes < 0) { // if -1 the messages will be kept indefinitely
            LOG.trace("Retention of sent messages is not active.");
            return;
        }

        int metadataRetentionOffset = pModeProvider.getMetadataRetentionOffsetByMpcURI(mpc);
        boolean isDeleteMessageMetadata= pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        LOG.debug("Deleting expired sent messages for MPC [{}] using deleteMessagesLimit [{}], messageRetentionMinutes [{}], metadataRetentionOffset [{}]",
                mpc, deleteMessagesLimit, messageRetentionMinutes, metadataRetentionOffset);
        Date messageRetentionDate = DateUtils.addMinutes(new Date(), messageRetentionMinutes * -1);
        if (isDeleteMessageMetadata && metadataRetentionOffset == 0) {
            List<UserMessageLogDto> messagesToClean = userMessageLogDao.getSentUserMessagesOlderThan(messageRetentionDate,
                    mpc, deleteMessagesLimit, eArchiveIsActive);
            deleteMessageMetadataAndPayload(mpc, messagesToClean);
            return;
        }
        List<UserMessageLogDto> messagesToClean = userMessageLogDao.getSentUserMessagesOlderThan(messageRetentionDate,
                mpc, deleteMessagesLimit, eArchiveIsActive);
        deleteMessagePayload(messagesToClean);
    }

    protected void deleteExpiredPayloadDeletedMessages(String mpc, Integer deleteMessagesLimit, boolean eArchiveIsActive) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        if (!isDeleteMessageMetadata || deleteMessagesLimit < 0) { // only delete of entire messages if delete metadata is true
            LOG.trace("Retention of payload deleted messages is not active.");
            return;
        }

        int metadataRetentionOffset = pModeProvider.getMetadataRetentionOffsetByMpcURI(mpc);
        LOG.debug("Deleting expired deleted messages for MPC [{}] using deleteMessagesLimit [{}], metadataRetentionOffset [{}]",
                mpc, deleteMessagesLimit, metadataRetentionOffset);
        Date messageRetentionDate = DateUtils.addMinutes(new Date(), 1 - metadataRetentionOffset);  // give 1 minute for the previous state
        final List<UserMessageLogDto> messagesToClean = userMessageLogDao.getDeletedUserMessagesOlderThan(messageRetentionDate,
                mpc, deleteMessagesLimit, eArchiveIsActive);
        deleteMessageMetadataAndPayload(mpc, messagesToClean);
    }

    private void deleteMessagePayload(List<UserMessageLogDto> messagesToClean) {
        if (CollectionUtils.isEmpty(messagesToClean)) {
            LOG.debug("Found 0 message payloads to delete");
            return;
        }
        final int deleted = messagesToClean.size();
        LOG.debug("Attempting to delete the payloads of [{}] messages", deleted);
        List<Long> entityIds = messagesToClean.stream()
                .map(UserMessageLogDto::getEntityId)
                .collect(Collectors.toList());
        userMessageDefaultService.clearPayloadData(entityIds);
        backendNotificationService.notifyMessageDeleted(messagesToClean);
        LOG.debug("Deleted the payloads of [{}] messages ", deleted);
    }

    private void deleteMessageMetadataAndPayload(String mpc, List<UserMessageLogDto> messagesToClean) {
        if (CollectionUtils.isEmpty(messagesToClean)) {
            LOG.debug("Found 0 messages to delete using mpc [{}]", mpc);
            return;
        }
        final int deleted = messagesToClean.size();
        LOG.debug("Attempting to delete [{}] messages using mpc [{}]", deleted, mpc);
        deleteMessages(messagesToClean, mpc);
        LOG.debug("Deleted [{}] messages using mpc [{}]", deleted, mpc);
    }

    public void deleteMessages(List<UserMessageLogDto> userMessageLogs, String mpc) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        LOG.trace("isDeleteMessageMetadata [{}]", isDeleteMessageMetadata);
        if (isDeleteMessageMetadata) { // delete in batch
            final int maxBatch = pModeProvider.getRetentionMaxBatchByMpcURI(mpc, domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE));
            LOG.debug("Bulk delete messages, maxBatch [{}]", maxBatch);
            deleteMessages(userMessageLogs, maxBatch);
            return;
        }
        // schedule delete one by one
        LOG.debug("Schedule delete messages one by one");
        scheduleDeleteMessagesByMessageLog(userMessageLogs);
    }

    public void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogDtos) {
        if (CollectionUtils.isEmpty(userMessageLogDtos)) {
            LOG.debug(NO_MESSAGE_TO_BE_SCHEDULED_FOR_DELETION);
            return;
        }

        LOG.debug("Scheduling delete messages [{}]", userMessageLogDtos);
        userMessageLogDtos.forEach(messageLogDto -> {
            JmsMessage message = JMSMessageBuilder.create()
                    .property(DELETE_TYPE, MessageDeleteType.SINGLE.name())
                    .property(MESSAGE_ID, messageLogDto.getMessageId())

                    .property(MSH_ROLE, Optional.ofNullable(messageLogDto.getMshRole()).map(MSHRole::name).orElse(null)) // role might not be set!!!!
                    .property(FINAL_RECIPIENT, messageLogDto.getProperties().get(FINAL_RECIPIENT))
                    .property(ORIGINAL_SENDER, messageLogDto.getProperties().get(ORIGINAL_SENDER))
                    .build();
            jmsManager.sendMessageToQueue(message, retentionMessageQueue);
        });
    }

    public void scheduleDeleteMessages(List<UserMessage> userMessages) {
        if (CollectionUtils.isEmpty(userMessages)) {
            LOG.debug(NO_MESSAGE_TO_BE_SCHEDULED_FOR_DELETION);
            return;
        }

        LOG.debug("Scheduling delete messages [{}]", userMessages);
        userMessages.forEach(userMessage -> {
            Map<String, String> properties = userMessageServiceHelper.getProperties(userMessage);

            JmsMessage message = JMSMessageBuilder.create()
                    .property(DELETE_TYPE, MessageDeleteType.SINGLE.name())
                    .property(MESSAGE_ID, userMessage.getMessageId())
                    .property(MSH_ROLE, userMessage.getMshRole().getRole().name())
                    .property(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT))
                    .property(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER))
                    .build();
            jmsManager.sendMessageToQueue(message, retentionMessageQueue);
        });
    }

    public void deletePayloadOnSendSuccess(UserMessage userMessage, UserMessageLog userMessageLog) {
        if (shouldDeletePayloadOnSendSuccess()) {
            LOG.trace("Message payload cleared on send success.");
            deletePayload(userMessage, userMessageLog);
            return;
        }
        LOG.trace("Message payload not cleared on send success");
    }

    public void deletePayloadOnSendFailure(UserMessage userMessage, UserMessageLog userMessageLog) {
        if (shouldDeletePayloadOnSendFailure(userMessage)) {
            LOG.trace("Message payload cleared on send failure.");
            deletePayload(userMessage, userMessageLog);
            return;
        }
        LOG.trace("Message payload not cleared on send failure");
    }

    protected void deletePayload(UserMessage userMessage, UserMessageLog userMessageLog) {
        partInfoService.clearPayloadData(userMessage.getEntityId());
        userMessageLog.setDeleted(new Date());
    }

    protected boolean shouldDeletePayloadOnSendSuccess() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
    }

    protected boolean shouldDeletePayloadOnSendFailure(UserMessage userMessage) {
        if (userMessage.isMessageFragment()) {
            return true;
        }
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
    }


    public void deleteMessages(List<UserMessageLogDto> userMessageLogs, int maxBatch) {
        if (CollectionUtils.isEmpty(userMessageLogs)) {
            LOG.debug(NO_MESSAGE_TO_BE_SCHEDULED_FOR_DELETION);
            return;
        }

        List<UserMessageLogDto> userMessageLogsToDelete = new ArrayList<>(userMessageLogs);

        while (CollectionUtils.isNotEmpty(userMessageLogsToDelete)) {
            LOG.debug("messageIds size is [{}]", userMessageLogsToDelete.size());
            int currentBatch = userMessageLogsToDelete.size();
            if (currentBatch > maxBatch) {
                LOG.debug("currentBatch [{}] is higher than maxBatch [{}]", currentBatch, maxBatch);
                currentBatch = maxBatch;
            }
            List<UserMessageLogDto> userMessageLogsBatch = userMessageLogsToDelete.stream().limit(currentBatch).collect(Collectors.toList());
            userMessageLogsToDelete.removeAll(userMessageLogsBatch);
            LOG.debug("After removal messageIds size is [{}]", userMessageLogsToDelete.size());
            userMessageDefaultService.deleteMessages(userMessageLogsBatch);
        }
    }

    protected Integer getRetentionValue(String propertyName) {
        return domibusPropertyProvider.getIntegerProperty(propertyName);
    }
}
