package eu.domibus.core.earchive.listener;


import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchiveBatchStatus;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ErrorHandler;

/**
 * @author François Gautier
 * @since 5.0
 */

@Service("eArchiveErrorHandler")
public class EArchiveErrorHandler implements ErrorHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveErrorHandler.class);

    private final EArchivingDefaultService eArchivingDefaultService;


    public EArchiveErrorHandler(EArchivingDefaultService eArchivingDefaultService) {
        this.eArchivingDefaultService = eArchivingDefaultService;
    }

    @Override
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Transactional
    public void handleError(Throwable t) {

        long entityId = Long.parseLong(LOG.getMDC(DomibusLogger.MDC_BATCH_ENTITY_ID));
        LOG.warn("Handling dispatch error for batch entityId [{}] ", entityId, t);

        EArchiveBatchEntity eArchiveBatchByBatchId = eArchivingDefaultService.getEArchiveBatch(entityId);
        eArchivingDefaultService.setStatus(eArchiveBatchByBatchId, EArchiveBatchStatus.FAILED, t.getMessage());
        eArchivingDefaultService.sendToNotificationQueue(eArchiveBatchByBatchId, EArchiveBatchStatus.FAILED);

    }

}