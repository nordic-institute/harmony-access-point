package eu.domibus.core.error;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.dictionary.MshRoleDao;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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

    @Injectable
    protected MshRoleDao mshRoleDao;

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

    @Test
    public void getErrors() {
        EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", "MESS_ID", null);
        List<ErrorLogEntry> list = new ArrayList<>();
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.RECEIVING);
        ErrorLogEntry errorLogEntry = new ErrorLogEntry(ex, mshRole);
        list.add(errorLogEntry);

        new Expectations(){{
            errorLogDao.getErrorsForMessage("MESS_ID");
            result = list;
        }};

        List<? extends ErrorResult> result = errorService.getErrors("MESS_ID");

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(errorLogEntry.getErrorCode(), result.get(0).getErrorCode());
        Assert.assertEquals(errorLogEntry.getErrorDetail(), result.get(0).getErrorDetail());
    }
}