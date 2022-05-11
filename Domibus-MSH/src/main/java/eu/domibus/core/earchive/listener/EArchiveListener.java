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
    @Timer(clazz = EArchiveListener.class, value = "earchive_process_1_batch")
    @Counter(clazz = EArchiveListener.class, value = "earchive_process_1_batch")
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
            onMessageArchiveBatch(eArchiveBatchByBatchId, userMessageDtos);
        } else if (StringUtils.equals(EArchiveBatchStatus.EXPORTED.name(), batchMessageType))  {
            onMessageExportBatch(eArchiveBatchByBatchId, userMessageDtos);
        } else {
            LOG.error("Invalid JMS message type [{}] of the batchId [{}] and/or entityId [{}]! The batch processing is ignored!",
                    batchMessageType, batchId, entityId);
            // If this happen then this is programming flow miss-failure. Validate all JMS submission. And if new message type is added
            // make sure to add also the processing of new message type
            throw new IllegalArgumentException( "Invalid JMS message type ["+batchMessageType+"] for the eArchive processing of the batchId ["+batchId+"]!");
        }
    }

    public void onMessageExportBatch(EArchiveBatchEntity eArchiveBatch, List<EArchiveBatchUserMessage> userMessageDtos) {
        eArchivingDefaultService.setStatus(eArchiveBatch, EArchiveBatchStatus.STARTED);
        if (CollectionUtils.isEmpty(userMessageDtos)) {
            LOG.info("eArchiving for batchId [{}] has no messages", eArchiveBatch.getBatchId());
        } else {
            LOG.info("eArchiving for batchId [{}] starting userMessageLog from [{}] to [{}]",
                    eArchiveBatch.getBatchId(),
                    userMessageDtos.get(0).getMessageId(),
                    userMessageDtos.get(userMessageDtos.size() - 1).getMessageId());
        }
        String manifestChecksum = exportInFileSystem(eArchiveBatch, userMessageDtos);
        eArchiveBatch.setManifestChecksum(manifestChecksum);
        eArchivingDefaultService.executeBatchIsExported(eArchiveBatch, userMessageDtos);
    }

    protected void onMessageArchiveBatch(EArchiveBatchEntity eArchiveBatch, List<EArchiveBatchUserMessage> userMessageDtos) {
        LOG.debug("Set batchId [{}] archived starting userMessageLog from [{}] to [{}]",
                eArchiveBatch.getBatchId(),
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        eArchivingDefaultService.executeBatchIsArchived(eArchiveBatch, userMessageDtos);
    }

    private String exportInFileSystem(EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> batchUserMessages) {
        DomibusEARKSIPResult eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .requestType(eArchiveBatchByBatchId.getRequestType() != null ? eArchiveBatchByBatchId.getRequestType().name() : null)
                        .status("SUCCESS")
                        .timestamp(DateTimeFormatter.ISO_DATE_TIME.format(eArchiveBatchByBatchId.getDateRequested().toInstant().atZone(ZoneOffset.UTC)))
                        .messageStartId(getMessageStartDate(batchUserMessages, 0))
                        .messageEndId(getMessageStartDate(batchUserMessages, getLastIndex(batchUserMessages)))
                        .messages(eArchiveBatchUtils.getMessageIds(batchUserMessages))
                        .createBatchEArchiveDTO(),
                batchUserMessages);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Earchive saved in location [{}]", eArkSipStructure.getDirectory().toAbsolutePath().toString());
        }
        return eArkSipStructure.getManifestChecksum();
    }

    private int getLastIndex(List<EArchiveBatchUserMessage> batchUserMessages) {
        if(CollectionUtils.isEmpty(batchUserMessages)){
            return 0;
        }
        return batchUserMessages.size() - 1;
    }

    private String getMessageStartDate(List<EArchiveBatchUserMessage> batchUserMessages, int index) {
        if(CollectionUtils.isEmpty(batchUserMessages)){
            return null;
        }
        return "" + batchUserMessages.get(index).getUserMessageEntityId();
    }
}
