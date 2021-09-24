package eu.domibus.core.earchive.job;

import com.fasterxml.uuid.NoArgGenerator;
import com.google.gson.Gson;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.earchive.*;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.jms.spi.InternalJMSConstants;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.DATETIME_FORMAT_DEFAULT;
import static eu.domibus.api.model.DomibusDatePrefixedSequenceIdGeneratorGenerator.NUMBER_FORMAT_DEFAULT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveBatchDispatcherService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveBatchDispatcherService.class);

    private final JMSManager jmsManager;

    private final Queue eArchiveQueue;
    private final EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    private final EArchiveBatchDao eArchiveBatchDao;
    private final UserMessageLogDao userMessageLogDao;
    protected NoArgGenerator uuidGenerator;

    protected DomibusPropertyProvider domibusPropertyProvider;

    private PModeProvider pModeProvider;

    public EArchiveBatchDispatcherService(EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao,
                                          EArchiveBatchDao eArchiveBatchDao,
                                          UserMessageLogDao userMessageLogDao,
                                          JMSManager jmsManager,
                                          @Qualifier(InternalJMSConstants.EARCHIVE_QUEUE) Queue eArchiveQueue,
                                          NoArgGenerator uuidGenerator,
                                          DomibusPropertyProvider domibusPropertyProvider,
                                          PModeProvider pModeProvider) {
        this.eArchiveBatchUserMessageDao = eArchiveBatchUserMessageDao;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDao = userMessageLogDao;
        this.jmsManager = jmsManager;
        this.eArchiveQueue = eArchiveQueue;
        this.uuidGenerator = uuidGenerator;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.pModeProvider = pModeProvider;
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Timer(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    @Counter(clazz = EArchiveBatchDispatcherService.class, value = "earchive_createBatch")
    public void startBatch(Domain domain) {
        LOG.debug("Start eArchive batch");
        Long lastEntityIdProcessed = getLastEntityIdArchived();

        long maxEntityIdToArchived = Long.parseLong(ZonedDateTime
                .now(ZoneOffset.UTC)
                .minusMinutes(getRetryTimeOut())
                .format(ofPattern(DATETIME_FORMAT_DEFAULT, ENGLISH)) + format(NUMBER_FORMAT_DEFAULT, 0));
        int batchSize = getProperty(DOMIBUS_EARCHIVE_BATCH_SIZE);
        int nbrBatchMax = getProperty(DOMIBUS_EARCHIVE_BATCH_MAX);

        for (int i = 0; i < nbrBatchMax; i++) {
            LOG.debug("Start creation batch number [{}]", i);
            lastEntityIdProcessed = createBatchAndEnqueue(lastEntityIdProcessed, batchSize, maxEntityIdToArchived, domain);
            if (lastEntityIdProcessed == null) {
                break;
            }
            LOG.info("EArchive created with last entity [{}]", lastEntityIdProcessed);
        }
        LOG.debug("Dispatch eArchiving batches finished with last entityId [{}]", lastEntityIdProcessed);
    }

    /**
     * @return null if no messages found
     */
    private Long createBatchAndEnqueue(Long lastEntityIdProcessed, int batchSize, long maxEntityIdToArchived, Domain domain) {
        ListUserMessageDto userMessageToBeArchived = userMessageLogDao.findMessagesForArchivingDesc(lastEntityIdProcessed, maxEntityIdToArchived, batchSize);
        if (CollectionUtils.isEmpty(userMessageToBeArchived.getUserMessageDtos())) {
            LOG.debug("no message to archive");
            return null;
        }
        lastEntityIdProcessed = userMessageToBeArchived.getUserMessageDtos().get(0).getEntityId();

        EArchiveBatch eArchiveBatch = createEArchiveBatch(lastEntityIdProcessed, batchSize, userMessageToBeArchived);

        enqueueEArchive(eArchiveBatch, domain);

        if (userMessageToBeArchived.getUserMessageDtos().size() < batchSize) {
            LOG.debug("Last batch created");
            return null;
        }
        return lastEntityIdProcessed;
    }

    private EArchiveBatch createEArchiveBatch(Long lastEntityIdProcessed, int batchSize, ListUserMessageDto userMessageToBeArchived) {
        EArchiveBatch eArchiveBatch = createEArchiveBatch(userMessageToBeArchived, batchSize, lastEntityIdProcessed);

        for (UserMessageDTO s : userMessageToBeArchived.getUserMessageDtos()) {
            eArchiveBatchUserMessageDao.create(eArchiveBatch, s.getEntityId());
        }
        return eArchiveBatch;
    }

    protected long getRetryTimeOut() {
        // Check override with property Batch retry timeout of this Access point
        long retryTimeOut = domibusPropertyProvider.getLongProperty(DOMIBUS_EARCHIVE_BATCH_RETRY_TIMEOUT);
        if (retryTimeOut > 0) {
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

    private long getLastEntityIdArchived() {
        Long lastEntityIdArchived = eArchiveBatchDao.findLastEntityIdArchived();
        if (lastEntityIdArchived == null) {
            return 0;
        }
        return lastEntityIdArchived;
    }

    private int getProperty(String property) {
        Integer integerProperty = domibusPropertyProvider.getIntegerProperty(property);
        if (integerProperty == null) {
            throw new DomibusEArchiveException("Property [" + property + "] not found");
        }
        return integerProperty;
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

    public void enqueueEArchive(EArchiveBatch eArchiveBatch, Domain domain) {
        jmsManager.sendMessageToQueue(JMSMessageBuilder
                .create()
                .property(MessageConstants.BATCH_ID, eArchiveBatch.getBatchId())
                .property(MessageConstants.BATCH_ENTITY_ID, "" + eArchiveBatch.getEntityId())
                .property(MessageConstants.DOMAIN, getDomainCode(domain))
                .build(), eArchiveQueue);
    }

    private String getDomainCode(Domain domain) {
        if (domain == null) {
            return "default";
        }
        return domain.getCode();
    }
}
