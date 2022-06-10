package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.property.DomibusPropertyProvider;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVING_NOTIFICATION_DETAILS_ENABLED;

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

    private final DomibusPropertyProvider domibusPropertyProvider;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DatabaseUtil databaseUtil,
            EArchiveBatchUtils eArchiveBatchUtils,
            EArchivingDefaultService eArchivingDefaultService,
            JmsUtil jmsUtil,
            DomibusPropertyProvider domibusPropertyProvider) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.databaseUtil = databaseUtil;
        this.eArchivingDefaultService = eArchivingDefaultService;
        this.jmsUtil = jmsUtil;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
        this.domibusPropertyProvider = domibusPropertyProvider;
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
        Date messageStartDate = null;
        Date messageEndDate = null;
        DomibusEARKSIPResult eArkSipStructure;
        String batchId = eArchiveBatchByBatchId.getBatchId();

        final Boolean isNotificationWithStartAndEndDate = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVING_NOTIFICATION_DETAILS_ENABLED);
        LOG.debug("EArchive client with batch Id [{}] needs to receive notifications with message start date and end date: [{}]", batchId, isNotificationWithStartAndEndDate);

        Long firstUserMessageEntityId = eArchiveBatchUtils.getMessageStartDate(batchUserMessages, 0);
        Long lastUserMessageEntityId = eArchiveBatchUtils.getMessageStartDate(batchUserMessages, eArchiveBatchUtils.getLastIndex(batchUserMessages));
        if (BooleanUtils.isTrue(isNotificationWithStartAndEndDate)) {
            messageStartDate = eArchiveBatchUtils.getBatchMessageDate(firstUserMessageEntityId);
            messageEndDate = eArchiveBatchUtils.getBatchMessageDate(lastUserMessageEntityId);
        }
        LOG.debug("EArchive batch messageStartDate [{}] and messageEndDate [{}] for batchId [{}]", messageStartDate, messageEndDate, batchId);

        eArkSipStructure = fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(batchId)
                        .requestType(eArchiveBatchByBatchId.getRequestType() != null ? eArchiveBatchByBatchId.getRequestType().name() : null)
                        .status("SUCCESS")
                        .timestamp(DateTimeFormatter.ISO_DATE_TIME.format(eArchiveBatchByBatchId.getDateRequested().toInstant().atZone(ZoneOffset.UTC)))
                        .messageStartId(String.valueOf(firstUserMessageEntityId))
                        .messageEndId(String.valueOf(lastUserMessageEntityId))
                        .messages(eArchiveBatchUtils.getMessageIds(batchUserMessages))
                        .createBatchEArchiveDTO(),
                batchUserMessages, messageStartDate, messageEndDate);

        if (LOG.isDebugEnabled()) {
            LOG.debug("EArchive saved in location [{}]", eArkSipStructure.getDirectory().toAbsolutePath().toString());
        }
        return eArkSipStructure.getManifestChecksum();
    }
}
