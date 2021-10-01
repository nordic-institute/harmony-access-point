package eu.domibus.core.earchive.job;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.EArchiveBatch;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_MAX;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_SIZE;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveBatchDispatcherService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchDispatcherService.class);

    private final JMSManager jmsManager;

    private final Queue eArchiveQueue;
    private final UserMessageLogDao userMessageLogDao;

    protected DomibusPropertyProvider domibusPropertyProvider;

    private EArchiveBatchService eArchiveBatchService;

    public EArchiveBatchDispatcherService(UserMessageLogDao userMessageLogDao,
                                          JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          EArchiveBatchService eArchiveBatchService) {
        this.userMessageLogDao = userMessageLogDao;
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.eArchiveBatchService = eArchiveBatchService;
    }

    @Timer(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    @Counter(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    public void startBatch(Domain domain) {
        LOG.debug("Start eArchive batch");
        Long lastEntityIdProcessed = eArchiveBatchService.getLastEntityIdArchived();

        long maxEntityIdToArchived = eArchiveBatchService.getMaxEntityIdToArchived();
        int batchSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE);
        int maxNumberOfBatchesCreated = getProperty(DOMIBUS_EARCHIVE_BATCH_MAX);

        for (int i = 0; i < maxNumberOfBatchesCreated; i++) {
            LOG.debug("Start creation batch number [{}]", i);
            lastEntityIdProcessed = createBatchAndEnqueue(lastEntityIdProcessed, batchSize, maxEntityIdToArchived, domain);
            if (lastEntityIdProcessed == null) {
                break;
            }
            LOG.debug("EArchive created with last entity [{}]", lastEntityIdProcessed);
        }
        LOG.debug("Dispatch eArchiving batches finished with last entityId [{}]", lastEntityIdProcessed);
    }


    /**
     * @return null if no messages found
     */
    private Long createBatchAndEnqueue(final Long lastEntityIdProcessed, int batchSize, long maxEntityIdToArchived, Domain domain) {
        long lastEntityIdTreated;
        ListUserMessageDto userMessageToBeArchived = userMessageLogDao.findMessagesForArchivingDesc(lastEntityIdProcessed, maxEntityIdToArchived, batchSize);
        if (CollectionUtils.isEmpty(userMessageToBeArchived.getUserMessageDtos())) {
            LOG.debug("No message to archive");
            return null;
        }
        lastEntityIdTreated = userMessageToBeArchived.getUserMessageDtos().get(0).getEntityId();

        EArchiveBatch eArchiveBatch = eArchiveBatchService.createEArchiveBatch(lastEntityIdTreated, batchSize, userMessageToBeArchived);

        enqueueEArchive(eArchiveBatch, domain);

        if (userMessageToBeArchived.getUserMessageDtos().size() < batchSize) {
            LOG.debug("Last batch created");
            return null;
        }
        return lastEntityIdTreated;
    }


    private int getProperty(String property) {
        Integer integerProperty = domibusPropertyProvider.getIntegerProperty(property);
        if (integerProperty == null) {
            throw new DomibusEArchiveException("Property [" + property + "] not found");
        }
        return integerProperty;
    }

    protected void enqueueEArchive(EArchiveBatch eArchiveBatch, Domain domain) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatch.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, "" + eArchiveBatch.getEntityId())
                .property(MessageConstants.DOMAIN, getDomainCode(domain))
                .build(), eArchiveQueue);
    }

    private String getDomainCode(Domain domain) {
        if (domain == null) {
            return "default";
        }
        return domain.getCode();
    }
}
