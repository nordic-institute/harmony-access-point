package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
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
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
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

    private final EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    private final EArchiveBatchStartDao eArchiveBatchStartDao;

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
                                    EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                    JMSManager jmsManager,
                                    @Qualifier(InternalJMSConstants.EARCHIVE_NOTIFICATION_QUEUE) Queue eArchiveNotificationQueue,
                                    EArchiveBatchMapper eArchiveBatchMapper,
                                    EArchiveBatchDispatcherService eArchiveBatchDispatcherService,
                                    EArchiveBatchUtils eArchiveBatchUtils,
                                    DomainContextProvider domainContextProvider,
                                    DomibusPropertyProvider domibusPropertyProvider) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
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
    @Transactional
    public List<EArchiveBatchRequestDTO> getBatchRequestList(EArchiveBatchFilter filter) {

        Boolean returnMessages = domibusPropertyProvider.getBooleanProperty(DOMIBUS_EARCHIVE_REST_API_RETURN_MESSAGES);
        List<EArchiveBatchEntity> requestDTOList = eArchiveBatchDao.getBatchRequestList(filter);
        if (BooleanUtils.isTrue(returnMessages)) {
            for (EArchiveBatchEntity eArchiveBatchEntity : requestDTOList) {
                eArchiveBatchEntity.seteArchiveBatchUserMessages(eArchiveBatchUserMessageDao.getBatchMessageList(eArchiveBatchEntity.getBatchId(), null, null));
            }
        }
        return requestDTOList.stream()
                .map(eArchiveBatchMapper::eArchiveBatchRequestEntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getBatchUserMessageList(String batchId, Integer pageStart, Integer pageSize) {
        return eArchiveBatchUtils.getMessageIds(eArchiveBatchUserMessageDao.getBatchMessageList(batchId, pageStart, pageSize));
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
    public List<String> getNotArchivedMessages(Date messageStartDate, Date messageEndDate, Integer pageStart, Integer pageSize) {
        return eArchiveBatchUtils.getMessageIds(eArchiveBatchDao.getNotArchivedMessages(messageStartDate, messageEndDate, pageStart, pageSize));
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
    public EArchiveBatchRequestDTO setBatchClientStatus(String batchId, @NotNull EArchiveBatchStatus batchStatus, String message) {
        LOG.debug("Got status notification with status: [{}] and message: [{}] for batchId: [{}]", batchStatus.name(), message, batchId);
        EArchiveBatchEntity eArchiveBatchEntity = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);
        if (eArchiveBatchEntity == null) {
            throw new DomibusEArchiveException("EArchive batch not found batchId: [" + batchId + "]");
        }
        DomibusMessageCode messageCode;
        if (batchStatus == EArchiveBatchStatus.ARCHIVED) {
            messageCode = DomibusMessageCode.BUS_ARCHIVE_BATCH_ARCHIVED_NOTIFICATION_RECEIVED;
            // submit jms message to update batch messages to "archived"
            eArchiveBatchDispatcherService.enqueueEArchive(eArchiveBatchEntity, domainContextProvider.getCurrentDomain(), EArchiveBatchStatus.ARCHIVED.name());
        } else if (batchStatus == EArchiveBatchStatus.ARCHIVE_FAILED) {
            messageCode = DomibusMessageCode.BUS_ARCHIVE_BATCH_ERROR_NOTIFICATION_RECEIVED;
        } else {
            throw new DomibusEArchiveException("Client submitted invalid batch status [" + batchStatus + "] for batchId: [" + batchId + "]. " +
                    "Only ARCHIVED and ARCHIVE_FAILED are allowed!");
        }

        EArchiveBatchEntity result = eArchiveBatchDao.setStatus(eArchiveBatchEntity, batchStatus, message, messageCode.getCode());
        LOG.businessInfo(messageCode, batchId, message);
        return eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(result);
    }

    @Transactional
    public EArchiveBatchEntity getEArchiveBatch(long entityId, boolean fetchEarchiveBatchUm) {
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchEntityId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }
        if (fetchEarchiveBatchUm) {
            eArchiveBatchByBatchId.seteArchiveBatchUserMessages(eArchiveBatchUserMessageDao.getBatchMessageList(eArchiveBatchByBatchId.getBatchId(), null, null));
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
                .property(MessageConstants.BATCH_ENTITY_ID, String.valueOf(eArchiveBatchByBatchId.getEntityId()))
                .property(MessageConstants.NOTIFICATION_TYPE, type.name())
                .build(), eArchiveNotificationQueue);
    }

    @Transactional
    public void executeBatchIsExported(EArchiveBatchEntity eArchiveBatchByBatchId) {
        setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_EXPORTED, eArchiveBatchByBatchId.getBatchId(), eArchiveBatchByBatchId.getStorageLocation());
        sendToNotificationQueue(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
    }

    @Transactional
    public void executeBatchIsArchived(EArchiveBatchEntity eArchiveBatchByBatchId, List<EArchiveBatchUserMessage> userMessageDtos) {
        userMessageLogDefaultService.updateStatusToArchived(eArchiveBatchUtils.getEntityIds(userMessageDtos));
        setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.ARCHIVED);
        LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_ARCHIVED,
                eArchiveBatchByBatchId.getBatchId(), eArchiveBatchByBatchId.getStorageLocation(),
                userMessageDtos.get(userMessageDtos.size() - 1), userMessageDtos.get(0));
    }
}