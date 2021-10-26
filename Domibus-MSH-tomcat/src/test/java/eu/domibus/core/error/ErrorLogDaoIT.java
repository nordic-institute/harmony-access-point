package eu.domibus.core.error;


import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.dictionary.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 5.0
 */

public class ErrorLogDaoIT extends AbstractIT {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorLogDaoIT.class);

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private MshRoleDao mshRoleDao;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    protected MpcDao mpcDao;

    @Autowired
    protected PartyIdDao partyIdDao;

    @Autowired
    protected PartyRoleDao partyRoleDao;

    @Autowired
    protected ActionDao actionDao;

    @Autowired
    protected ServiceDao serviceDao;

    @Autowired
    protected AgreementDao agreementDao;



    @Before
    public void setUp() {
        createErrorLog(MSHRole.SENDING, "messageId_123", ErrorCode.EBMS_0003, "error test 4", new Date(), "messageId_123");
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0001, "error test 1", DateUtils.addDays(new Date(), -1), "messageId_1");
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 2", DateUtils.addDays(new Date(), -2), "messageId_2");
        createErrorLog(MSHRole.SENDING, null, ErrorCode.EBMS_0002, "error test 3", DateUtils.addDays(new Date(), -5), "messageId_3");

        createErrorLog(MSHRole.RECEIVING, "messageId_4", ErrorCode.EBMS_0004, "error test filter", new Date(), "messageId_4");

        LOG.putMDC(DomibusLogger.MDC_USER, "test_user");
    }

    private void createErrorLog(MSHRole mshRole, String messageInErrorId, ErrorCode errorCode, String errorDetail, Date timestamp, String msgId) {
        ErrorLogEntry errorLogEntry = new ErrorLogEntry();
        MSHRoleEntity mshRole1 = mshRoleDao.findOrCreate(mshRole);
        errorLogEntry.setMshRole(mshRole1);
        errorLogEntry.setMessageInErrorId(messageInErrorId);
        errorLogEntry.setErrorCode(errorCode);
        errorLogEntry.setErrorDetail(errorDetail);
        errorLogEntry.setTimestamp(timestamp);
        UserMessageLog userMessageLog =messageDaoTestUtil.createUserMessageLog(msgId, new Date());
        errorLogEntry.setUserMessage(userMessageLog.getUserMessage());
        errorLogDao.create(errorLogEntry);
    }

    @Test
    @Transactional
    public void test_deleteErrorLogsWithoutMessageIdOlderThan() {

        int result = errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(2, 1000);
        Assert.assertEquals(2, result);
    }

    @Test
    @Transactional
    public void test_findPaged() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("mshRole", MSHRole.RECEIVING);

        List<ErrorLogEntry> list = errorLogDao.findPaged(0, 10, "timestamp", false, filters);
        Assert.assertEquals(1, list.size());

        long count = errorLogDao.countEntries(filters);
        Assert.assertEquals(1, count);
    }

}
