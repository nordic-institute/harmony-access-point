package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.eark.DomibusEARKSIPResult;
import eu.domibus.core.earchive.eark.FileSystemEArchivePersistence;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.util.JmsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
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
        Long entityId = jmsUtil.getLongPropertySafely(message, MessageConstants.BATCH_ENTITY_ID);
        LOG.putMDC(DomibusLogger.MDC_BATCH_ENTITY_ID, entityId + "");
        if (StringUtils.isBlank(batchId) || entityId == null) {
            LOG.error("Could not get the batchId [{}] and/or entityId [{}]", batchId, entityId);
            return;
        }
        jmsUtil.setDomain(message);

        EArchiveBatchEntity eArchiveBatchByBatchId = eArchivingDefaultService.getEArchiveBatch(entityId, true);
        List<EArchiveBatchUserMessage> userMessageDtos = eArchiveBatchByBatchId.geteArchiveBatchUserMessages();

        String batchMessageType = jmsUtil.getMessageTypeSafely(message);
        if (StringUtils.equals(EArchiveBatchStatus.ARCHIVED.name(), batchMessageType)) {
            onMessageArchiveBatch(batchId, eArchiveBatchByBatchId, userMessageDtos);
        } else if (StringUtils.equals(EArchiveBatchStatus.EXPORTED.name(), batchMessageType))  {
            onMessageExportBatch(batchId, eArchiveBatchByBatchId, userMessageDtos);
        } else {
            LOG.error("Invalid JMS message type [{}] of the batchId [{}] and/or entityId [{}]! The batch processing is ignored!",
                    batchMessageType, batchId, entityId);
            // If this happen then this is programming flow miss-failure. Validate all JMS submission. And if new message type is added
            // make sure to add also the processing of new message type
            throw new IllegalArgumentException( "Invalid JMS message type ["+batchMessageType+"] for the eArchive processing of the batchId ["+batchId+"]!");
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

        String manifestChecksum = exportInFileSystem(eArchiveBatchByBatchId, userMessageDtos);
        eArchiveBatchByBatchId.setManifestChecksum(manifestChecksum);
        eArchivingDefaultService.executeBatchIsExported(eArchiveBatchByBatchId);
    }

    protected void onMessageArchiveBatch(String batchId, EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> userMessageDtos) {
        LOG.debug("Set batchId [{}] archived starting userMessageLog from [{}] to [{}]",
                batchId,
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        eArchivingDefaultService.executeBatchIsArchived(eArchiveBatchByBatchId, userMessageDtos);
    }

    private String exportInFileSystem(EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> batchUserMessages) {
        DomibusEARKSIPResult eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .requestType(eArchiveBatchByBatchId.getRequestType() != null ? eArchiveBatchByBatchId.getRequestType().name() : null)
                        .status(eArchiveBatchByBatchId.getEArchiveBatchStatus() != null ? eArchiveBatchByBatchId.getEArchiveBatchStatus().name() : null)
                        .timestamp(DateTimeFormatter.ISO_DATE_TIME.format(eArchiveBatchByBatchId.getDateRequested().toInstant().atZone(ZoneOffset.UTC)))
                        .messageStartId("" + batchUserMessages.get(0).getUserMessageEntityId())
                        .messageEndId("" + batchUserMessages.get(batchUserMessages.size() - 1).getUserMessageEntityId())
                        .messages(eArchiveBatchUtils.getMessageIds(batchUserMessages))
                        .createBatchEArchiveDTO(),
                batchUserMessages);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Earchive saved in location [{}]", eArkSipStructure.getDirectory().toAbsolutePath().toString());
        }
        return eArkSipStructure.getManifestChecksum();
    }
}
