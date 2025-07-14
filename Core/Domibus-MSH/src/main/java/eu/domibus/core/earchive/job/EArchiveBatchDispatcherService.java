package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchStart;
import eu.domibus.core.earchive.EArchiveBatchUserMessage;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveBatchDispatcherService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchDispatcherService.class);

    private final JMSManager jmsManager;

    private final Queue eArchiveQueue;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final EArchivingJobService eArchivingJobService;
    private final DateUtil dateUtil;

    public EArchiveBatchDispatcherService(JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          EArchivingJobService eArchivingJobService, DateUtil dateUtil) {
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eArchivingJobService = eArchivingJobService;
        this.dateUtil = dateUtil;
    }

    @Timer(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    @Counter(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    public void startBatch(Domain domain, EArchiveRequestType eArchiveRequestType) {
        final String eArchiveActive = domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isNotTrue(BooleanUtils.toBooleanObject(eArchiveActive))) {
            LOG.debug("eArchiving is not enabled");
            return;
        }

        EArchiveBatchStart startDate = eArchivingJobService.getStartDate(eArchiveRequestType);
        Long lastEntityIdProcessed = startDate.getLastPkUserMessage();
        LOG.info("start eArchive batch for domain [{}] and of type [{}] startDate: [{}]", domain, eArchiveRequestType, dateUtil.getDateHour("" + lastEntityIdProcessed));
        Long newLastEntityIdProcessed = lastEntityIdProcessed;
        long maxEntityIdToArchived = eArchivingJobService.getMaxEntityIdToArchived(eArchiveRequestType, lastEntityIdProcessed);
        int batchMaxSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE);
        int batchPayloadMaxSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE_PAYLOAD) * 1024 * 1024;
        int maxNumberOfBatchesToCreate = getProperty(DOMIBUS_EARCHIVE_BATCH_MAX);
        LOG.trace("Start eArchive batch lastEntityIdProcessed [{}], " +
                        "maxEntityIdToArchived [{}], " +
                        "batchMaxSize [{}], " +
                        "batchPayloadMaxSize [{}], " +
                        "maxNumberOfBatchesToCreate [{}]",
                lastEntityIdProcessed,
                maxEntityIdToArchived,
                batchMaxSize,
                batchPayloadMaxSize,
                maxNumberOfBatchesToCreate);

        int numberOfBatchesCreated = 0;
        for (int i = 0; i < maxNumberOfBatchesToCreate; i++) {
            try {
                LOG.info("Preparing batch #[{}] for domain [{}] and type [{}]", i, domain, eArchiveRequestType);
                EArchiveBatchEntity batchAndEnqueue = createBatchAndEnqueue(newLastEntityIdProcessed, batchMaxSize, batchPayloadMaxSize, maxEntityIdToArchived, domain, eArchiveRequestType);
                if (batchAndEnqueue == null) {
                    break;
                }
                newLastEntityIdProcessed = batchAndEnqueue.getLastPkUserMessage();
                LOG.debug("eArchive batch [{}] created with last entity [{}]", batchAndEnqueue.getBatchId(), newLastEntityIdProcessed);
                numberOfBatchesCreated++;
            } catch (Exception ex) {
                LOG.error("Error while creating eArchive batch #[{}] for domain [{}] and type [{}]", i, domain, eArchiveRequestType, ex);
                throw ex;
            }
        }
        LOG.info("[{}] eArchiver created [{}] batches for domain [{}]; last message entity id was [{}] and now is [{}]", eArchiveRequestType, numberOfBatchesCreated, domain, lastEntityIdProcessed, newLastEntityIdProcessed);

        if (eArchiveRequestType == EArchiveRequestType.SANITIZER) {
            eArchivingJobService.createEventOnNonFinalMessages(lastEntityIdProcessed, newLastEntityIdProcessed);
            eArchivingJobService.createEventOnStartDateContinuousJobStopped(eArchivingJobService.getStartDate(EArchiveRequestType.CONTINUOUS).getModificationTime());
        }
        if (batchCreated(lastEntityIdProcessed, newLastEntityIdProcessed)) {
            eArchivingJobService.updateLastEntityIdExported(newLastEntityIdProcessed, eArchiveRequestType);
            LOG.debug("Dispatch eArchiving batches finished with last entityId [{}]", lastEntityIdProcessed);
        } else {
            if (BooleanUtils.isTrue(domibusPropertyProvider.getBooleanProperty(domain, DOMIBUS_EARCHIVE_EXPORT_EMPTY)) && eArchiveRequestType == EArchiveRequestType.CONTINUOUS) {
                // create empty batch!
                EArchiveBatchEntity eArchiveBatchWithoutMessages = createBatchAndEnqueue(lastEntityIdProcessed, domain, EArchiveRequestType.CONTINUOUS, new ArrayList<>());
                LOG.debug("eArchive [{}] created with no messages", eArchiveBatchWithoutMessages.getBatchId());
            }
        }
    }

    private boolean batchCreated(Long lastEntityIdProcessed, Long newLastEntityIdProcessed) {
        return !Objects.equals(newLastEntityIdProcessed, lastEntityIdProcessed);
    }

    /**
     * Create a new batch and enqueue it
     */
    private EArchiveBatchEntity createBatchAndEnqueue(final Long lastEntityIdProcessed, int batchMaxSize, int batchPayloadMaxSize, long maxEntityIdToArchived, Domain domain, EArchiveRequestType requestType) {
        List<EArchiveBatchUserMessage> messagesForArchivingAsc = eArchivingJobService.findMessagesForArchivingAsc(lastEntityIdProcessed, maxEntityIdToArchived, batchMaxSize, batchPayloadMaxSize);

        if (CollectionUtils.isEmpty(messagesForArchivingAsc)) {
            LOG.debug("No message to archive");
            return null;
        }
        long lastEntityIdTreated = messagesForArchivingAsc.get(messagesForArchivingAsc.size() - 1).getUserMessageEntityId();

        return createBatchAndEnqueue(lastEntityIdTreated, domain, requestType, messagesForArchivingAsc);
    }

    public EArchiveBatchEntity createBatchAndEnqueue(long lastEntityIdTreated, Domain domain, EArchiveRequestType requestType, List<EArchiveBatchUserMessage> messagesForArchivingAsc) {
        LOG.info("Creating eArchive batch with last entity id [{}] and [{}] messages", lastEntityIdTreated, CollectionUtils.size(messagesForArchivingAsc));
        EArchiveBatchEntity eArchiveBatch = eArchivingJobService.createEArchiveBatchWithMessages(lastEntityIdTreated, messagesForArchivingAsc, requestType);

        enqueueEArchive(eArchiveBatch, domain, EArchiveBatchStatus.EXPORTED.name());
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_CREATE, requestType, eArchiveBatch.getBatchId());
        return eArchiveBatch;
    }

    /**
     * updates the data for batchId  and send it to EArchive queue for reexport
     *
     * @param batchId the batch id
     * @return reexported batch entity
     */
    public EArchiveBatchEntity reExportBatchAndEnqueue(final String batchId, Domain domain) {
        LOG.info("Re-Export [{}] the batch and submit it to queue!", batchId);
        EArchiveBatchEntity eArchiveBatch = eArchivingJobService.reExportEArchiveBatch(batchId);
        enqueueEArchive(eArchiveBatch, domain, EArchiveBatchStatus.EXPORTED.name());
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_REEXPORT, batchId);
        return eArchiveBatch;
    }

    private int getProperty(String property) {
        Integer integerProperty = domibusPropertyProvider.getIntegerProperty(property);
        if (integerProperty == null) {
            throw new DomibusEArchiveException("Property [" + property + "] not found");
        }
        return integerProperty;
    }

    public void enqueueEArchive(EArchiveBatchEntity eArchiveBatch, Domain domain, String jmsType) {

        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatch.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, String.valueOf(eArchiveBatch.getEntityId()))
                .property(MessageConstants.DOMAIN, getDomainCode(domain))
                .type(jmsType)
                .build(), eArchiveQueue);
    }

    private String getDomainCode(Domain domain) {
        if (domain == null) {
            return "default";
        }
        return domain.getCode();
    }
}
