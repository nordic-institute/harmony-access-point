package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistence;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    private final EArchiveBatchDao eArchiveBatchDao;

    private final UserMessageLogDefaultService userMessageLogDefaultService;

    private final JmsUtil jmsUtil;

    private final EArchiveBatchUtils eArchiveBatchUtils;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DatabaseUtil databaseUtil,
            EArchiveBatchDao eArchiveBatchDao,
            UserMessageLogDefaultService userMessageLogDefaultService,
            JmsUtil jmsUtil,
            EArchiveBatchUtils eArchiveBatchUtils) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.databaseUtil = databaseUtil;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
        this.jmsUtil = jmsUtil;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
    }

    @Override
    @Timer(clazz = EArchiveListener.class, value = "process_1_batch_earchive")
    @Counter(clazz = EArchiveListener.class, value = "process_1_batch_earchive")
    public void onMessage(Message message) {
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        String batchId = jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
        Long entityId = jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        EArchiveBatchEntity eArchiveBatchByBatchId = getEArchiveBatch(entityId);
        try {
            eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.STARTED);

            List<UserMessageDTO> userMessageDtos = eArchiveBatchUtils.getUserMessageDtoFromJson(eArchiveBatchByBatchId).getUserMessageDtos();

            if (CollectionUtils.isEmpty(userMessageDtos)) {
                throw new DomibusEArchiveException("no messages present in the earchive batch [" + batchId + "]");
            }
            LOG.info("eArchiving for batchId [{}] starting userMessageLog from [{}] to [{}]",
                    batchId,
                    userMessageDtos.get(userMessageDtos.size() - 1),
                    userMessageDtos.get(0));

            exportInFileSystem(batchId, eArchiveBatchByBatchId, userMessageDtos);
            userMessageLogDefaultService.updateStatusToArchived(eArchiveBatchUtils.getEntityIds(userMessageDtos));
            eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
        } catch (Exception e) {
            LOG.error("EArchive Batch in error", e);
            eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.RETRIED);
            // TODO: François Gautier 21-10-21 update batch.json with description and error code
            throw e;
        }
    }

    private void exportInFileSystem(String batchId, EArchiveBatchEntity eArchiveBatchByBatchId, List<UserMessageDTO> userMessageDtos) {
        try (FileObject eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .requestType(eArchiveBatchByBatchId.getRequestType().name())
                        .status(eArchiveBatchByBatchId.geteArchiveBatchStatus().name())
                        .timestamp(DateTimeFormatter.ISO_DATE_TIME.format(eArchiveBatchByBatchId.getDateRequested().toInstant().atZone(ZoneOffset.UTC)))
                        .messageStartId(""+ userMessageDtos.get(userMessageDtos.size() - 1).getEntityId())
                        .messageEndId(""+ userMessageDtos.get(0))
                        .messages(eArchiveBatchUtils.getMessageIds(userMessageDtos))
                        .createBatchEArchiveDTO(),
                userMessageDtos)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Earchive saved in location [{}]", eArkSipStructure.getPath().toAbsolutePath().toString());
            }
        } catch (FileSystemException e) {
            throw new DomibusEArchiveException("EArchive failed to persists the batch [" + batchId + "]", e);
        }
    }

    private EArchiveBatchEntity getEArchiveBatch(long entityId) {
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }
        return eArchiveBatchByBatchId;
    }
}
