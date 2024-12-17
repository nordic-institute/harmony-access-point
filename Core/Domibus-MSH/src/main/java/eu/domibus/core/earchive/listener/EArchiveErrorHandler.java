package eu.domibus.core.earchive.listener;


import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.core.util.DomibusStringUtilImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class EArchiveErrorHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveErrorHandler.class);

    private final EArchivingDefaultService eArchivingDefaultService;

    public EArchiveErrorHandler(EArchivingDefaultService eArchivingDefaultService) {
        this.eArchivingDefaultService = eArchivingDefaultService;
    }

    @Transactional
    public void handleError(EArchiveException eArchiveException) {
        Long entityId = eArchiveException.getBatchEntityId();
        String batchId = eArchiveException.getBatchId();
        EArchiveBatchStatus batchStatus = eArchiveException.getBatchStatus();

        if (batchStatus == EArchiveBatchStatus.ARCHIVED) {
            // failure to handle ARCHIVED batch - just log the error :
            LOG.warn("Handling ARCHIVED batch [{}] with entityId [{}] failed. The eArchive structure may not have been cleaned up and/or the archived messages may not have been marked as 'archived'. ", batchId, entityId, eArchiveException);
        } else {
            // failure to handle EXPORTED batch :
            LOG.warn("Handling dispatch error for batch entityId [{}] with status [{}]", entityId, batchStatus, eArchiveException);

            EArchiveBatchEntity eArchiveBatchByBatchId = eArchivingDefaultService.getEArchiveBatch(entityId, false);
            LOG.debug("Changing status of batch [{}] with entityId [{}] from [{}] to [{}]", batchId, entityId, eArchiveBatchByBatchId.getEArchiveBatchStatus(), EArchiveBatchStatus.FAILED);
            eArchivingDefaultService.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.FAILED, StringUtils.substring(eArchiveException.getMessage(), 0, DomibusStringUtilImpl.DEFAULT_MAX_STRING_LENGTH - 1), DomibusMessageCode.BUS_ARCHIVE_BATCH_EXPORT_FAILED.getCode());
            LOG.businessInfo(DomibusMessageCode.BUS_ARCHIVE_BATCH_EXPORT_FAILED, eArchiveBatchByBatchId.getBatchId(), eArchiveException.getMessage());
            eArchivingDefaultService.sendToNotificationQueue(eArchiveBatchByBatchId, EArchiveBatchStatus.FAILED);
        }
    }

}