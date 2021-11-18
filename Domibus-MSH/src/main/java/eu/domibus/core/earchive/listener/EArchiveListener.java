package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistence;
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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class EArchiveListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveListener.class);

    private final FileSystemEArchivePersistence fileSystemEArchivePersistence;

    private final DatabaseUtil databaseUtil;

    private final EArchivingDefaultService eArchivingDefaultService;

    private final JmsUtil jmsUtil;

    private final EArchiveBatchUtils eArchiveBatchUtils;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DatabaseUtil databaseUtil,
            EArchiveBatchUtils eArchiveBatchUtils,
            EArchivingDefaultService eArchivingDefaultService,
            JmsUtil jmsUtil) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.databaseUtil = databaseUtil;
        this.eArchivingDefaultService = eArchivingDefaultService;
        this.jmsUtil = jmsUtil;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
    }

    @Override
    @Timer(clazz = EArchiveListener.class, value = "process_1_batch_earchive")
    @Counter(clazz = EArchiveListener.class, value = "process_1_batch_earchive")
    public void onMessage(Message message) {
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        String batchId = jmsUtil.getStringPropertySafely(message, MessageConstants.BATCH_ID);
        String statusTo = jmsUtil.getStringPropertySafely(message, MessageConstants.STATUS_TO);
        Long entityId = jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
        LOG.putMDC(DomibusLogger.MDC_BATCH_ENTITY_ID, entityId + "");
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        EArchiveBatchEntity eArchiveBatchByBatchId = eArchivingDefaultService.getEArchiveBatch(entityId, true);
        List<EArchiveBatchUserMessage> userMessageDtos = eArchiveBatchByBatchId.geteArchiveBatchUserMessages();
        // export if status-to is not set as "Archived"
        if (StringUtils.isBlank(statusTo) || !StringUtils.equals(statusTo, EArchiveBatchStatus.ARCHIVED.name())) {
            onMessageExportBatch(batchId, eArchiveBatchByBatchId, userMessageDtos);
        } else {
            onMessageArchiveBatch(batchId, eArchiveBatchByBatchId, userMessageDtos);
        }
    }

    protected void onMessageExportBatch(String batchId, EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> userMessageDtos) {
        eArchivingDefaultService.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.STARTED);
        if (CollectionUtils.isEmpty(userMessageDtos)) {
            throw new DomibusEArchiveException("no messages present in the earchive batch [" + batchId + "]");
        }
        LOG.info("eArchiving for batchId [{}] starting userMessageLog from [{}] to [{}]",
                batchId,
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        exportInFileSystem(batchId, eArchiveBatchByBatchId, userMessageDtos);
        eArchivingDefaultService.executeBatchIsExported(eArchiveBatchByBatchId);
    }

    protected void onMessageArchiveBatch(String batchId, EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> userMessageDtos) {
        LOG.debug("Set batchId [{}] archived starting userMessageLog from [{}] to [{}]",
                batchId,
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        eArchivingDefaultService.executeBatchIsArchived(eArchiveBatchByBatchId, userMessageDtos);
    }

    private void exportInFileSystem(String batchId, EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> batchUserMessages) {
        try (FileObject eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .requestType(eArchiveBatchByBatchId.getRequestType() != null ? eArchiveBatchByBatchId.getRequestType().name() : null)
                        .status(eArchiveBatchByBatchId.getEArchiveBatchStatus() != null ? eArchiveBatchByBatchId.getEArchiveBatchStatus().name() : null)
                        .timestamp(DateTimeFormatter.ISO_DATE_TIME.format(eArchiveBatchByBatchId.getDateRequested().toInstant().atZone(ZoneOffset.UTC)))
                        .messageStartId("" + batchUserMessages.get(0).getUserMessageEntityId())
                        .messageEndId("" + batchUserMessages.get(batchUserMessages.size() - 1).getUserMessageEntityId())
                        .messages(eArchiveBatchUtils.getMessageIds(batchUserMessages))
                        .createBatchEArchiveDTO(),
                batchUserMessages)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Earchive saved in location [{}]", eArkSipStructure.getPath().toAbsolutePath().toString());
            }
        } catch (FileSystemException e) {
            throw new DomibusEArchiveException("EArchive failed to persists the batch [" + batchId + "]", e);
        }
    }
}
