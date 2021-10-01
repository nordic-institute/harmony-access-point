package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.earchive.*;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveBatchService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchService.class);
    public static final String MAX = "9999999999";

    private final EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final PModeProvider pModeProvider;

    private final EArchiveBatchDao eArchiveBatchDao;

    private final NoArgGenerator uuidGenerator;


    public EArchiveBatchService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao, DomibusPropertyProvider domibusPropertyProvider, PModeProvider pModeProvider, EArchiveBatchDao eArchiveBatchDao, NoArgGenerator uuidGenerator) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.uuidGenerator = uuidGenerator;
    }

    public long getLastEntityIdArchived() {
        Long lastEntityIdArchived = eArchiveBatchDao.findLastEntityIdArchived();
        if (lastEntityIdArchived == null) {
            return 0;
        }
        return lastEntityIdArchived;
    }

    public EArchiveBatch createEArchiveBatch(Long lastEntityIdProcessed, int batchSize, ListUserMessageDto userMessageToBeArchived) {
        EArchiveBatch eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntityIdProcessed);

        Integer longProperty = domibusPropertyProvider.getIntegerProperty(DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE);
        batches(userMessageToBeArchived, longProperty)
                .forEach(userMessageDTOS ->
                        eArchiveBatchUserMessageDao.create(eArchiveBatch, getEntityIds(userMessageDTOS.getUserMessageDtos())));
        return eArchiveBatch;
    }

    private List<Long> getEntityIds(List<UserMessageDTO> userMessageDTOS) {
        return userMessageDTOS.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }

    protected List<ListUserMessageDto> batches(ListUserMessageDto source, int length) {
        if (length <= 0) {
            throw new DomibusEArchiveException(DOMIBUS_EARCHIVE_BATCH_INSERT_BATCH_SIZE + " invalid");
        }
        if (source == null || CollectionUtils.isEmpty(source.getUserMessageDtos())) {
            return Collections.singletonList(new ListUserMessageDto(new ArrayList<>()));
        }
        int size = source.getUserMessageDtos().size();

        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                        n -> source.getUserMessageDtos().subList(n * length, n == fullChunks ? size : (n + 1) * length))
                .map(ListUserMessageDto::new)
                .collect(Collectors.toList());
    }

    private EArchiveBatch createEArchiveBatch(ListUserMessageDto userMessageToBeArchived, int batchSize, long lastEntity) {
        EArchiveBatch entity = new EArchiveBatch();
        entity.setSize(batchSize);
        entity.setEArchiveBatchStatus(EArchiveBatchStatus.STARTING);
        entity.setRequestType(RequestType.CONTINUOUS);
        entity.setStorageLocation(domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_STORAGE_LOCATION));
        entity.setBatchId(uuidGenerator.generate().toString());
        entity.setMessageIdsJson(new Gson().toJson(userMessageToBeArchived, ListUserMessageDto.class));
        entity.setLastPkUserMessage(lastEntity);
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

        Map<String, List<LegConfiguration>> allLegConfigurations = pModeProvider.getAllLegConfigurations();
        if (allLegConfigurations.isEmpty()) {
            throw new DomibusEArchiveException("No leg found in the PMode");
        }
        return getMaxRetryTimeOutFiltered(mpcs, allLegConfigurations);
    }


    private int getMaxRetryTimeOutFiltered(List<String> mpcs, Map<String, List<LegConfiguration>> allLegConfigurations) {
        int maxRetryTimeOut = 0;
        for (Map.Entry<String, List<LegConfiguration>> legConfigPerMpcs : allLegConfigurations.entrySet()) {
            LOG.debug("MPC: [{}]", legConfigPerMpcs.getKey());
            if (CollectionUtils.isEmpty(mpcs) || mpcs.stream().anyMatch(s -> equalsIgnoreCase(legConfigPerMpcs.getKey(), s))) {
                for (LegConfiguration legConfiguration : legConfigPerMpcs.getValue()) {
                    int retryTimeout = legConfiguration.getReceptionAwareness().getRetryTimeout();
                    if (maxRetryTimeOut < retryTimeout) {
                        maxRetryTimeOut = retryTimeout;
                    }
                }
            }
        }
        return maxRetryTimeOut;
    }

    private List<String> getMpcs() {
        String mpcs = domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
        if (StringUtils.isBlank(mpcs)) {
            return new ArrayList<>();
        }
        return Arrays.asList(StringUtils.split(mpcs, ','));
    }

}
