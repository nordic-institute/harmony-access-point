package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.earchive.FileSystemEArchivePersistence;
import eu.domibus.core.earchive.job.EArchiveBatch;
import eu.domibus.core.earchive.job.EArchiveBatchDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author François Gautier
 * @since 5.0
 */
@Component
public class EArchiveListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveListener.class);

    private final FileSystemEArchivePersistence fileSystemEArchivePersistence;

    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;

    private EArchiveBatchDao eArchiveBatchDao;

    private UserMessageLogDefaultService userMessageLogDefaultService;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DomainContextProvider domainContextProvider,
            DatabaseUtil databaseUtil,
            EArchiveBatchDao eArchiveBatchDao,
            UserMessageLogDefaultService userMessageLogDefaultService) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
        this.eArchiveBatchDao = eArchiveBatchDao;
        this.userMessageLogDefaultService = userMessageLogDefaultService;
    }

    @Override
    public void onMessage(Message message) {

        String batchId = getStringProperty(message, MessageConstants.BATCH_ID);
        long entityId = getLongProperty(message, MessageConstants.BATCH_ENTITY_ID);
        if (StringUtils.isBlank(batchId)) {
            LOG.error("Could not get the batchId");
            return;
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        LOG.info("eArchiving starting for batchId [{}]", batchId);

        EArchiveBatch eArchiveBatchByBatchId = geteArchiveBatch(entityId);

        List<UserMessageDTO> userMessageDtos = getUserMessageDtoFromJson(eArchiveBatchByBatchId).getUserMessageDtos();

        setDomain(message);

        LOG.info("eArchiving starting userMessageLog from [{}] to [{}]",
                userMessageDtos.get(userMessageDtos.size() - 1),
                userMessageDtos.get(0));

        fileSystemEArchivePersistence.createEArkSipStructure(
                new BatchEArchiveDTOBuilder()
                        .batchId(eArchiveBatchByBatchId.getBatchId())
                        .messages(getMessageIds(userMessageDtos))
                        .createBatchEArchiveDTO(),
                userMessageDtos);

        userMessageLogDefaultService.updateStatusToArchived(getEntityIds(userMessageDtos));
    }

    private EArchiveBatch geteArchiveBatch(long batchId) {
        EArchiveBatch eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);

        if (eArchiveBatchByBatchId == null) {
            throw new DomibusEArchiveException("EArchive batch not found for batchId: [" + batchId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    private List<String> getMessageIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getMessageId).collect(Collectors.toList());
    }
    private List<Long> getEntityIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getEntityId).collect(Collectors.toList());
    }

    private void setDomain(Message message) {
        String domainCode = getStringProperty(message, MessageConstants.DOMAIN);
        if (StringUtils.isNotEmpty(domainCode)) {
            domainContextProvider.setCurrentDomain(domainCode);
        } else {
            domainContextProvider.clearCurrentDomain();
        }
    }

    private ListUserMessageDto getUserMessageDtoFromJson(EArchiveBatch eArchiveBatchByBatchId) {
        return new Gson().fromJson(new String(eArchiveBatchByBatchId.getMessageIdsJson(), StandardCharsets.UTF_8), ListUserMessageDto.class);
    }

    private String getStringProperty(Message message, String variable) {
        String property;
        try {
            property = message.getStringProperty(variable);
        } catch (JMSException e) {
            LOG.debug("Could not get the [{}]", variable, e);
            property = null;
        }
        return property;
    }

    private Long getLongProperty(Message message, String variable) {
        Long property;
        try {
            property = Long.parseLong(message.getStringProperty(variable));
        } catch (NumberFormatException | JMSException e) {
            LOG.debug("Could not get the [{}]", variable, e);
            property = null;
        }
        return property;
    }


}
