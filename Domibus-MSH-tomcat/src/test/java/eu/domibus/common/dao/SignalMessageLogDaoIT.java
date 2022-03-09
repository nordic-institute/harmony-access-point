package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.NotificationStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class SignalMessageLogDaoIT extends AbstractIT {

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    private Date before;
    private Date now;
    private Date after;
    private Date old;

    @Before
    public void setup() {
        before = dateUtil.fromString("2019-01-01T12:00:00Z");
        now = dateUtil.fromString("2020-01-01T12:00:00Z");
        after = dateUtil.fromString("2021-01-01T12:00:00Z");
        old = Date.from(before.toInstant().minusSeconds(60 * 60 * 24)); // one day older than "before"

        messageDaoTestUtil.createSignalMessageLog("msg1", now);
        messageDaoTestUtil.createSignalMessageLog("msg2", now);
        messageDaoTestUtil.createSignalMessageLog("msg3", old);
    }

    @Test
    @Transactional
    public void testCount() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = signalMessageLogDao.countEntries(filters);

        Assert.assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testCountWithMoreFilters() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {"mshRole", MSHRole.RECEIVING},
                {"messageStatus", MessageStatus.RECEIVED},
                // these filters should be ignored:
                {"notificationStatus", NotificationStatus.NOTIFIED},
                {"conversationId", "1"},
                {"failed", now},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = signalMessageLogDao.countEntries(filters);

        Assert.assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testFindAllInfoPaged() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        List<MessageLogInfo> messages = signalMessageLogDao.findAllInfoPaged(0, 10, "received", true, filters);

        Assert.assertEquals(2, messages.size());
    }

    @Test
    @Transactional
    public void testFindAllInfoPagedWithOrder() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                // these filters should be ignored:
                {"notificationStatus", NotificationStatus.NOTIFIED},
                {"conversationId", "1"},
                {"failed", now},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        String[] columnsToTest = new String[]{"nextAttempt", "sendAttempts", "sendAttemptsMax", "failed", "restored", "conversationId", "notificationStatus", "messageStatus", "mshRole", "refToMessageId", "toPartyId", "finalRecipient"};
        for (String column : columnsToTest) {
            List<MessageLogInfo> messages = signalMessageLogDao.findAllInfoPaged(0, 10, column, false, filters);
            Assert.assertEquals(2, messages.size());
        }
    }
}
