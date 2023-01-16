package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.earchive.EArchiveRequestType;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.payload.PartInfoService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.earchive.*;
import eu.domibus.core.earchive.alerts.EArchivingEventService;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.MAX;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchivingJobService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchivingJobService.class);
    private final EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final PModeProvider pModeProvider;

    private final EArchiveBatchDao eArchiveBatchDao;

    private final EArchiveBatchStartDao eArchiveBatchStartDao;

    private final NoArgGenerator uuidGenerator;

    private final UserMessageLogDao userMessageLogDao;

    private final EArchivingEventService eArchivingEventService;

    private final PartInfoService partInfoService;

    public EArchivingJobService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                DomibusPropertyProvider domibusPropertyProvider,
                                PModeProvider pModeProvider,
                                EArchiveBatchDao eArchiveBatchDao,
                                EArchiveBatchStartDao eArchiveBatchStartDao,
                                NoArgGenerator uuidGenerator,
                                UserMessageLogDao userMessageLogDao,
                                EArchivingEventService eArchivingEventService, PartInfoService partInfoService) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.uuidGenerator = uuidGenerator;
        this.userMessageLogDao = userMessageLogDao;
        this.eArchivingEventService = eArchivingEventService;
        this.partInfoService = partInfoService;
    }

    @Transactional(readOnly = true)
    public EArchiveBatchStart getContinuousStartDate(EArchiveRequestType eArchiveRequestType) {
        EArchiveBatchStart byReference = eArchiveBatchStartDao.findByReference(getEArchiveBatchStartId(eArchiveRequestType));
        Hibernate.initialize(byReference);
        return byReference;
    }

    @Transactional
    public void updateLastEntityIdExported(Long lastPkUserMessage, EArchiveRequestType eArchiveRequestType) {
        eArchiveBatchStartDao.findByReference(getEArchiveBatchStartId(eArchiveRequestType)).setLastPkUserMessage(lastPkUserMessage);
    }

    @Transactional
    public EArchiveBatchEntity createEArchiveBatchWithMessages(Long lastEntityIdProcessed, List<EArchiveBatchUserMessage> userMessageToBeArchived, EArchiveRequestType requestType) {
        return createEArchiveBatchWithMessages(null,
                (userMessageToBeArchived.isEmpty() ? null : userMessageToBeArchived.get(0).getUserMessageEntityId()),
                lastEntityIdProcessed, userMessageToBeArchived, requestType);
    }

    @Transactional
    public EArchiveBatchEntity createEArchiveBatchWithMessages(String originalBatchId, Long firstEntityIdProcessed, Long lastEntityIdProcessed, List<EArchiveBatchUserMessage> userMessageToBeArchived, EArchiveRequestType requestType) {
        EArchiveBatchEntity eArchiveBatch = createEArchiveBatch(originalBatchId, userMessageToBeArchived != null ? userMessageToBeArchived.size() : 0, firstEntityIdProcessed, lastEntityIdProcessed, requestType);
        if (CollectionUtils.isNotEmpty(userMessageToBeArchived)) {
            eArchiveBatchUserMessageDao.create(eArchiveBatch, userMessageToBeArchived);
        }
        return eArchiveBatch;
    }

    /**
     * Method creates a new (copy) batch from the batch for given batchId. Message ids
     * request date, storage location, and RequestType which is set as MANUAL.
     * The Original batch re-exported flag is set to true
     *
     * @param batchId
     * @return new batch entry
     */
    @Transactional
    public EArchiveBatchEntity reExportEArchiveBatch(String batchId) {
        EArchiveBatchEntity originEntity = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);
        if (originEntity == null) {
            throw new DomibusEArchiveException(DomibusCoreErrorCode.DOM_009, "eArchive batch not found batchId: [" + batchId + "]");
        }
        List<EArchiveBatchUserMessage> messages = eArchiveBatchUserMessageDao.getBatchMessageList(originEntity.getBatchId(), null, null);

        EArchiveBatchEntity reExportedBatch = createEArchiveBatchWithMessages(originEntity.getBatchId(),
                originEntity.getFirstPkUserMessage(),
                originEntity.getLastPkUserMessage(),
                messages,
                EArchiveRequestType.MANUAL);// rexported batch is set to manual
        // set original entity as re-exported
        originEntity.setReExported(Boolean.TRUE);
        return reExportedBatch;
    }

    protected int getEArchiveBatchStartId(EArchiveRequestType requestType) {
        if (requestType == EArchiveRequestType.CONTINUOUS) {
            return EArchivingDefaultService.CONTINUOUS_ID;
        }
        if (requestType == EArchiveRequestType.SANITIZER) {
            return EArchivingDefaultService.SANITY_ID;
        }
        throw new DomibusEArchiveException("BatchRequestType [" + requestType + "] doesn't have a startDate saved in database");
    }

    private EArchiveBatchEntity createEArchiveBatch(String originalBatchId, int batchSize, Long firstEntity, Long lastEntity, EArchiveRequestType requestType) {
        EArchiveBatchEntity entity = new EArchiveBatchEntity();
        entity.setOriginalBatchId(originalBatchId);
        entity.setBatchSize(batchSize);
        entity.setRequestType(requestType);
        entity.setStorageLocation(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION));
        entity.setBatchId(uuidGenerator.generate().toString());
        entity.setFirstPkUserMessage(firstEntity);
        entity.setLastPkUserMessage(lastEntity);
        entity.setEArchiveBatchStatus(EArchiveBatchStatus.QUEUED);
        entity.setDateRequested(new Date());
        return eArchiveBatchDao.merge(entity);
    }

    @Transactional(readOnly = true)
    public long getMaxEntityIdToArchived(EArchiveRequestType eArchiveRequestType) {
        if (eArchiveRequestType == EArchiveRequestType.SANITIZER) {
            return eArchiveBatchStartDao.findByReference(EArchivingDefaultService.CONTINUOUS_ID).getLastPkUserMessage();
        }
        return Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(rounding60min(getRetryTimeOut()))
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);
    }

    /**
     * The format of {@code DomibusDatePrefixedSequenceIdGeneratorGenerator#DATETIME_FORMAT_DEFAULT}
     * does not allow a precision with minutes.
     *
     * @param retryTimeOut in minutes
     * @return rounded per hour (60, 120, etc.)
     */
    protected long rounding60min(long retryTimeOut) {
        if (retryTimeOut % 60L == 0) {
            return retryTimeOut;
        }
        long hours = retryTimeOut / 60L;
        return (hours + 1) * 60;
    }

    protected long getRetryTimeOut() {
        // Check override with property Batch retry timeout of this Access point
        long retryTimeOut = domibusPropertyProvider.getLongProperty(DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT);
        if (retryTimeOut >= 0) {
            return retryTimeOut;
        }

        //If no override, check if there is a filter on the MPCs to use to find the retry time out in the Pmode
        List<String> mpcs = getMpcs();

        LegConfigurationPerMpc allLegConfigurations = pModeProvider.getAllLegConfigurations();
        if (allLegConfigurations.isEmpty()) {
            throw new DomibusEArchiveException("No leg found in the PMode");
        }
        return getMaxRetryTimeOutFiltered(mpcs, allLegConfigurations);
    }


    protected int getMaxRetryTimeOutFiltered(List<String> mpcs, LegConfigurationPerMpc legConfigurationPerMpc) {
        int maxRetryTimeOut = 0;
        for (Map.Entry<String, List<LegConfiguration>> legConfigPerMpcs : legConfigurationPerMpc.entrySet()) {
            LOG.debug("MPCs: [{}]", legConfigPerMpcs.getKey());
            if (CollectionUtils.isEmpty(mpcs) || mpcs.stream().anyMatch(s -> equalsIgnoreCase(legConfigPerMpcs.getKey(), s))) {
                maxRetryTimeOut = findNewMaxRetryTimeOut(maxRetryTimeOut, legConfigPerMpcs);
            }
        }
        return maxRetryTimeOut;
    }

    private int findNewMaxRetryTimeOut(int actualMaxRetryTimeOut, Map.Entry<String, List<LegConfiguration>> legConfigPerMpcs) {
        int maxRetryTimeOut = actualMaxRetryTimeOut;
        for (LegConfiguration legConfiguration : legConfigPerMpcs.getValue()) {
            int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
            if (actualMaxRetryTimeOut < retryTimeout) {
                maxRetryTimeOut = retryTimeout;
            }
        }
        return maxRetryTimeOut;
    }

    protected List<String> getMpcs() {
        String mpcs = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
        if (StringUtils.isBlank(mpcs)) {
            return new ArrayList<>();
        }
        return Arrays.stream(StringUtils.split(mpcs, ',')).map(StringUtils::trim).collect(toList());
    }

    public List<EArchiveBatchUserMessage> findMessagesForArchivingAsc(long lastUserMessageLogId, long maxEntityIdToArchived, int batchMaxSize, int batchPayloadMaxSize) {
        List<EArchiveBatchUserMessage> messagesForArchiving = userMessageLogDao.findMessagesForArchivingAsc(lastUserMessageLogId, maxEntityIdToArchived, batchMaxSize);
        if (batchPayloadMaxSize == 0) {
            LOG.trace("BatchPayloadMaxSize is 0 so no restrictions on payload size.");
            return messagesForArchiving;
        }
        List<EArchiveBatchUserMessage> results = new ArrayList<>();
        long totalPayloadSize = 0;
        for (EArchiveBatchUserMessage message : messagesForArchiving) {
            final long totalLength = partInfoService.findPartInfoTotalLength(message.getUserMessageEntityId());
            totalPayloadSize += totalLength;
            if (totalPayloadSize >= batchPayloadMaxSize) {
                LOG.info("Reached the eArchive maximum payload limit of [{}]; stopping at [{}] messages", batchPayloadMaxSize, results.size());
                break;
            }
            results.add(message);
        }
        return results;
    }

    public void createEventOnNonFinalMessages(Long lastEntityIdProcessed, Long maxEntityIdToArchived) {
        List<EArchiveBatchUserMessage> messagesNotFinalAsc = userMessageLogDao.findMessagesNotFinalAsc(lastEntityIdProcessed, maxEntityIdToArchived);

        for (EArchiveBatchUserMessage userMessageDto : messagesNotFinalAsc) {
            MessageStatus messageStatus = userMessageLogDao.getMessageStatus(userMessageDto.getUserMessageEntityId());
            LOG.debug("Message [{}] has status [{}]", userMessageDto.getMessageId(), messageStatus);
            eArchivingEventService.sendEventMessageNotFinal(userMessageDto.getMessageId(), messageStatus);
        }
    }

    public void createEventOnStartDateContinuousJobStopped(Date continuousLastUpdatedDate) {
        Integer property = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);

        if (property == null || continuousLastUpdatedDate == null) {
            if (property == null) {
                LOG.error("The configuration is incorrect: [{}] is undefined", DOMIBUS_EARCHIVE_START_DATE_STOPPED_ALLOWED_HOURS);
            } else {
                LOG.error("The configuration is incorrect: the continuous job start date is undefined");
            }
            eArchivingEventService.sendEventStartDateStopped();
            return;
        }

        ZonedDateTime continuousStartDateTime = ZonedDateTime.ofInstant(continuousLastUpdatedDate.toInstant(), ZoneOffset.UTC);
        ZonedDateTime allowedDateTime = ZonedDateTime.now(ZoneOffset.UTC).minusHours(property);

        if (allowedDateTime.isAfter(continuousStartDateTime)) {
            LOG.warn("Earchive continuous job StartDate has not been updated since [{}] which is before the allowed time window [{}]", continuousStartDateTime, allowedDateTime);
            eArchivingEventService.sendEventStartDateStopped();
        }
    }

}
