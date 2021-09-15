package eu.domibus.core.earchive.job;

import com.google.gson.Gson;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.util.UUID;

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

    public EArchiveBatchDispatcherService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                          EArchiveBatchDao eArchiveBatchDao,
                                          UserMessageLogDao userMessageLogDao,
                                          JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDao = userMessageLogDao;
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
    }

    void startBatch() {
        // TODO: François Gautier 15-09-21 initialized properly
        LOG.info("Start eArchive batch");
        ListUserMessageDto userMessageToBeArchived = null;
        long lastEntityId = 0;
        int batchSize = 100;

        while (userMessageToBeArchived == null || userMessageToBeArchived.getUserMessageDtos().size() < batchSize) {

            userMessageToBeArchived = userMessageLogDao.findMessagesForArchivingDesc(lastEntityId, batchSize);
            long lastEntity = userMessageToBeArchived.getUserMessageDtos().get(0).getEntityId();

            EArchiveBatch eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntity);

            lastEntityId = lastEntity;

            for (UserMessageDTO s : userMessageToBeArchived.getUserMessageDtos()) {
                eArchiveBatchUserMessageDao.create(eArchiveBatch, s.getEntityId());
            }

            enqueueEArchive(eArchiveBatch.getBatchId());
        }
        LOG.info("Dispatch finished with last entityId [{}]", lastEntityId);
    }

    private EArchiveBatch createEArchiveBatch(ListUserMessageDto userMessageToBeArchived, int batchSize, long lastEntity) {
        EArchiveBatch entity = new EArchiveBatch();
        entity.setSize(batchSize);
        entity.setEArchiveBatchStatus(EArchiveBatchStatus.STARTING);
        entity.setRequestType(RequestType.CONTINUOUS);
        entity.setStorageLocation("");
        entity.setBatchId(UUID.randomUUID().toString());
        entity.setMessageIdsJson(new Gson().toJson(userMessageToBeArchived, ListUserMessageDto.class));
        entity.setLastPkUserMessage(lastEntity);
        eArchiveBatchDao.create(entity);
        return entity;
    }

    public void enqueueEArchive(String batchId) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, batchId)
                // TODO: François Gautier 15-09-21 handle domain correctly
                .property(MessageConstants.DOMAIN, "default")
                .build(), eArchiveQueue);
    }
}
