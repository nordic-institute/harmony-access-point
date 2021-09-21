package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import com.google.gson.Gson;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.earchive.*;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;

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
    private final EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    private final EArchiveBatchDao eArchiveBatchDao;
    private final UserMessageLogDao userMessageLogDao;
    protected NoArgGenerator uuidGenerator;

    protected DomibusPropertyProvider domibusPropertyProvider;

    public EArchiveBatchDispatcherService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                          EArchiveBatchDao eArchiveBatchDao,
                                          UserMessageLogDao userMessageLogDao,
                                          JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          NoArgGenerator uuidGenerator,
                                          DomibusPropertyProvider domibusPropertyProvider) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDao = userMessageLogDao;
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.uuidGenerator = uuidGenerator;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void startBatch() {
        LOG.debug("Start eArchive batch");
        ListUserMessageDto userMessageToBeArchived;
        long lastEntityId = getLastEntityIdArchived();

        int batchSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE);
        int nbrBatchMax = getProperty(DOMIBUS_EARCHIVE_BATCH_MAX);
        for (int i = 0; i < nbrBatchMax; i++) {

            userMessageToBeArchived = userMessageLogDao.findMessagesForArchivingDesc(lastEntityId, batchSize);
            if (CollectionUtils.isEmpty(userMessageToBeArchived.getUserMessageDtos())) {
                LOG.debug("no message to archive");
                break;
            }
            lastEntityId = userMessageToBeArchived.getUserMessageDtos().get(0).getEntityId();

            EArchiveBatch eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntityId);

            for (UserMessageDTO s : userMessageToBeArchived.getUserMessageDtos()) {
                eArchiveBatchUserMessageDao.create(eArchiveBatch, s.getEntityId());
            }

            enqueueEArchive(eArchiveBatch);

            if (userMessageToBeArchived.getUserMessageDtos().size() < batchSize) {
                LOG.debug("Last batch created");
                break;
            }
        }
        LOG.debug("Dispatch eArchiving batches finished with last entityId [{}]", lastEntityId);
    }

    private long getLastEntityIdArchived() {
        Long lastEntityIdArchived = eArchiveBatchDao.findLastEntityIdArchived();
        if(lastEntityIdArchived == null){
            return 0;
        }
        return lastEntityIdArchived;
    }

    private int getProperty(String domibusEarchiveBatchSize) {
        Integer integerProperty = domibusPropertyProvider.getIntegerProperty(domibusEarchiveBatchSize);
        if (integerProperty == null) {
            throw new DomibusEArchiveException("Property [" + domibusEarchiveBatchSize + "] not found");
        }
        return integerProperty;
    }

    private EArchiveBatch createEArchiveBatch(ListUserMessageDto userMessageToBeArchived, int batchSize, long lastEntity) {
        EArchiveBatch entity = new EArchiveBatch();
        entity.setSize(batchSize);
        entity.setEArchiveBatchStatus(EArchiveBatchStatus.STARTING);
        entity.setRequestType(RequestType.CONTINUOUS);
        entity.setStorageLocation("");
        entity.setBatchId(uuidGenerator.generate().toString());
        entity.setMessageIdsJson(new Gson().toJson(userMessageToBeArchived, ListUserMessageDto.class));
        entity.setLastPkUserMessage(lastEntity);
        eArchiveBatchDao.create(entity);
        return entity;
    }

    public void enqueueEArchive(EArchiveBatch eArchiveBatch) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatch.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, "" + eArchiveBatch.getEntityId())
                // TODO: François Gautier 15-09-21 handle domain correctly
                .property(MessageConstants.DOMAIN, "default")
                .build(), eArchiveQueue);
    }
}
