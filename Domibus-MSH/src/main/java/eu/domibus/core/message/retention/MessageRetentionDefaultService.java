package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.JsonUtil;
import eu.domibus.core.message.*;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.WarningUtil;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

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

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

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
    private UserMessageDefaultService userMessageDefaultService;

    @Autowired
    DomainTaskExecutor domainTaskExecutor;

    private List<DeleteUserMessagesDetails> deleteUserMessagesDetails = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionDefaultService.class, value = "retention_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionDefaultService.class, value = "retention_deleteExpiredMessages")
    public void deleteExpiredMessages() {
        if (storedProcedureEnabled()) {
            deleteUserMessagesDetails.clear();
        }
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        final Integer expiredSentMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
        final Integer expiredPayloadDeletedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_PAYLOAD_DELETED_MAX_DELETE);

        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit, expiredPayloadDeletedMessagesLimit);
        }

        Integer timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_TIMEOUT);

        if (!storedProcedureEnabled()) {
            return;
        }

        LOG.debug("Waiting for delete procedures to execute");
        deleteUserMessagesDetails.forEach(detail -> {
            try {
                detail.getDeleteExpiredFuture().get(timeout.longValue(), TimeUnit.SECONDS);
                LOG.debug("Delete procedure [{}] for mpc [{}] ended in [{}] millis", detail.getQueryName(), detail.getMpc(), System.currentTimeMillis() - detail.getStartTime());
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                LOG.warn(WarningUtil.warnOutput("Error in retention!"), e);
                LOG.warn("Error executing delete procedure [{}] for mpc [{}], time [{}] millis", detail.getQueryName(), detail.getMpc(), System.currentTimeMillis() - detail.getStartTime(), e);
                detail.getDeleteExpiredFuture().cancel(true);
            } catch (Exception ex) {
                LOG.error("Exception when canceling the job executing the delete stored procedure", ex);
            }
        });
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

    protected void deleteUserMessagesUsingStoredProcedure(Date startDate, String mpc, Integer maxCount, String queryName) {
        DeleteUserMessagesProcedureRunnable deleteUserMessagesProcedureRunnable = new DeleteUserMessagesProcedureRunnable(userMessageLogDao, startDate, mpc, maxCount, queryName);

        Future<?> future = domainTaskExecutor.submit(deleteUserMessagesProcedureRunnable, false);
        DeleteUserMessagesDetails detail = new DeleteUserMessagesDetails(future, queryName, mpc, System.currentTimeMillis());
        deleteUserMessagesDetails.add(detail);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        String fileLocation = domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
        // If messageRetentionDownloaded is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionDownloaded < 0 || (messageRetentionDownloaded == 0 && StringUtils.isEmpty(fileLocation))) {
            LOG.trace("Retention of downloaded messages is not active.");
            return;
        }

        if (storedProcedureEnabled()) {
            deleteUserMessagesUsingStoredProcedure(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1), mpc, expiredDownloadedMessagesLimit, "DeleteExpiredDownloadedMessages");
            return;
        }

        LOG.debug("Deleting expired downloaded messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit);
        List<UserMessageLogDto> downloadedMessages = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1),
                mpc, expiredDownloadedMessagesLimit);
        if (CollectionUtils.isEmpty(downloadedMessages)) {
            LOG.debug("There are no expired downloaded messages.");
            return;
        }
        final int deleted = downloadedMessages.size();
        LOG.debug("Found [{}] downloaded messages to delete", deleted);
        deleteMessages(downloadedMessages, mpc);
        LOG.debug("Deleted [{}] downloaded messages", deleted);
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer expiredNotDownloadedMessagesLimit) {
        final int messageRetentionNotDownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionNotDownloaded < 0) {// if -1 the messages will be kept indefinitely and if 0, although it makes no sense, is legal
            LOG.trace("Retention of not downloaded messages is not active.");
            return;
        }

        if (storedProcedureEnabled()) {
            deleteUserMessagesUsingStoredProcedure(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1), mpc, expiredNotDownloadedMessagesLimit, "DeleteExpiredNotDownloadedMessages");
            return;
        }

        LOG.debug("Deleting expired not-downloaded messages for MPC [{}] using expiredNotDownloadedMessagesLimit [{}]", mpc, expiredNotDownloadedMessagesLimit);
        final List<UserMessageLogDto> notDownloadedMessages = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1),
                mpc, expiredNotDownloadedMessagesLimit);
        if (CollectionUtils.isEmpty(notDownloadedMessages)) {
            LOG.debug("There are no expired not-downloaded messages.");
            return;
        }
        final int deleted = notDownloadedMessages.size();
        LOG.debug("Found [{}] not-downloaded messages to delete", deleted);
        deleteMessages(notDownloadedMessages, mpc);
        LOG.debug("Deleted [{}] not-downloaded messages", deleted);
    }

    protected void deleteExpiredSentMessages(String mpc, Integer expiredSentMessagesLimit) {
        final int messageRetentionSent = pModeProvider.getRetentionSentByMpcURI(mpc);

        if (messageRetentionSent < 0) { // if -1 the messages will be kept indefinitely
            LOG.trace("Retention of sent messages is not active.");
            return;
        }

        if (storedProcedureEnabled()) {
            deleteUserMessagesUsingStoredProcedure(DateUtils.addMinutes(new Date(), messageRetentionSent * -1), mpc, expiredSentMessagesLimit, "DeleteExpiredSentMessages");
            return;
        }

        LOG.debug("Deleting expired sent messages for MPC [{}] using expiredSentMessagesLimit [{}]", mpc, expiredSentMessagesLimit);
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        List<UserMessageLogDto> sentMessages = userMessageLogDao.getSentUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionSent * -1),
                mpc, expiredSentMessagesLimit, isDeleteMessageMetadata);

        if (CollectionUtils.isEmpty(sentMessages)) {
            LOG.debug("There are no expired sent messages.");
            return;
        }
        final int deleted = sentMessages.size();
        LOG.debug("Found [{}] sent messages to delete", deleted);
        deleteMessages(sentMessages, mpc);
        LOG.debug("Deleted [{}] sent messages", deleted);
    }

    protected void deleteExpiredPayloadDeletedMessages(String mpc, Integer expiredPayloadDeletedMessagesLimit) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        if (!isDeleteMessageMetadata || expiredPayloadDeletedMessagesLimit < 0) { // only delete of entire messages if delete metadata is true
            LOG.trace("Retention of payload deleted messages is not active.");
            return;
        }

        if (storedProcedureEnabled()) {
            deleteUserMessagesUsingStoredProcedure(new Date(), mpc, expiredPayloadDeletedMessagesLimit, "deleteExpiredPayloadDeletedMessages");
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
        deleteMessages(deletedMessages, mpc);
        LOG.debug("Deleted [{}] payload deleted messages", deleted);
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

    @Override
    public void scheduleDeleteMessagesByMessageLog(List<UserMessageLogDto> userMessageLogs) {
        List<String> messageIds = userMessageLogs.stream().map(userMessageLog ->
                userMessageLog.getMessageId()).collect(Collectors.toList());
        scheduleDeleteMessages(messageIds);
    }

    @Override
    public void scheduleDeleteMessages(List<String> messageIds) {
        if (CollectionUtils.isEmpty(messageIds)) {
            LOG.debug("No message to be scheduled for deletion");
            return;
        }

        LOG.debug("Scheduling delete messages [{}]", messageIds);
        messageIds.forEach(messageId -> {
            JmsMessage message = JMSMessageBuilder.create()
                    .property(DELETE_TYPE, MessageDeleteType.SINGLE.name())
                    .property(MessageConstants.MESSAGE_ID, messageId)
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
    public void deleteMessages(List<UserMessageLogDto> userMessageLogs, int maxBatch) {
        if (CollectionUtils.isEmpty(userMessageLogs)) {
            LOG.debug("No message to be deleted");
            return;
        }

        List<UserMessageLogDto> userMessageLogsToDelete = new ArrayList<>(userMessageLogs);

        while (userMessageLogsToDelete.size() > 0) {
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

    protected boolean storedProcedureEnabled() {

        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_RETENTION_WORKER_STORED_PROCEDURE_ENABLED)) {
            LOG.trace("Stored procedure disabled");
            return false;
        }

        LOG.debug("Stored procedure enabled");
        return true;
    }
}
