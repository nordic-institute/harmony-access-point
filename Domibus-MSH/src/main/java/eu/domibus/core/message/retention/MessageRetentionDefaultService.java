package eu.domibus.core.message.retention;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * This service class is responsible for the retention and clean up of Domibus messages, including signal messages.
 * Notice that only payloads data are really deleted.
 *
 * @author Christian Koch, Stefan Mueller, Federico Martini, Cosmin Baciu
 * @since 3.0
 */
@Service
public class MessageRetentionDefaultService implements MessageRetentionService {

    public static final String MESSAGE_IDS = "MESSAGE_IDS";
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteExpiredMessages() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        final Integer expiredDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE);
        final Integer expiredNotDownloadedMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE);
        final Integer expiredSentMessagesLimit = getRetentionValue(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_SENT_MAX_DELETE);
        for (final String mpc : mpcs) {
            deleteExpiredMessages(mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit, expiredSentMessagesLimit);
        }
    }

    @Override
    public void deleteExpiredMessages(String mpc, Integer expiredDownloadedMessagesLimit, Integer expiredNotDownloadedMessagesLimit, Integer expiredSentMessagesLimit) {
        LOG.debug("Deleting expired messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]" +
                " and expiredNotDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit, expiredNotDownloadedMessagesLimit);
        deleteExpiredDownloadedMessages(mpc, expiredDownloadedMessagesLimit);
        deleteExpiredNotDownloadedMessages(mpc, expiredNotDownloadedMessagesLimit);
        deleteExpiredSentMessages(mpc, expiredSentMessagesLimit);
    }

    protected void deleteExpiredDownloadedMessages(String mpc, Integer expiredDownloadedMessagesLimit) {
        LOG.debug("Deleting expired downloaded messages for MPC [{}] using expiredDownloadedMessagesLimit [{}]", mpc, expiredDownloadedMessagesLimit);
        final int messageRetentionDownloaded = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
        String fileLocation = domibusPropertyProvider.getProperty(DOMIBUS_ATTACHMENT_STORAGE_LOCATION);
        // If messageRetentionDownloaded is equal to -1, the messages will be kept indefinitely and, if 0 and no file system storage was used, they have already been deleted during download operation.
        if (messageRetentionDownloaded > 0 || (StringUtils.isNotEmpty(fileLocation) && messageRetentionDownloaded >= 0)) {
            List<String> downloadedMessageIds = userMessageLogDao.getDownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionDownloaded * -1),
                    mpc, expiredDownloadedMessagesLimit);
            if (CollectionUtils.isEmpty(downloadedMessageIds)) {
                LOG.debug("There are no expired downloaded messages.");
                return;
            }
            final int deleted = downloadedMessageIds.size();
            LOG.debug("Found [{}] downloaded messages to delete", deleted);
            scheduleDeleteMessages(downloadedMessageIds, mpc);
            LOG.debug("Deleted [{}] downloaded messages", deleted);
        }
    }

    protected void deleteExpiredNotDownloadedMessages(String mpc, Integer expiredNotDownloadedMessagesLimit) {
        LOG.debug("Deleting expired not-downloaded messages for MPC [{}] using expiredNotDownloadedMessagesLimit [{}]", mpc, expiredNotDownloadedMessagesLimit);
        final int messageRetentionNotDownloaded = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
        if (messageRetentionNotDownloaded > -1) { // if -1 the messages will be kept indefinitely and if 0, although it makes no sense, is legal
            final List<String> notDownloadedMessageIds = userMessageLogDao.getUndownloadedUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionNotDownloaded * -1),
                    mpc, expiredNotDownloadedMessagesLimit);
            if (CollectionUtils.isEmpty(notDownloadedMessageIds)) {
                LOG.debug("There are no expired not-downloaded messages.");
                return;
            }
            final int deleted = notDownloadedMessageIds.size();
            LOG.debug("Found [{}] not-downloaded messages to delete", deleted);
            scheduleDeleteMessages(notDownloadedMessageIds, mpc);
            LOG.debug("Deleted [{}] not-downloaded messages", deleted);
        }
    }

    protected void deleteExpiredSentMessages(String mpc, Integer expiredSentMessagesLimit) {
        LOG.debug("Deleting expired sent messages for MPC [{}] using expiredSentMessagesLimit [{}]", mpc, expiredSentMessagesLimit);
        final int messageRetentionSent = pModeProvider.getRetentionSentByMpcURI(mpc);

        if (messageRetentionSent > -1) { // if -1 the messages will be kept indefinitely
            LOG.trace("messageRetentionSent [{}]", messageRetentionSent);
            final List<String> sentMessageIds = userMessageLogDao.getSentUserMessagesOlderThan(DateUtils.addMinutes(new Date(), messageRetentionSent * -1),
                    mpc, expiredSentMessagesLimit);
            if (CollectionUtils.isEmpty(sentMessageIds)) {
                LOG.debug("There are no expired sent messages.");
                return;
            }
            final int deleted = sentMessageIds.size();
            LOG.debug("Found [{}] sent messages to delete", deleted);
            scheduleDeleteMessages(sentMessageIds, mpc);
            LOG.debug("Deleted [{}] sent messages", deleted);
        }
    }

    public void scheduleDeleteMessages(List<String> messageIds, String mpc) {
        final boolean isDeleteMessageMetadata = pModeProvider.isDeleteMessageMetadataByMpcURI(mpc);
        LOG.trace("isDeleteMessageMetadata [{}]", isDeleteMessageMetadata);
        if (isDeleteMessageMetadata) { // schedule delete in batch
            final int maxBatch = pModeProvider.getRetentionMaxBatchByMpcURI(mpc, domibusPropertyProvider.getIntegerProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_BATCH_DELETE));
            LOG.debug("Schedule bulk delete messages, maxBatch [{}]", maxBatch);
            scheduleDeleteMessages(messageIds, maxBatch);
            return ;
        }
        // schedule delete one by one
        LOG.debug("Schedule delete messages one by one");
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
    public void scheduleDeleteMessages(List<String> messageIds, int maxBatch) {
        if (CollectionUtils.isEmpty(messageIds)) {
            LOG.debug("No message to be scheduled for deletion");
            return;
        }

        while (messageIds.size() > 0) {
            LOG.debug("messageIds size is [{}]", messageIds.size());
            LOG.trace("messageIds: [{}]", messageIds);
            int currentBatch = messageIds.size();
            if (currentBatch > maxBatch) {
                LOG.debug("currentBatch [{}] is higher than maxBatch [{}]", currentBatch, maxBatch);
                currentBatch = maxBatch;
            }
            List<String> messageIdsBatch = messageIds.stream().limit(currentBatch).collect(Collectors.toList());
            messageIds.removeAll(messageIdsBatch);
            LOG.debug("After removal messageIds size is [{}]", messageIds.size());
            LOG.trace("messageIds: [{}]", messageIds);
            final String separator = domibusPropertyProvider.getProperty(DOMIBUS_RETENTION_WORKER_MESSAGE_ID_LIST_SEPARATOR);
            scheduleDeleteBatchMessages(messageIdsBatch, separator);
        }
    }

    protected void scheduleDeleteBatchMessages(List<String> messageIdsBatch, final String separator) {
        LOG.debug("Scheduling to delete [{}] messages", messageIdsBatch.size());
        LOG.trace("MessageIdsBatch: [{}]", messageIdsBatch);
        List<String> invalidMessageIds = messageIdsBatch.stream().filter(id -> id.contains(separator)).collect(Collectors.toList());
        if (invalidMessageIds.size() > 0) {
            LOG.warn("Separator [{}] found in the messageId. Deletion for following messages cannot be performed in batch.");
            messageIdsBatch.removeAll(invalidMessageIds);
            invalidMessageIds.stream().forEach(messageId-> scheduleDeleteMessages(Arrays.asList(messageId)));
        }

        String messageIdsBatchStr = String.join(separator, messageIdsBatch);
        LOG.trace("Separator is [{}], messageIdsString [{}]", messageIdsBatchStr);
        JmsMessage message = JMSMessageBuilder.create()
                .property(DELETE_TYPE, MessageDeleteType.MULTI.name())
                .property(MESSAGE_IDS, messageIdsBatchStr)
                .build();
        jmsManager.sendMessageToQueue(message, retentionMessageQueue);
    }


    protected Integer getRetentionValue(String propertyName) {
        return domibusPropertyProvider.getIntegerProperty(propertyName);
    }

}
