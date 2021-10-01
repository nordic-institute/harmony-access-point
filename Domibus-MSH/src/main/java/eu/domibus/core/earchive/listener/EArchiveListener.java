package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE;

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

    private DomibusPropertyProvider domibusPropertyProvider;

    private UserMessageLogDefaultService userMessageLogDefaultService;

    private JmsUtil jmsUtil;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DatabaseUtil databaseUtil,
            EArchiveBatchDao eArchiveBatchDao,
            UserMessageLogDefaultService userMessageLogDefaultService,
            JmsUtil jmsUtil,
            DomibusPropertyProvider domibusPropertyProvider) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.databaseUtil = databaseUtil;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
        this.jmsUtil = jmsUtil;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    @Override
    public void onMessage(Message message) {
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        String batchId = jmsUtil.getStringProperty(message, MessageConstants.BATCH_ID);
        Long entityId = jmsUtil.getLongProperty(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        LOG.info("eArchiving starting for batchId [{}]", batchId);

        EArchiveBatch eArchiveBatchByBatchId = geteArchiveBatch(entityId);

        List<UserMessageDTO> userMessageDtos = getUserMessageDtoFromJson(eArchiveBatchByBatchId).getUserMessageDtos();

        if (CollectionUtils.isEmpty(userMessageDtos)) {
            LOG.error("no messages present in the earchive batch [{}]", batchId);
            return;
        }
        LOG.info("eArchiving starting userMessageLog from [{}] to [{}]",
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        try (FileObject eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .messages(getMessageIds(userMessageDtos))
                        .createBatchEArchiveDTO(),
                userMessageDtos)) {

            LOG.info("Earchive saved in location [{}]", eArkSipStructure.getPath().toAbsolutePath().toString());
        } catch (FileSystemException e) {
            throw new DomibusEArchiveException("EArchive failed to persists the batch [" + batchId + "]", e);
        }
        Integer insertBatchSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE);

        userMessageLogDefaultService.updateStatusToArchived(getEntityIds(userMessageDtos), insertBatchSize);
    }

    private EArchiveBatch geteArchiveBatch(long entityId) {
        EArchiveBatch eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
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
