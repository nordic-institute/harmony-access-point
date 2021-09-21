package eu.domibus.core.message.retention;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLogDto;
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
        LOG.trace("Using MessageRetentionPartitionsService to deleteExpiredMessages");
        int maxRetention = getMaxRetention();
        Date newestPartitionToCheckDate = DateUtils.addMinutes(new Date(), maxRetention * -1);
        String newestPartitionName = getPartitionNameFromDate(newestPartitionToCheckDate);
        List<String> partitionNames = userMessageDao.findPotentialExpiredPartitions(newestPartitionName);

        partitionNames.remove(DEFAULT_PARTITION_NAME);
        LOG.debug("Expired partitions are [{}]", partitionNames);

        for (String pName : partitionNames) {
            boolean toDelete = true;
            toDelete = verifyIfAllMessagesAreArchived(pName);
            if (toDelete == false) {
                LOG.info("Partition [{}] has messages that are not archived", pName);
                continue;
            }

            toDelete = verifyIfAllMessagesAreExpired(pName);
            if (toDelete == false) {
                LOG.info("Partition [{}] has messages that are not expired", pName);
                continue;
            }

            userMessageDao.deletePartition(pName);
        }
    }

    protected boolean verifyIfAllMessagesAreArchived(String pName) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE)) {
            LOG.debug("Archiving messages is disabled.");
            return true;
        }
        int count = userMessageLogDao.countUnarchivedMessagesOnPartition(pName);
        if (count != 0) {
            LOG.debug("[{}] messages not archived on partition [{}]", count, pName);
            return false;
        }
        LOG.debug("All messages are archived on partition [{}]", pName);
        return true;
    }

    protected boolean verifyIfAllMessagesAreExpired(String pName) {

        List<MessageStatus> messageStatuses = new ArrayList<>(
                Arrays.asList(MessageStatus.SEND_ENQUEUED,
                        MessageStatus.SEND_IN_PROGRESS,
                        MessageStatus.BEING_PULLED,
                        MessageStatus.READY_TO_PULL,
                        MessageStatus.WAITING_FOR_RECEIPT,
                        MessageStatus.WAITING_FOR_RETRY
                )
        );

        // check for messages that are not in final status
        int count = userMessageLogDao.countByMessageStatusOnPartition(messageStatuses, pName);
        if (count != 0) {
            LOG.info("[{}] in progress messages on partition [{}]", count, pName);
            return false;
        }

        // check (not)expired messages based on retention values for each MPC
        final List<String> mpcs = pModeProvider.getMpcURIList();
        for (final String mpc : mpcs) {
            boolean allMessagesExpired = checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.DOWNLOADED, pName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.ACKNOWLEDGED, pName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.SEND_FAILURE, pName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.RECEIVED, pName);

            if(!allMessagesExpired) {
                LOG.info("Cannot delete partition [{}]", pName);
                return false;
            }
        }

        return true;
    }
    protected boolean checkByMessageStatusAndMpcOnPartition(String mpc, MessageStatus messageStatus, String pName) {
        int retention = getRetention(mpc, messageStatus);
        int count = userMessageLogDao.getMessagesNewerThan(
                DateUtils.addMinutes(new Date(), retention * -1), mpc, messageStatus, pName);
        if (count != 0) {
            LOG.info("[{}] [{}] messages newer than retention [{}] on partition [{}]", messageStatus, count, retention, pName);
            return false;
        }

        return true;
    }

    protected String getPartitionNameFromDate(Date partitionDate) {
        String pName = 'P' + sdf.format(partitionDate).substring(0, 8);
        LOG.debug("Partition name is [{}]", pName);
        return pName;
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
