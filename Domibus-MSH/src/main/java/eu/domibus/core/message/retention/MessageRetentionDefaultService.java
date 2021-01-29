package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.model.UserMessageLogDto;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static eu.domibus.messaging.MessageConstants.*;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 *
 * @author Cosmin Baciu, Ioana Dragusanu
 * @since 3.0
 */
@Service
public class MessageRetentionDefaultService implements MessageRetentionService {

    public static final String MESSAGE_LOGS = "MESSAGE_LOGS";
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
    @Qualifier("retentionMessageQueue")
    private Queue retentionMessageQueue;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private UserMessageServiceHelper userMessageServiceHelper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionDefaultService.class, value = "schedule_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionDefaultService.class, value = "schedule_deleteExpiredMessages")
    public void deleteExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        final Integer expiredSentMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
        final Integer expiredPayloadDeletedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_PAYLOAD_DELETED_MAX_DELETE);

        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit);
        }
    }

    @Override
    public void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit, Integer expiredSentMessagesLimit, Integer expiredPayloadDeletedMessagesLimit) {
        LOG.debug("Deleting expired messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]" +
                " and expiredNotDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit);
        deleteExpiredSentMessages(mpc, expiredSentMessagesLimit);
        deleteExpiredPayloadDeletedMessages(mpc, expiredPayloadDeletedMessagesLimit);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        LOG.debug("Deleting expired downloaded messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit);
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        String fileLocation = domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
        // If messageRetentionDownloaded is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionDownloaded > 0 || (StringUtils.isNotEmpty(fileLocation) && messageRetentionDownloaded >= 0)) {
            List<UserMessageLogDto> downloadedMessages = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1),
                    mpc, expiredDownloadedMessagesLimit);
            if (CollectionUtils.isEmpty(downloadedMessages)) {
                LOG.debug("There are no expired downloaded messages.");
                return;
            }
            final int deleted = downloadedMessages.size();
            LOG.debug("Found [{}] downloaded messages to delete", deleted);
            scheduleDeleteMessages(downloadedMessages, mpc);
            LOG.debug("Scheduled [{}] downloaded messages", deleted);
        }
    }

    protected void deleteExpiredPayloadDeletedMessages(String mpc, Integer expiredPayloadDeletedMessagesLimit) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        if (!isDeleteMessageMetadata) { // only schedule delete of entire messages if delete metadata is true
            return;
        }

        LOG.debug("Deleting expired payload deleted messages for MPC [{}] using expiredPayloadDeletedMessagesLimit [{}]", mpc, expiredPayloadDeletedMessagesLimit);
        final List<UserMessageLogDto> deletedMessages = userMessageLogDao.getDeletedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), 1), // give 1 minute for the previous state
                mpc, expiredPayloadDeletedMessagesLimit);
        if (CollectionUtils.isEmpty(deletedMessages)) {
            LOG.debug("There are no expired payload deleted messages.");
            return;
        }
        final int deleted = deletedMessages.size();
        LOG.debug("Found [{}] payload deleted messages to delete", deleted);
        scheduleDeleteMessages(deletedMessages, mpc);
        LOG.debug("Scheduled [{}] payload deleted messages", deleted);
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired not-downloaded messages for MPC [{}] using expiredNotDownloadedMessagesLimit [{}]", mpc, expiredNotDownloadedMessagesLimit);
        final int messageRetentionNotDownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionNotDownloaded > -1) { // if -1 the messages will be kept indefinitely and if 0, although it makes no sense, is legal
            final List<UserMessageLogDto> notDownloadedMessages = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1),
                    mpc, expiredNotDownloadedMessagesLimit);
            if (CollectionUtils.isEmpty(notDownloadedMessages)) {
                LOG.debug("There are no expired not-downloaded messages.");
                return;
            }
            final int deleted = notDownloadedMessages.size();
            LOG.debug("Found [{}] not-downloaded messages to delete", deleted);
            scheduleDeleteMessages(notDownloadedMessages, mpc);
            LOG.debug("Scheduled [{}] not-downloaded messages", deleted);
        }
    }

    protected void deleteExpiredSentMessages(String mpc, Integer expiredSentMessagesLimit) {
        LOG.debug("Deleting expired sent messages for MPC [{}] using expiredSentMessagesLimit [{}]", mpc, expiredSentMessagesLimit);
        final int messageRetentionSent = pModeProvider.getRetentionSentByMpcURI(mpc);

        if (messageRetentionSent > -1) { // if -1 the messages will be kept indefinitely
            LOG.trace("messageRetentionSent [{}]", messageRetentionSent);
            final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
            List<UserMessageLogDto> sentMessages = userMessageLogDao.getSentUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionSent * -1),
                    mpc, expiredSentMessagesLimit, isDeleteMessageMetadata);

            if (CollectionUtils.isEmpty(sentMessages)) {
                LOG.debug("There are no expired sent messages.");
                return;
            }
            final int deleted = sentMessages.size();
            LOG.debug("Found [{}] sent messages to delete", deleted);
            scheduleDeleteMessages(sentMessages, mpc);
            LOG.debug("Scheduled [{}] sent messages", deleted);
        }
    }

    public void scheduleDeleteMessages(List<UserMessageLogDto> userMessageLogs, String mpc) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        LOG.trace("isDeleteMessageMetadata [{}]", isDeleteMessageMetadata);
        if (isDeleteMessageMetadata) { // schedule delete in batch
            final int maxBatch = pModeProvider.getRetentionMaxBatchByMpcURI(mpc, domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE));
            LOG.debug("Schedule bulk delete messages, maxBatch [{}]", maxBatch);
            scheduleDeleteMessagesByMessageLog(userMessageLogs, maxBatch);
            return;
        }
        // schedule delete one by one
        LOG.debug("Schedule delete messages one by one");
        scheduleDeleteMessagesByMessageLog(userMessageLogs);
    }

    @Override
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
                    .property(FINAL_RECIPIENT, messageLogDto.getProperties().get(FINAL_RECIPIENT))
                    .property(ORIGINAL_SENDER, messageLogDto.getProperties().get(ORIGINAL_SENDER))
                    .build();
            jmsManager.sendMessageToQueue(message, retentionMessageQueue);
        });
    }

    @Override
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
                    .property(MESSAGE_ID, userMessage.getMessageInfo().getMessageId())
                    .property(FINAL_RECIPIENT, properties.get(FINAL_RECIPIENT))
                    .property(ORIGINAL_SENDER, properties.get(ORIGINAL_SENDER))
                    .build();
            jmsManager.sendMessageToQueue(message, retentionMessageQueue);
        });
    }

    @Override
    public void deletePayloadOnSendSuccess(UserMessage userMessage, UserMessageLog userMessageLog) {
        if (shouldDeletePayloadOnSendSuccess()) {
            LOG.trace("Message payload cleared on send success.");
            deletePayload(userMessage, userMessageLog);
            return;
        }
        LOG.trace("Message payload not cleared on send success");
    }

    @Override
    public void deletePayloadOnSendFailure(UserMessage userMessage, UserMessageLog userMessageLog) {
        if (shouldDeletePayloadOnSendFailure(userMessage)) {
            LOG.trace("Message payload cleared on send failure.");
            deletePayload(userMessage, userMessageLog);
            return;
        }
        LOG.trace("Message payload not cleared on send failure");
    }

    protected void deletePayload(UserMessage userMessage, UserMessageLog userMessageLog) {
        messagingDao.clearPayloadData(userMessage);
        userMessageLog.setDeleted(new Date());
    }

    protected boolean shouldDeletePayloadOnSendSuccess() {
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_SUCCESS_DELETE_PAYLOAD);
    }

    protected boolean shouldDeletePayloadOnSendFailure(UserMessage userMessage) {
        if (userMessage.isUserMessageFragment()) {
            return true;
        }
        return domibusPropertyProvider.getBooleanProperty(DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD);
    }

    @Override
    public void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogs, int maxBatch) {
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
            List<UserMessageLogDto> userMessageLogsBatch = userMessageLogsToDelete
                    .stream()
                    .limit(currentBatch)
                    .collect(Collectors.toList());
            userMessageLogsToDelete.removeAll(userMessageLogsBatch);

            LOG.debug("After removal messageIds size is [{}]", userMessageLogsToDelete.size());
            scheduleDeleteBatchMessages(userMessageLogsBatch);
        }
    }

    protected void scheduleDeleteBatchMessages(List<UserMessageLogDto> userMessageLogsBatch) {
        LOG.debug("Scheduling to delete [{}] messages", userMessageLogsBatch.size());

        JmsMessage message = JMSMessageBuilder.create()
                .property(DELETE_TYPE, MessageDeleteType.MULTI.name())
                .property(MESSAGE_LOGS, jsonUtil.listToJson(userMessageLogsBatch))
                .build();
        jmsManager.sendMessageToQueue(message, retentionMessageQueue);
    }


    protected Integer getRetentionValue(String propertyName) {
        return domibusPropertyProvider.getIntegerProperty(propertyName);
    }

}
