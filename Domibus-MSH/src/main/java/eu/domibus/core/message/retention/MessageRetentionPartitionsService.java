package eu.domibus.core.message.retention;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;

/**
 * This service class is responsible for the retention and clean up of Domibus messages.
 * This service uses the technique to drop an entire partitions after checking all messages are expired and archived (if required)
 *
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class MessageRetentionPartitionsService implements MessageRetentionService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionPartitionsService.class);

    protected PModeProvider pModeProvider;

    protected UserMessageDao userMessageDao;

    protected UserMessageLogDao userMessageLogDao;

    protected DomibusPropertyProvider domibusPropertyProvider;


    public static final String DEFAULT_PARTITION_NAME = "P21000000";
    public static final String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    final SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);

    public MessageRetentionPartitionsService(PModeProvider pModeProvider,
                                             UserMessageDao userMessageDao,
                                             UserMessageLogDao userMessageLogDao,
                                             DomibusPropertyProvider domibusPropertyProvider) {
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.userMessageLogDao = userMessageLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public boolean handlesDeletionStrategy(String retentionStrategy) {
        return DeletionStrategy.PARTITIONS == DeletionStrategy.valueOf(retentionStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionPartitionsService.class, value = "retention_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionPartitionsService.class, value = "retention_deleteExpiredMessages")
    public void deleteExpiredMessages() {
        LOG.debug("Using MessageRetentionPartitionsService to deleteExpiredMessages");
        int maxRetention = getMaxRetention();
        Date newestPartitionToCheckDate = DateUtils.addMinutes(new Date(), maxRetention * -1);
        String newestPartitionName = getPartitionNameFromDate(newestPartitionToCheckDate);
        LOG.debug("Find partitions older than [{}]", newestPartitionName);
        List<String> partitionNames = userMessageDao.findPotentialExpiredPartitions(newestPartitionName);

        LOG.info("Found [{}] partitions to verify expired messages: [{}]", partitionNames.size(), partitionNames);
        partitionNames.remove(DEFAULT_PARTITION_NAME);

        for (String partitionName : partitionNames) {
            LOG.info("Verify partition [{}]", partitionName);
            boolean toDelete = verifyIfAllMessagesAreArchived(partitionName);
            if (toDelete == false) {
                LOG.info("Partition [{}] will not be deleted", partitionName);
                continue;
            }

            toDelete = verifyIfAllMessagesAreExpired(partitionName);
            if (toDelete == false) {
                LOG.info("Partition [{}] will not be deleted", partitionName);
                continue;
            }

            userMessageDao.deletePartition(partitionName);
        }
    }

    protected boolean verifyIfAllMessagesAreArchived(String partitionName) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE)) {
            LOG.debug("Archiving messages is disabled.");
            return true;
        }
        LOG.info("Verifying if all messages are archived on partition [{}]", partitionName);
        int count = userMessageLogDao.countUnarchivedMessagesOnPartition(partitionName);
        if (count != 0) {
            LOG.debug("There are [{}] messages not archived on partition [{}]", count, partitionName);
            return false;
        }
        LOG.debug("All messages are archived on partition [{}]", partitionName);
        return true;
    }

    protected boolean verifyIfAllMessagesAreExpired(String partitionName) {

        LOG.info("Verifying if all messages expired on partition [{}]", partitionName);
        List<MessageStatus> messageStatuses = new ArrayList<>(
                Arrays.asList(MessageStatus.SEND_ENQUEUED,
                        MessageStatus.SEND_IN_PROGRESS,
                        MessageStatus.BEING_PULLED,
                        MessageStatus.READY_TO_PULL,
                        MessageStatus.WAITING_FOR_RECEIPT,
                        MessageStatus.WAITING_FOR_RETRY
                )
        );

        LOG.debug("Counting messages in progress for partition [{}]", partitionName);
        // check for messages that are not in final status
        int count = userMessageLogDao.countByMessageStatusOnPartition(messageStatuses, partitionName);
        if (count != 0) {
            LOG.info("There are [{}] in progress messages on partition [{}]", count, partitionName);
            return false;
        }
        LOG.info("There is no message in progress found on partition [{}]", partitionName);

        // check (not)expired messages based on retention values for each MPC
        final List<String> mpcs = pModeProvider.getMpcURIList();
        for (final String mpc : mpcs) {
            LOG.debug("Verify expired messages for mpc [{}] on partition [{}]", mpc, partitionName);
            boolean allMessagesExpired = checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.DOWNLOADED, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.ACKNOWLEDGED, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.SEND_FAILURE, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.RECEIVED, partitionName);

            if(!allMessagesExpired) {
                LOG.info("There are messages that did not yet expired for mpc [{}] on partition [{}]", mpc, partitionName);
                return false;
            }
            LOG.info("All messages expired for mpc [{}] on partition [{}].", mpc, partitionName);
        }

        LOG.info("All messages expired on partition [{}]", partitionName);
        return true;
    }

    protected boolean checkByMessageStatusAndMpcOnPartition(String mpc, MessageStatus messageStatus, String partitionName) {
        int retention = getRetention(mpc, messageStatus);
        int count = userMessageLogDao.getMessagesNewerThan(
                DateUtils.addMinutes(new Date(), retention * -1), mpc, messageStatus, partitionName);
        if (count != 0) {
            LOG.info("[{}] [{}] messages newer than retention [{}] on partition [{}]", messageStatus, count, retention, partitionName);
            return false;
        }
        LOG.info("All [{}] messages are older than retention [{}] on partition [{}]", messageStatus, retention, partitionName);
        return true;
    }

    protected String getPartitionNameFromDate(Date partitionDate) {
        String partitionName = 'P' + sdf.format(partitionDate).substring(0, 8);
        LOG.debug("Partition name [{}] created for partitionDate [{}]", partitionName, partitionDate);
        return partitionName;
    }

    protected int getRetention(String mpc, MessageStatus messageStatus) {
        int retention = -1;
        switch (messageStatus) {
            case DOWNLOADED:
                retention = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
                break;
            case RECEIVED:
                retention = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
                break;
            case ACKNOWLEDGED:
            case SEND_FAILURE:
                retention = pModeProvider.getRetentionSentByMpcURI(mpc);
                break;
        }
        LOG.debug("Got retention value [{}] for mpc [{}] and messageStatus [{}] is [{}]", retention, mpc, messageStatus);
        return retention;
    }

    protected int getMaxRetention() {
        final List<String> mpcs = pModeProvider.getMpcURIList();
        int maxRetention = -1;
        for (String mpc : mpcs) {
            int retention = pModeProvider.getRetentionDownloadedByMpcURI(mpc);
            LOG.trace("Retention downloaded [{}] for mpc [{}]", retention, mpc);
            if (maxRetention < retention) {
                maxRetention = retention;
            }

            retention = pModeProvider.getRetentionUndownloadedByMpcURI(mpc);
            LOG.trace("Retention undownloaded [{}] for mpc [{}]", retention, mpc);
            if (maxRetention < retention) {
                maxRetention = retention;
            }

            retention = pModeProvider.getRetentionSentByMpcURI(mpc);
            LOG.trace("Retention sent [{}] for mpc [{}]", retention, mpc);
            if (maxRetention < retention) {
                maxRetention = retention;
            }
        }

        LOG.debug("Got max retention [{}]", maxRetention);
        return maxRetention;
    }

}
