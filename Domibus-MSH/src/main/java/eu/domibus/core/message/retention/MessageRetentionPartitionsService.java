package eu.domibus.core.message.retention;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

    private static final String PARTITION_NAME_REGEXP = "P[0-9]{8}";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionPartitionsService.class);

    protected PModeProvider pModeProvider;

    protected UserMessageDao userMessageDao;

    protected UserMessageLogDao userMessageLogDao;

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected EventService eventService;

    protected DomibusConfigurationService domibusConfigurationService;

    protected DomainService domainService;

    protected DomainContextProvider domainContextProvider;

    public static final String DEFAULT_PARTITION_NAME = "P22000000"; // default partition that we never delete
    public static final String DATETIME_FORMAT_DEFAULT = "yyMMddHH";
    final protected SimpleDateFormat sdf;

    public MessageRetentionPartitionsService(PModeProvider pModeProvider,
                                          UserMessageDao userMessageDao,
                                             UserMessageLogDao userMessageLogDao,
                                             DomibusPropertyProvider domibusPropertyProvider,
                                             EventService eventService,
                                             DomibusConfigurationService domibusConfigurationService,
                                             DomainService domainService,
                                             DomainContextProvider domainContextProvider) {
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.userMessageLogDao = userMessageLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eventService = eventService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.domainService = domainService;
        this.domainContextProvider = domainContextProvider;

        sdf = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public boolean handlesDeletionStrategy(String retentionStrategy) {
        return DeletionStrategy.PARTITIONS == DeletionStrategy.valueOf(retentionStrategy);
    }

    @Override
    public void deleteAllMessages() {
        throw new DomibusCoreException("Deleting all messages not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Timer(clazz = MessageRetentionPartitionsService.class, value = "retention_deleteExpiredMessages")
    @Counter(clazz = MessageRetentionPartitionsService.class, value = "retention_deleteExpiredMessages")
    @Transactional(readOnly = true)
    public void deleteExpiredMessages() {
        LOG.debug("Using MessageRetentionPartitionsService to deleteExpiredMessages");
        // A partition may have messages with all statuses, received/sent on any MPC
        // We only consider for deletion those partitions older than the maximum retention over all the MPCs defined in the pMode
        int maxRetention = getMaxRetention();
        LOG.info("Max retention time configured in pMode is [{}] minutes", maxRetention);
        Date newestPartitionToCheckDate = DateUtils.addMinutes(new Date(), maxRetention * -1);;
        String newestPartitionName = getPartitionNameFromDate(newestPartitionToCheckDate);
        LOG.info("Verify if all messages expired for partitions older than [{}]", newestPartitionName);
        List<String> partitionNames = getExpiredPartitions(newestPartitionName);
        for (String partitionName : partitionNames) {
            LOG.info("Verify partition [{}]", partitionName);
            // To avoid SQL injection issues, check the partition name used in the next checks, inside native SQL queries
            if(!partitionName.matches(PARTITION_NAME_REGEXP)) {
                LOG.error("Partition [{}] has invalid name", partitionName);
                continue;
            }

            // Verify if all messages were archived
            boolean toDelete = verifyIfAllMessagesAreArchived(partitionName);
            if (toDelete == false) {
                LOG.info("Partition [{}] will not be deleted because not all messages are archived", partitionName);
                eventService.enqueuePartitionExpirationEvent(partitionName);
                continue;
            }

            // TODO We might consider that, if a message was archived it is already expired (in final status and older than the specified retention for its MPC) and skip the next verifications
            // Verify if all messages expired
            toDelete = verifyIfAllMessagesAreExpired(partitionName);
            if (toDelete == false) {
                LOG.info("Partition [{}] will not be deleted because there are still ongoing messages", partitionName);
                eventService.enqueuePartitionExpirationEvent(partitionName);
                continue;
            }

            LOG.info("Delete partition [{}]", partitionName);
            userMessageDao.dropPartition(partitionName);
        }
    }

    protected List<String> getExpiredPartitions(String newestPartitionName){
        List<String> partitionNames;
        if(domibusConfigurationService.isMultiTenantAware()) {
            Domain currentDomain = domainContextProvider.getCurrentDomain();
            partitionNames = userMessageDao.findAllPartitionsOlderThan(newestPartitionName, domainService.getDatabaseSchema(currentDomain));
        } else {
            partitionNames = userMessageDao.findAllPartitionsOlderThan(newestPartitionName);
        }
        LOG.info("Found [{}] partitions to verify expired messages: [{}]", partitionNames.size(), partitionNames);

        // remove default partition (the oldest partition) as we don't delete it
        partitionNames.remove(DEFAULT_PARTITION_NAME);

        return partitionNames;
    }

    protected boolean verifyIfAllMessagesAreArchived(String partitionName) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE)) {
            LOG.debug("Archiving messages is disabled.");
            return true;
        }
        LOG.info("Verify if all messages are archived on partition [{}]", partitionName);
        int count = userMessageLogDao.countUnarchivedMessagesOnPartition(partitionName);
        if (count != 0) {
            LOG.info("There are [{}] messages not archived on partition [{}]", count, partitionName);
            return false;
        }
        LOG.info("All messages are archived on partition [{}]", partitionName);
        return true;
    }

    protected boolean verifyIfAllMessagesAreExpired(String partitionName) {

        LOG.info("Verify if all messages expired on partition [{}]", partitionName);
        List<String> messageStatuses = MessageStatus.getFinalStatesForDroppingPartitionAsString();

        LOG.info("Counting ongoing messages for partition [{}]", partitionName);
        // check for messages that are not in final status
        int count = userMessageLogDao.countByMessageStatusOnPartition(messageStatuses, partitionName);
        if (count != 0) {
            LOG.info("There are still [{}] ongoing messages on partition [{}]", count, partitionName);
            return false;
        }
        LOG.info("There is no ongoing message on partition [{}]", partitionName);

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
        LOG.debug("Get partition name from date, PartitionDate [{}], partitionName [{}]", partitionDate, partitionName);
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
        LOG.debug("Retention value for MPC [{}] and messageStatus [{}] is [{}]", mpc, messageStatus, retention);
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

        LOG.info("Max retention for all MPCs and message statuses is [{}]", maxRetention);
        return maxRetention;
    }
}
