package eu.domibus.core.earchive.job;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.EArchiveBatchEntity;
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

    public EArchiveBatchDispatcherService(JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          EArchivingJobService eArchivingJobService) {
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eArchivingJobService = eArchivingJobService;
    }

    @Timer(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    @Counter(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    public void startBatch(Domain domain, EArchiveRequestType eArchiveRequestType) {
         final String eArchiveActive = domibusPropertyProvider.getProperty(domain, DOMIBUS_EARCHIVE_ACTIVE);
        if (BooleanUtils.isNotTrue(BooleanUtils.toBooleanObject(eArchiveActive))) {
            LOG.debug("eArchiving is not enabled");
            return;
        }
        Long lastEntityIdProcessed = eArchivingJobService.getLastEntityIdArchived(eArchiveRequestType);
        Long newLastEntityIdProcessed = lastEntityIdProcessed;
        long maxEntityIdToArchived = eArchivingJobService.getMaxEntityIdToArchived(eArchiveRequestType);
        int batchSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE);
        int maxNumberOfBatchesCreated = getProperty(DOMIBUS_EARCHIVE_BATCH_MAX);
        LOG.trace("Start eArchive batch lastEntityIdProcessed [{}], " +
                        "maxEntityIdToArchived [{}], " +
                        "batchSize [{}], " +
                        "maxNumberOfBatchesCreated [{}]",
                lastEntityIdProcessed,
                maxEntityIdToArchived,
                batchSize,
                maxNumberOfBatchesCreated);

        for (int i = 0; i < maxNumberOfBatchesCreated; i++) {
            EArchiveBatchEntity batchAndEnqueue = createBatchAndEnqueue(newLastEntityIdProcessed, batchSize, maxEntityIdToArchived, domain, eArchiveRequestType);
            if (batchAndEnqueue == null) {
                break;
            }
            newLastEntityIdProcessed = batchAndEnqueue.getLastPkUserMessage();
            LOG.debug("EArchive created with last entity [{}]", lastEntityIdProcessed);
        }
        if(eArchiveRequestType == EArchiveRequestType.SANITIZER){
            eArchivingJobService.createEventOnNonFinalMessages(lastEntityIdProcessed, maxEntityIdToArchived);
        }
        if (batchCreated(lastEntityIdProcessed, newLastEntityIdProcessed)) {
            eArchivingJobService.updateLastEntityIdExported(newLastEntityIdProcessed, eArchiveRequestType);
            LOG.debug("Dispatch eArchiving batches finished with last entityId [{}]", lastEntityIdProcessed);
        }
    }

    private boolean batchCreated(Long lastEntityIdProcessed, Long newLastEntityIdProcessed) {
        return !Objects.equals(newLastEntityIdProcessed, lastEntityIdProcessed);
    }

    /**
     * Create a new batch and enqueue it
     */
    private EArchiveBatchEntity createBatchAndEnqueue(final Long lastEntityIdProcessed, int batchSize, long maxEntityIdToArchived, Domain domain, EArchiveRequestType requestType) {
        List<EArchiveBatchUserMessage> messagesForArchivingAsc = eArchivingJobService.findMessagesForArchivingAsc(lastEntityIdProcessed, maxEntityIdToArchived, batchSize);
        if (CollectionUtils.isEmpty(messagesForArchivingAsc)) {
            LOG.debug("No message to archive");
            return null;
        }
        long lastEntityIdTreated = messagesForArchivingAsc.get(messagesForArchivingAsc.size() - 1).getUserMessageEntityId();

        EArchiveBatchEntity eArchiveBatch = eArchivingJobService.createEArchiveBatchWithMessages(lastEntityIdTreated, batchSize, messagesForArchivingAsc, requestType);

        enqueueEArchive(eArchiveBatch, domain, EArchiveBatchStatus.EXPORTED.name());
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_CREATE, eArchiveBatch.getBatchId());
        return eArchiveBatch;
    }

    /**
     * updates the data for batchId  and send it to EArchive queue for reexport
     *
     * @param batchId the batch id
     * @return reexported batch entity
     */
    public EArchiveBatchEntity reExportBatchAndEnqueue(final String batchId, Domain domain) {
        LOG.debug("Re-Export [{}] the batch and submit it to queue!", batchId);
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
