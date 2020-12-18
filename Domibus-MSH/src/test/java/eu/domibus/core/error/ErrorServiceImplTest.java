package eu.domibus.core.error;

import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS;

/**
 * @author Catalin Enache
 * @since 5.0
 */
@RunWith(JMockit.class)
public class ErrorServiceImplTest {

    @Tested
    ErrorServiceImpl errorService;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Test
    public void createErrorLog(final @Mocked ErrorLogEntry errorLogEntry) {
        //tested method
        errorService.createErrorLog(errorLogEntry);

        new FullVerifications() {{
            errorLogDao.create(errorLogEntry);
        }};

    }

    @Test
    public void deleteErrorLogWithoutMessageIds() {
        final int days = 10;
        final int batchSize = 1000;
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS);
            result = days;

            domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE);
            result = batchSize;
        }};

        errorService.deleteErrorLogWithoutMessageIds();

        new FullVerifications() {{
            int daysActual, batchSizeActual;
            errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(daysActual = withCapture(), batchSizeActual = withCapture());
            Assert.assertEquals(days, daysActual);
            Assert.assertEquals(batchSize, batchSizeActual);
        }};
    }
}