package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.EArchiveBatchMapper;
import eu.domibus.core.earchive.job.EArchiveBatchDispatcherService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_REST_API_RETURN_MESSAGES;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingDefaultService implements DomibusEArchiveService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingDefaultService.class);

    public static final int CONTINUOUS_ID = 1;

    public static final int SANITY_ID = 2;

    private final EArchiveBatchDao eArchiveBatchDao;
    private final EArchiveBatchStartDao eArchiveBatchStartDao;

    // circular dependency on Wildfly use lazy initialization
    private final EArchiveBatchMapper eArchiveBatchMapper;
    private final EArchiveBatchDispatcherService eArchiveBatchDispatcherService;
    private final EArchiveBatchUtils eArchiveBatchUtils;
    private final DomainContextProvider domainContextProvider;
    private final DomibusPropertyProvider domibusPropertyProvider;
    private final UserMessageLogDefaultService userMessageLogDefaultService;

    private final JMSManager jmsManager;
    private final Queue eArchiveNotificationQueue;

    public EArchivingDefaultService(EArchiveBatchStartDao eArchiveBatchStartDao,
                                    @Lazy UserMessageLogDefaultService userMessageLogDefaultService,
                                    EArchiveBatchDao eArchiveBatchDao,
                                    JMSManager jmsManager,
                                    @Qualifier(InternalJMSConstants.EARCHIVE_NOTIFICATION_QUEUE) Queue eArchiveNotificationQueue,
                                    EArchiveBatchMapper eArchiveBatchMapper,
                                    EArchiveBatchDispatcherService eArchiveBatchDispatcherService,
                                    EArchiveBatchUtils eArchiveBatchUtils,
                                    DomainContextProvider domainContextProvider,
                                    DomibusPropertyProvider domibusPropertyProvider) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchMapper = eArchiveBatchMapper;
        this.eArchiveBatchDispatcherService = eArchiveBatchDispatcherService;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
        this.jmsManager = jmsManager;
        this.eArchiveNotificationQueue = eArchiveNotificationQueue;
    }

    @Override
    public void updateStartDateContinuousArchive(Long startMessageDate) {
        updateEArchiveBatchStart(CONTINUOUS_ID, startMessageDate);
    }

    @Override
    public void updateStartDateSanityArchive(Long startMessageDate) {
        updateEArchiveBatchStart(SANITY_ID, startMessageDate);
    }

    @Override
    public Long getStartDateContinuousArchive() {
        return eArchiveBatchUtils.extractDateFromPKUserMessageId(eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage());
    }

    @Override
    public Long getStartDateSanityArchive() {
        return eArchiveBatchUtils.extractDateFromPKUserMessageId(eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage());
    }

    private void updateEArchiveBatchStart(int sanityId, Long startMessageDate) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(sanityId);
        long lastPkUserMessage = eArchiveBatchUtils.dateToPKUserMessageId(startMessageDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("New start date archive [{}] batch lastPkUserMessage : [{}]", byReference.getDescription(), lastPkUserMessage);
        }
        byReference.setLastPkUserMessage(lastPkUserMessage);
        eArchiveBatchStartDao.update(byReference);
    }

    @Override
    public Long getBatchRequestListCount(EArchiveBatchFilter filter) {
        return eArchiveBatchDao.getBatchRequestListCount(filter);
    }

    @Override
    public List<EArchiveBatchRequestDTO> getBatchRequestList(EArchiveBatchFilter filter) {

        Boolean returnMessages = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_REST_API_RETURN_MESSAGES);
        Class<? extends EArchiveBatchBaseEntity> getResultProjections = returnMessages ? EArchiveBatchEntity.class : EArchiveBatchSummaryEntity.class;
        List<? extends EArchiveBatchBaseEntity> requestDTOList = eArchiveBatchDao.getBatchRequestList(filter, getResultProjections);
        return requestDTOList.stream().map(eArchiveBatchEntity ->
                eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(eArchiveBatchEntity)
        ).collect(Collectors.toList());
    }

    @Override
    public ListUserMessageDto getBatchUserMessageList(String batchId, Integer pageStart, Integer pageSize) {
        List<UserMessageDTO> list = eArchiveBatchDao.getBatchMessageList(batchId, pageStart, pageSize);
        return new ListUserMessageDto(list);
    }

    @Override
    public Long getBatchUserMessageListCount(String batchId) {
        EArchiveBatchEntity batch = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);
        if (batch == null) {
            throw new DomibusEArchiveException("EArchive batch not found batchId: [" + batchId + "]");
        }
        return batch.getBatchSize() != null ? batch.getBatchSize().longValue() : 0L;
    }

    @Override
    public ListUserMessageDto getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        List<UserMessageDTO> list = eArchiveBatchDao.getNotArchivedMessages(messageStartDate, messageEndDate, pageStart, pageSize);
        return new ListUserMessageDto(list);
    }

    @Override
    public Long getNotArchivedMessagesCount(Date messageStartDate, Date messageEndDate) {
        return eArchiveBatchDao.getNotArchivedMessageCountForPeriod(messageStartDate, messageEndDate);
    }

    @Override
    public EArchiveBatchRequestDTO reExportBatch(String batchId) {
        // create a copy of the  batch and submit it to JMS
        EArchiveBatchEntity copyBatch = eArchiveBatchDispatcherService.reExportBatchAndEnqueue(batchId, domainContextProvider.getCurrentDomain());
        return eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(copyBatch);
    }

    @Override
    @Transactional
    public EArchiveBatchRequestDTO setBatchClientStatus(String batchId, EArchiveBatchStatus batchStatus,String message) {
        LOG.debug("Got status notification with status: [{}] and message: [{}] for batchId: [{}]",batchStatus.name(), message,batchId);
        EArchiveBatchEntity eArchiveBatchEntity = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);
        if (eArchiveBatchEntity == null) {
            throw new DomibusEArchiveException("EArchive batch not found batchId: [" + batchId + "]");
        }
        DomibusMessageCode messageCode;
        if (batchStatus == EArchiveBatchStatus.ARCHIVED) {
            messageCode = DomibusMessageCode.BUS_ARCHIVE_BATCH_ARCHIVED_NOTIFICATION_RECEIVED;
        } else if (batchStatus == EArchiveBatchStatus.ARCHIVE_FAILED){
            messageCode = DomibusMessageCode.BUS_ARCHIVE_BATCH_ERROR_NOTIFICATION_RECEIVED;
        } else {
            throw new DomibusEArchiveException("Client submitted invalid batch status ["+batchStatus+"] for batchId: [" + batchId + "]. " +
                    "Only ARCHIVED and ARCHIVE_FAILED are allowed!");
        }
        EArchiveBatchEntity result = eArchiveBatchDao.setStatus(eArchiveBatchEntity, batchStatus, message, messageCode.getCode());
        LOG.businessInfo(messageCode, batchId, message );
        return eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(result);
    }

    public EArchiveBatchEntity getEArchiveBatch(long entityId) {
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchEntityId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, null, null);
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status, String error, String errorCode) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, error, errorCode);
    }

    public void sendToNotificationQueue(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus type) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatchByBatchId.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, "" + eArchiveBatchByBatchId.getEntityId())
                .property(MessageConstants.NOTIFICATION_TYPE, type.name())
                .build(), eArchiveNotificationQueue);
    }

    @Transactional
    public void executeBatchIsExported(EArchiveBatchEntity eArchiveBatchByBatchId, List<UserMessageDTO> userMessageDtos) {
        userMessageLogDefaultService.updateStatusToArchived(eArchiveBatchUtils.getEntityIds(userMessageDtos));
        setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_EXPORTED, eArchiveBatchByBatchId.getBatchId(),eArchiveBatchByBatchId.getStorageLocation() );
        sendToNotificationQueue(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
    }
}
