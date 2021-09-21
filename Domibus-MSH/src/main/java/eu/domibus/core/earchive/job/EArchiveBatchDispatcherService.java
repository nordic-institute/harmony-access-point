package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import com.google.gson.Gson;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
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

    public EArchiveBatchDispatcherService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                          EArchiveBatchDao eArchiveBatchDao,
                                          UserMessageLogDao userMessageLogDao,
                                          JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          NoArgGenerator uuidGenerator) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDao = userMessageLogDao;
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.uuidGenerator = uuidGenerator;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void startBatch() {
        LOG.debug("Start eArchive batch");
        ListUserMessageDto userMessageToBeArchived = null;
        long lastEntityId = 0;
        int batchSize = 100;

        while (userMessageToBeArchived == null || userMessageToBeArchived.getUserMessageDtos().size() < batchSize) {

            userMessageToBeArchived = userMessageLogDao.findMessagesForArchivingDesc(lastEntityId, batchSize);
            if(CollectionUtils.isEmpty(userMessageToBeArchived.getUserMessageDtos())){
                LOG.debug("no message to archive");
                return;
            }
            long lastEntity = userMessageToBeArchived.getUserMessageDtos().get(0).getEntityId();

            EArchiveBatch eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntity);

            lastEntityId = lastEntity;

            for (UserMessageDTO s : userMessageToBeArchived.getUserMessageDtos()) {
                eArchiveBatchUserMessageDao.create(eArchiveBatch, s.getEntityId());
            }

            enqueueEArchive(eArchiveBatch);
        }
        LOG.debug("Dispatch finished with last entityId [{}]", lastEntityId);
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
