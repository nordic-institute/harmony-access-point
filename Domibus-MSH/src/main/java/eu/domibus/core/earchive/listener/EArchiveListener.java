package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.FileSystemEArchivePersistence;
import eu.domibus.core.earchive.EArchiveBatch;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class EArchiveListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveListener.class);

    private final FileSystemEArchivePersistence fileSystemEArchivePersistence;

    private final DatabaseUtil databaseUtil;

    private EArchiveBatchDao eArchiveBatchDao;

    private UserMessageLogDefaultService userMessageLogDefaultService;
    private JmsUtil jmsUtil;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DatabaseUtil databaseUtil,
            EArchiveBatchDao eArchiveBatchDao,
            UserMessageLogDefaultService userMessageLogDefaultService,
            JmsUtil jmsUtil) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.databaseUtil = databaseUtil;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
        this.jmsUtil = jmsUtil;
    }

    @Override
    public void onMessage(Message message) {

        String batchId = jmsUtil.getStringProperty(message, MessageConstants.BATCH_ID);
        long entityId = jmsUtil.getLongProperty(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId)) {
            LOG.error("Could not get the batchId");
            return;
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        LOG.info("eArchiving starting for batchId [{}]", batchId);

        EArchiveBatch eArchiveBatchByBatchId = geteArchiveBatch(entityId);

        List<UserMessageDTO> userMessageDtos = getUserMessageDtoFromJson(eArchiveBatchByBatchId).getUserMessageDtos();

        jmsUtil.setDomain(message);

        LOG.info("eArchiving starting userMessageLog from [{}] to [{}]",
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .messages(getMessageIds(userMessageDtos))
                        .createBatchEArchiveDTO(),
                userMessageDtos);

        userMessageLogDefaultService.updateStatusToArchived(getEntityIds(userMessageDtos));
    }

    private EArchiveBatch geteArchiveBatch(long batchId) {
        EArchiveBatch eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + batchId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    private List<String> getMessageIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getMessageId).collect(Collectors.toList());
    }
    private List<Long> getEntityIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }

    private ListUserMessageDto getUserMessageDtoFromJson(EArchiveBatch eArchiveBatchByBatchId) {
        return new Gson().fromJson(new String(eArchiveBatchByBatchId.getMessageIdsJson(), StandardCharsets.UTF_8), ListUserMessageDto.class);
    }
}
