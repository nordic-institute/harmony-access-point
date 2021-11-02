package eu.domibus.core.earchive.listener;

import eu.domibus.api.earchive.EArchiveBatchStatus;
import eu.domibus.core.earchive.EArchiveBatchEntity;
import eu.domibus.core.earchive.EArchivingDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class EArchiveErrorHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(EArchiveErrorHandlerTest.class);

    @Tested
    private EArchiveErrorHandler eArchiveErrorHandler;

    @Injectable
    private EArchivingDefaultService eArchivingDefaultService;

    private final long entityId = 1L;

    @Test
    public void handleError_ok(@Injectable EArchiveBatchEntity eArchiveBatch) {

        LOG.putMDC(DomibusLogger.MDC_BATCH_ENTITY_ID, entityId + "");

        new Expectations() {{

            eArchivingDefaultService.getEArchiveBatch(entityId);
            result = eArchiveBatch;
        }};
        RuntimeException error = new RuntimeException("ERROR");
        eArchiveErrorHandler.handleError(error);

        new FullVerifications(){{
            eArchivingDefaultService.setStatus(eArchiveBatch, EArchiveBatchStatus.FAILED, error.getMessage());
            times = 1;
            eArchivingDefaultService.sendToNotificationQueue(eArchiveBatch, EArchiveBatchStatus.FAILED);
            times = 1;
        }};

    }
}