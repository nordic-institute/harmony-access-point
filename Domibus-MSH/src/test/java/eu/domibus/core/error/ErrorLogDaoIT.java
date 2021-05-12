package eu.domibus.core.error;


import eu.domibus.AbstractCoreIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.common.ErrorCode;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * @author Catalin Enache
 * @since 5.0
 */

public class ErrorLogDaoIT extends AbstractCoreIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorLogDaoIT.class);

    @Autowired
    private ErrorLogDao errorLogDao;

    @Before
    public void setUp(){
        createErrorLog(MSHRole.SENDING, "messageId_123", ErrorCode.EBMS_0003, "error test 4", new Date());
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0001, "error test 1", DateUtils.addDays(new Date(), -1));
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 2", DateUtils.addDays(new Date(), -2));
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 3", DateUtils.addDays(new Date(), -5));


        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    public void createErrorLog(MSHRole mshRole, String messageInErrorId, ErrorCode errorCode, String errorDetail, Date timestamp) {
        ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        MSHRoleEntity mshRole1 = new MSHRoleEntity();
        mshRole1.setRole(mshRole);
        errorLogEntry.setMshRole(mshRole1);
        errorLogEntry.setMessageInErrorId(messageInErrorId);
        errorLogEntry.setErrorCode(errorCode);
        errorLogEntry.setErrorDetail(errorDetail);
        errorLogEntry.setTimestamp(timestamp);

        errorLogDao.create(errorLogEntry);
    }

    @Test
    @Transactional
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void test_deleteErrorLogsWithoutMessageIdOlderThan(){

        int result = errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(2, 1000);
        Assert.assertEquals(2, result);
    }
}
