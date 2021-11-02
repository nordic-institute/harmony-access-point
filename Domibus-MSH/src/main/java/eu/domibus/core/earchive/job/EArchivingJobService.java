package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.earchive.*;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final EArchiveBatchUtils eArchiveBatchUtils;


    public EArchivingJobService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                DomibusPropertyProvider domibusPropertyProvider,
                                PModeProvider pModeProvider,
                                EArchiveBatchDao eArchiveBatchDao,
                                EArchiveBatchStartDao eArchiveBatchStartDao,
                                NoArgGenerator uuidGenerator,
                                EArchiveBatchUtils eArchiveBatchUtils) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.eArchiveBatchStartDao = eArchiveBatchStartDao;
        this.uuidGenerator = uuidGenerator;
        this.eArchiveBatchUtils = eArchiveBatchUtils;
    }

    @Transactional(readOnly = true)
    public long getLastEntityIdArchived() {
        return eArchiveBatchStartDao.findByReference(EArchivingDefaultService.CONTINUOUS_ID).getLastPkUserMessage();
    }

    @Transactional
    public void updateLastEntityIdExported(Long lastPkUserMessage) {
        eArchiveBatchStartDao.findByReference(EArchivingDefaultService.CONTINUOUS_ID).setLastPkUserMessage(lastPkUserMessage);
    }

    @Transactional
    public EArchiveBatchEntity createEArchiveBatch(Long lastEntityIdProcessed, int batchSize, ListUserMessageDto userMessageToBeArchived) {
        EArchiveBatchEntity eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntityIdProcessed);

        eArchiveBatchUserMessageDao.create(eArchiveBatch, eArchiveBatchUtils.getEntityIds(userMessageToBeArchived.getUserMessageDtos()));
        return eArchiveBatch;
    }

    @Transactional
    public EArchiveBatchEntity reExportEArchiveBatch(String batchId) {
        EArchiveBatchEntity originEntity = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);
        if (originEntity == null) {
            throw new DomibusEArchiveException("EArchive batch not found batchId: [" + batchId + "]");
        }

        // save batch data for audit
        EArchiveBatchEntity reExportBatch = new EArchiveBatchEntity();
        reExportBatch.setBatchSize(originEntity.getBatchSize());
        reExportBatch.setRequestType(originEntity.getRequestType());
        reExportBatch.setMessageIdsJson(originEntity.getMessageIdsJson());
        reExportBatch.setFirstPkUserMessage(originEntity.getFirstPkUserMessage());
        reExportBatch.setLastPkUserMessage(originEntity.getLastPkUserMessage());
        reExportBatch.seteArchiveBatchStatus(EArchiveBatchStatus.REEXPORTED);
        reExportBatch.setDateRequested(originEntity.getDateRequested());
        // set updated storage location and new batch id
        reExportBatch.setStorageLocation(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION));
        reExportBatch.setBatchId(originEntity.getBatchId());
        // reuse the same entity to reduce the need for insert "UserMessage mappings to the "TB_EARCHIVEBATCH_UM"
        originEntity.setDateRequested(new Date());
        originEntity.seteArchiveBatchStatus(EArchiveBatchStatus.QUEUED);
        originEntity.setRequestType(RequestType.MANUAL);
        // update database data
        eArchiveBatchDao.create(reExportBatch);
        eArchiveBatchDao.merge(originEntity);

        return originEntity;
    }

    private EArchiveBatchEntity createEArchiveBatch(ListUserMessageDto userMessageToBeArchived, int batchSize, long lastEntity) {
        EArchiveBatchEntity entity = new EArchiveBatchEntity();
        entity.setBatchSize(batchSize);
        entity.setRequestType(RequestType.CONTINUOUS);
        entity.setStorageLocation(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION));
        entity.setBatchId(uuidGenerator.generate().toString());
        entity.setMessageIdsJson(eArchiveBatchUtils.getRawJson(userMessageToBeArchived));
        entity.setLastPkUserMessage(lastEntity);
        entity.seteArchiveBatchStatus(EArchiveBatchStatus.QUEUED);
        entity.setDateRequested(new Date());
        eArchiveBatchDao.create(entity);
        return entity;
    }

    public long getMaxEntityIdToArchived() {
        return Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(getRetryTimeOut())
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + MAX);
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
}
