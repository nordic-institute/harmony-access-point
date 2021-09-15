package eu.domibus.core.earchive.listener;

import com.google.gson.Gson;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.UserMessageDTO;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.earchive.BatchEArchiveDTOBuilder;
import eu.domibus.core.earchive.FileSystemEArchivePersistence;
import eu.domibus.core.earchive.job.EArchiveBatch;
import eu.domibus.core.earchive.job.EArchiveBatchDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class EArchiveListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveListener.class);
    private final FileSystemEArchivePersistence fileSystemEArchivePersistence;
    private final DomainContextProvider domainContextProvider;

    private final DatabaseUtil databaseUtil;
    private EArchiveBatchDao eArchiveBatchDao;

    public EArchiveListener(
            FileSystemEArchivePersistence fileSystemEArchivePersistence,
            DomainContextProvider domainContextProvider,
            DatabaseUtil databaseUtil,
            EArchiveBatchDao eArchiveBatchDao) {
        this.fileSystemEArchivePersistence = fileSystemEArchivePersistence;
        this.domainContextProvider = domainContextProvider;
        this.databaseUtil = databaseUtil;
        this.eArchiveBatchDao = eArchiveBatchDao;
    }

    @JmsListener(containerFactory = "eArchiveJmsListenerContainerFactory", destination = "${domibus.jms.queue.earchive}")
    public void foo(final MapMessage message) {

        String batchId = getString(message, MessageConstants.BATCH_ID);
        if (StringUtils.isBlank(batchId)) {
            LOG.error("Could not get the batchId");
            return;
        }
        LOG.putMDC(DomibusLogger.MDC_USER, databaseUtil.getDatabaseUserName());

        LOG.info("eArchiving starting for batchId [{}]", batchId);

        EArchiveBatch eArchiveBatchByBatchId = geteArchiveBatch(batchId);

        List<UserMessageDTO> userMessageDtos = parse(eArchiveBatchByBatchId).getUserMessageDtos();

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
    }

    private EArchiveBatch geteArchiveBatch(String batchId) {
        EArchiveBatch eArchiveBatchByBatchId = eArchiveBatchDao.findEArchiveBatchByBatchId(batchId);

        if (eArchiveBatchByBatchId == null) {
            throw new IllegalStateException("EArchive batch not found for batchId: [" + batchId + "]");
        }
        return eArchiveBatchByBatchId;
    }

    private List<String> getMessageIds(List<UserMessageDTO> userMessageDtos) {
        return userMessageDtos.stream().map(UserMessageDTO::getMessageId).collect(Collectors.toList());
    }

    private void setDomain(MapMessage message) {
        String domainCode = getString(message, MessageConstants.DOMAIN);
        if (StringUtils.isNotEmpty(domainCode)) {
            domainContextProvider.setCurrentDomain(domainCode);
        } else {
            domainContextProvider.clearCurrentDomain();
        }
    }

    private ListUserMessageDto parse(EArchiveBatch eArchiveBatchByBatchId) {
        return new Gson().fromJson(new String(eArchiveBatchByBatchId.getMessageIdsJson(), StandardCharsets.UTF_8), ListUserMessageDto.class);
    }

    private String getString(MapMessage message, String variable) {
        String domainCode;
        try {
            domainCode = message.getStringProperty(variable);
        } catch (JMSException e) {
            LOG.debug("Could not get the [{}]", variable, e);
            domainCode = null;
        }
        return domainCode;
    }


}
