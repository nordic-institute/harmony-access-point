package eu.domibus.core.message.retention;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.DatabasePartition;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.api.util.DbSchemaUtil;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.EventProperties;
import eu.domibus.core.alerts.service.EventService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_ACTIVE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED;

/**
 * This service class is responsible for the retention and clean up of Domibus messages.
 * This service uses the technique to drop an entire partitions after checking all messages are expired and archived (if required)
 *
 * @author idragusa
 * @since 4.2.1
 */
@Service
public class MessageRetentionPartitionsService implements MessageRetentionService {

    protected static final String PARTITION_NAME_REGEXP = "SYS_P[0-9]+|P[0-9]+";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetentionPartitionsService.class);

    protected final PModeProvider pModeProvider;

    protected final UserMessageDao userMessageDao;

    protected final UserMessageLogDao userMessageLogDao;

    protected final DomibusPropertyProvider domibusPropertyProvider;

    protected final EventService eventService;

    protected final DomibusConfigurationService domibusConfigurationService;

    protected final DbSchemaUtil dbSchemaUtil;

    protected final DomainContextProvider domainContextProvider;

    protected final DateUtil dateUtil;

    protected final PartitionService partitionService;

    public static final String DEFAULT_PARTITION = "P1970"; // default partition that we never delete

    public MessageRetentionPartitionsService(PModeProvider pModeProvider,
                                             UserMessageDao userMessageDao,
                                             UserMessageLogDao userMessageLogDao,
                                             DomibusPropertyProvider domibusPropertyProvider,
                                             EventService eventService,
                                             DomibusConfigurationService domibusConfigurationService,
                                             DbSchemaUtil dbSchemaUtil,
                                             DomainContextProvider domainContextProvider, DateUtil dateUtil,
                                             PartitionService partitionService) {
        this.pModeProvider = pModeProvider;
        this.userMessageDao = userMessageDao;
        this.userMessageLogDao = userMessageLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eventService = eventService;
        this.domibusConfigurationService = domibusConfigurationService;
        this.dbSchemaUtil = dbSchemaUtil;
        this.domainContextProvider = domainContextProvider;
        this.dateUtil = dateUtil;
        this.partitionService = partitionService;
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
        LOG.debug("Max retention time configured in pMode is [{}] minutes", maxRetention);
        List<String> partitionNames = getExpiredPartitions(maxRetention);
        LOG.debug("Verify if all messages expired for partitions older than [{}] days", maxRetention/60/24);
        for (String partitionName : partitionNames) {
            LOG.debug("Verify partition [{}]", partitionName);
            // To avoid SQL injection issues, check the partition name used in the next checks, inside native SQL queries
            if (!partitionName.matches(PARTITION_NAME_REGEXP)) {
                LOG.error("Partition [{}] has invalid name", partitionName);
                continue;
            }

            // Verify if all messages were archived
            boolean toDelete = verifyIfAllMessagesAreArchived(partitionName);
            if (toDelete == false) {
                LOG.warn("Partition [{}] will not be deleted because not all messages are archived", partitionName);
                enqueuePartitionCheckEvent(partitionName);
                continue;
            }

            // TODO We might consider that, if a message was archived it is already expired (in final status and older than the specified retention for its MPC) and skip the next verifications
            // Verify if all messages expired
            toDelete = verifyIfAllMessagesAreExpired(partitionName);
            if (toDelete == false) {
                LOG.warn("Partition [{}] will not be deleted because there are still ongoing messages", partitionName);
                enqueuePartitionCheckEvent(partitionName);
                continue;
            }

            LOG.info("Delete partition [{}]", partitionName);
            userMessageDao.dropPartition(partitionName);
        }
    }

    protected void enqueuePartitionCheckEvent(String partitionName) {
        eventService.enqueueEvent(EventType.PARTITION_CHECK, partitionName, new EventProperties(partitionName));
    }

    protected List<String> getExpiredPartitions(int maxRetention) {
        List<DatabasePartition> partitions;
        if (domibusConfigurationService.isMultiTenantAware()) {
            Domain currentDomain = domainContextProvider.getCurrentDomain();
            partitions = userMessageDao.findAllPartitions(dbSchemaUtil.getDatabaseSchema(currentDomain));
        } else {
            partitions = userMessageDao.findAllPartitions();
        }
        LOG.debug("There are [{}] partitions.", partitions.size());

        Date newestPartitionToCheckDate = DateUtils.addMinutes(dateUtil.getUtcDate(), maxRetention * -1);
        LOG.debug("Date to check partitions expiration: [{}]", newestPartitionToCheckDate);
        Long maxHighValue = partitionService.getExpiredPartitionsHighValue(partitions, newestPartitionToCheckDate);
        List<String> partitionNames =
                partitions.stream()
                        .filter(p -> !StringUtils.equalsIgnoreCase(p.getPartitionName(), DEFAULT_PARTITION))
                        .filter(p -> p.getHighValue() < maxHighValue)
                        .map(DatabasePartition::getPartitionName)
                        .collect(Collectors.toList());
        LOG.debug("Found [{}] partitions to verify expired messages: [{}]", partitionNames.size());
        if(LOG.isDebugEnabled()) {
            LOG.debug("Expired Partitions are: ");
            partitionNames.stream().forEach(p->LOG.debug("["  + p + "] "));
        }

        return partitionNames;
    }

    protected boolean verifyIfAllMessagesAreArchived(String partitionName) {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_ACTIVE)) {
            LOG.debug("Archiving messages mechanism is disabled.");
            if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED)) {
                LOG.debug("The property [{}] is set to false, it is allowed to drop partitions containing unarchived messages.", DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED);
                return true;
            }
            LOG.debug("The property [{}] is set to true, partitions containing unarchived messages will not be dropped.", DOMIBUS_PARTITIONS_DROP_CHECK_MESSAGES_EARCHIVED);
        }
        LOG.debug("Verify if all successful messages are archived on partition [{}]", partitionName);
        int count = userMessageLogDao.countMessagesNotArchivedOnPartition(partitionName);
        if (count != 0) {
            LOG.debug("There are [{}] successful messages not archived on partition [{}]", count, partitionName);
            return false;
        }
        LOG.debug("All successful messages are archived on partition [{}]", partitionName);
        return true;
    }

    protected boolean verifyIfAllMessagesAreExpired(String partitionName) {

        LOG.debug("Verify if all messages expired on partition [{}]", partitionName);
        List<String> messageStatuses = MessageStatus.getFinalStatesForDroppingPartitionAsString();

        LOG.debug("Counting ongoing messages for partition [{}]", partitionName);
        // check for messages that are not in final status
        int count = userMessageLogDao.countByMessageStatusOnPartition(messageStatuses, partitionName);
        if (count != 0) {
            LOG.warn("There are still [{}] ongoing messages on partition [{}]", count, partitionName);
            return false;
        }
        LOG.debug("There is no ongoing message on partition [{}]", partitionName);

        // check (not)expired messages based on retention values for each MPC
        final List<String> mpcs = pModeProvider.getMpcURIList();
        for (final String mpc : mpcs) {
            LOG.debug("Verify expired messages for mpc [{}] on partition [{}]", mpc, partitionName);
            boolean allMessagesExpired = checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.DOWNLOADED, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.ACKNOWLEDGED, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.SEND_FAILURE, partitionName) &&
                    checkByMessageStatusAndMpcOnPartition(mpc, MessageStatus.RECEIVED, partitionName);

            if (!allMessagesExpired) {
                LOG.warn("There are messages that did not yet expired for mpc [{}] on partition [{}]", mpc, partitionName);
                return false;
            }
            LOG.debug("All messages expired for mpc [{}] on partition [{}].", mpc, partitionName);
        }

        LOG.debug("All messages expired on partition [{}]", partitionName);
        return true;
    }

    protected boolean checkByMessageStatusAndMpcOnPartition(String mpc, MessageStatus messageStatus, String partitionName) {
        int retention = getRetention(mpc, messageStatus);
        int count = userMessageLogDao.getMessagesNewerThan(
                DateUtils.addMinutes(new Date(), retention * -1), mpc, messageStatus, partitionName);
        if (count != 0) {
            LOG.warn("[{}] [{}] messages newer than retention [{}] on partition [{}]", messageStatus, count, retention, partitionName);
            return false;
        }
        LOG.debug("All [{}] messages are older than retention [{}] on partition [{}]", messageStatus, retention, partitionName);
        return true;
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

        LOG.debug("Max retention for all MPCs and message statuses is [{}]", maxRetention);
        return maxRetention;
    }
}
