package eu.domibus.common.dao;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */

@Repository
public class UserMessageDao extends BasicDaoNoGeneratePk<UserMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDao.class);

    @Autowired
    private MetricRegistry metricRegistry;

    public UserMessageDao() {
        super(UserMessage.class);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void clearPayloadData(final UserMessage userMessage) {
        LOG.debug("Start clearing payloadData");

        String messageId = userMessage.getMessageInfo().getMessageId();
        //add messageId to MDC map
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }

        if (userMessage.getPayloadInfo() == null || CollectionUtils.isEmpty(userMessage.getPayloadInfo().getPartInfo())) {
            LOG.debug("No payloads to clear");
            return;
        }
        clearDatabasePayloads(userMessage);
        clearFileSystemPayloads(userMessage);

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_DATA_CLEARED, messageId);
    }

    /**
     * Deletes the payloads saved on the file system
     *
     * @param userMessage
     */
    protected void clearFileSystemPayloads(final UserMessage userMessage) {
        List<PartInfo> fileSystemPayloads = getFileSystemPayloads(userMessage);
        if (CollectionUtils.isEmpty(fileSystemPayloads)) {
            LOG.debug("No file system payloads to clear");
            return;
        }

        for (PartInfo result : fileSystemPayloads) {
            try {
                Files.delete(Paths.get(result.getFileName()));
            } catch (IOException e) {
                LOG.debug("Problem deleting payload data files", e);
            }

        }
    }

    protected void clearDatabasePayloads(final UserMessage userMessage) {
        List<PartInfo> databasePayloads = getDatabasePayloads(userMessage);
        if (CollectionUtils.isEmpty(databasePayloads)) {
            LOG.debug("No database payloads to clear");
            return;
        }

        Predicate<PartInfo> filenameEmptyPredicate = getFilenameEmptyPredicate();
        userMessage.getPayloadInfo().getPartInfo().stream().filter(filenameEmptyPredicate).forEach(partInfo -> partInfo.setBinaryData(null));
        super.update(userMessage);
    }

    protected List<PartInfo> getPayloads(final UserMessage userMessage, Predicate<PartInfo> partInfoPredicate) {
        return userMessage.getPayloadInfo().getPartInfo().stream().filter(partInfoPredicate).collect(Collectors.toList());
    }

    protected Predicate<PartInfo> getFilenameEmptyPredicate() {
        return partInfo -> StringUtils.isBlank(partInfo.getFileName());
    }

    protected List<PartInfo> getDatabasePayloads(final UserMessage userMessage) {
        Predicate<PartInfo> filenameEmptyPredicate = getFilenameEmptyPredicate();
        return getPayloads(userMessage, filenameEmptyPredicate);
    }

    protected List<PartInfo> getFileSystemPayloads(final UserMessage userMessage) {
        Predicate<PartInfo> filenamePresentPredicate = getFilenameEmptyPredicate().negate();
        return getPayloads(userMessage, filenamePresentPredicate);
    }

}

