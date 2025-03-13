package eu.domibus.core.error;


import eu.domibus.test.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author Catalin Enache
 * @since 5.0
 */
public class ErrorLogDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorLogDaoIT.class);

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired
    private MshRoleDao mshRoleDao;
    private ArrayList<ErrorLogEntry> logEntries;

    @Before
    public void setUp() {
        errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(2, 1000);
        logEntries = new ArrayList<>();
        logEntries.add(createErrorLog(MSHRole.SENDING, "messageId_123", ErrorCode.EBMS_0003, "error test 4", new Date()));
        logEntries.add(createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0001, "error test 1", DateUtils.addDays(new Date(), -1)));
        logEntries.add(createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 2", DateUtils.addDays(new Date(), -2)));
        logEntries.add(createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 3", DateUtils.addDays(new Date(), -5)));
        logEntries.add(createErrorLog(MSHRole.RECEIVING, "messageId_2", ErrorCode.EBMS_0004, "error test filter", new Date()));

        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    @After
    public void tearDown() throws Exception {
        errorLogDao.deleteAll(logEntries);
    }

    private ErrorLogEntry createErrorLog(MSHRole mshRole, String messageInErrorId, ErrorCode errorCode, String errorDetail, Date timestamp) {
        UserMessage byEntityId = userMessageDao.findByEntityId(19700101L);

        ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        MSHRoleEntity mshRole1 = mshRoleDao.findOrCreate(mshRole);
        errorLogEntry.setMshRole(mshRole1);
        errorLogEntry.setMessageInErrorId(messageInErrorId);
        errorLogEntry.setErrorCode(errorCode);
        errorLogEntry.setErrorDetail(errorDetail);
        errorLogEntry.setTimestamp(timestamp);
        errorLogEntry.setUserMessage(byEntityId);
        errorLogDao.create(errorLogEntry);

        return errorLogEntry;
    }

    @Test
    public void test_deleteErrorLogsWithoutMessageIdOlderThan() {
        int result = errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(2, 1000);
        Assert.assertEquals(2, result);
    }

    @Test
    public void test_findPaged() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("mshRole", MSHRole.RECEIVING);

        List<ErrorLogEntry> list = errorLogDao.findPaged(0, 10, "timestamp", false, filters);
        for (ErrorLogEntry errorLogEntry : list) {

            LOG.info(errorLogEntry.toString());
        }
        Assert.assertEquals(1, list.size());

        long count = errorLogDao.countEntries(filters);
        Assert.assertEquals(1, count);
    }

}
