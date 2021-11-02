package eu.domibus.core.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.api.earchive.EArchiveBatchFilter;
import eu.domibus.api.earchive.EArchiveBatchRequestDTO;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.EArchiveBatchMapper;
import eu.domibus.core.earchive.job.EArchiveBatchDispatcherService;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.dateToPKUserMessageId;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.extractDateFromPKUserMessageId;
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
    private final EArchiveBatchMapper eArchiveBatchMapper;
    private final EArchiveBatchDispatcherService eArchiveBatchDispatcherService;
    private final DomainContextProvider domainContextProvider;
    private final DomibusPropertyProvider domibusPropertyProvider;
    private final UserMessageLogDefaultService userMessageLogDefaultService;

    private final JMSManager jmsManager;
    private final Queue eArchiveNotificationQueue;

    public EArchivingDefaultService(EArchiveBatchStartDao eArchiveBatchStartDao,
                                    UserMessageLogDefaultService userMessageLogDefaultService,
                                    EArchiveBatchDao eArchiveBatchDao,
                                    JMSManager jmsManager,
                                    @Qualifier(InternalJMSConstants.EARCHIVE_NOTIFICATION_QUEUE) Queue eArchiveNotificationQueue,
                                    EArchiveBatchMapper eArchiveBatchMapper,
                                    EArchiveBatchDispatcherService eArchiveBatchDispatcherService,
                                    DomainContextProvider domainContextProvider,
                                    DomibusPropertyProvider domibusPropertyProvider) {
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchMapper = eArchiveBatchMapper;
        this.eArchiveBatchDispatcherService = eArchiveBatchDispatcherService;
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
        this.jmsManager = jmsManager;
        this.eArchiveNotificationQueue = eArchiveNotificationQueue;
    }

    @Override
    public void updateStartDateContinuousArchive(Date startDate) {
        updateEArchiveBatchStart(CONTINUOUS_ID, startDate);
    }

    @Override
    public void updateStartDateSanityArchive(Date startDate) {
        updateEArchiveBatchStart(SANITY_ID, startDate);
    }

    @Override
    public Date getStartDateContinuousArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(CONTINUOUS_ID).getLastPkUserMessage() + "", 18, "0"), 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    @Override
    public Date getStartDateSanityArchive() {
        LocalDate parse = LocalDate.parse(StringUtils.substring(StringUtils.leftPad(eArchiveBatchStartDao.findByReference(SANITY_ID).getLastPkUserMessage() + "", 18, "0"), 0, 8), dtf);
        return Date.from(parse.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private void updateEArchiveBatchStart(int sanityId, Date startDate) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(sanityId);
        long lastPkUserMessage = Long.parseLong(ZonedDateTime.ofInstant(startDate.toInstant(), ZoneOffset.UTC).format(dtf) + MIN);
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
        Class<? extends EArchiveBatchBaseEntity> getResultProjections = returnMessages ? EArchiveBatchEntity.class : EArchiveBatchBaseEntity.EArchiveBatchSummaryEntity.class;
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
    public EArchiveBatchRequestDTO reExportBatch(String batchId) {
        // create a copy of the  batch and submit it to JMS
        EArchiveBatchEntity copyBatch = eArchiveBatchDispatcherService.createBatchCopyAndEnqueue(batchId, domainContextProvider.getCurrentDomain());
        return eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(copyBatch);
    }

    @Override
    public EArchiveBatchRequestDTO setBatchClientStatus(String batchId, EArchiveBatchStatus batchStatus) {
        EArchiveBatchEntity eArchiveBatchEntity = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);

        if (eArchiveBatchEntity == null) {
            throw new DomibusEArchiveException("EArchive batch not found batchId: [" + batchId + "]");
        }
        EArchiveBatchEntity result = eArchiveBatchDao.setStatus(eArchiveBatchEntity, batchStatus);
        return eArchiveBatchMapper.eArchiveBatchRequestEntityToDto(result);
    }

    public EArchiveBatchEntity getEArchiveBatch(long entityId) {
        EArchiveBatchEntity eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(entityId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + entityId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, null);
    }

    public void setStatus(EArchiveBatchEntity eArchiveBatchByBatchId, EArchiveBatchStatus status, String error) {
        eArchiveBatchDao.setStatus(eArchiveBatchByBatchId, status, error);
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
        userMessageLogDefaultService.updateStatusToArchived(getEntityIds(userMessageDtos));
        setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
        sendToNotificationQueue(eArchiveBatchByBatchId, EArchiveBatchStatus.EXPORTED);
    }


    private List<Long> getEntityIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }
}
