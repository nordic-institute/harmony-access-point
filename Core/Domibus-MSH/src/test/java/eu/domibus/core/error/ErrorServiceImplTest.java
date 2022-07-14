package eu.domibus.core.error;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.message.dictionary.MshRoleDao;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
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
    ErrorLogServiceImpl errorService;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    protected ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected MshRoleDao mshRoleDao;

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
        EbMS3Exception ex = EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0008)
                .message("MessageId value is too long (over 255 characters)")
                .refToMessageId("MESS_ID")
                .mshRole(MSHRole.RECEIVING)
                .build();
        List<ErrorLogEntry> list = new ArrayList<>();
        ErrorLogEntry errorLogEntry = new ErrorLogEntry(ex);
        MSHRoleEntity mshRole = new MSHRoleEntity();
        mshRole.setRole(MSHRole.RECEIVING);
        errorLogEntry.setMshRole(mshRole);
        list.add(errorLogEntry);

        new Expectations(){{
            errorLogDao.getErrorsForMessage("MESS_ID", MSHRole.RECEIVING);
            result = list;
        }};

        List<? extends ErrorResult> result = errorService.getErrors("MESS_ID", MSHRole.RECEIVING);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(errorLogEntry.getErrorCode(), result.get(0).getErrorCode());
        Assert.assertEquals(errorLogEntry.getErrorDetail(), result.get(0).getErrorDetail());
    }
}
