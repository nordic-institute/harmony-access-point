package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.ListUserMessageDto;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.UserMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore("EDELIVERY-8052 Failing tests must be ignored")
public class UserMessageLogDaoIT extends AbstractIT {

    @Autowired
    UserMessageLogDao userMessageLogDao;

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

        messageDaoTestUtil.createUserMessageLog("msg1", now);
        messageDaoTestUtil.createUserMessageLog("msg2", now);
        messageDaoTestUtil.createUserMessageLog("msg3", old);
    }

    @Test
    @Transactional
    public void testCount() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

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
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = userMessageLogDao.countEntries(filters);

        Assert.assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testFindAllInfoPaged() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        List<MessageLogInfo> messages = userMessageLogDao.findAllInfoPaged(0, 10, "received", true, filters);

        Assert.assertEquals(2, messages.size());
    }

    @Test
    @Transactional
    public void testFindLastTestMessageId() {
        UserMessageLog testMessage = messageDaoTestUtil.createTestMessage("msg-test-1");
        String testParty = testMessage.getUserMessage().getPartyInfo().getToParty(); // "domibus-red"

        String messageId = userMessageLogDao.findLastTestMessageId(testParty);
        Assert.assertEquals("msg-test-1", messageId);
    }

    @Test
    @Transactional
    public void testFindMessagesForArchiving_oldest() {
        UserMessageLog msg3 = userMessageLogDao.findByMessageId("msg1");

        ListUserMessageDto messagesForArchiving = userMessageLogDao.findMessagesForArchivingDesc(0L, 1);
        Assert.assertEquals(1, messagesForArchiving.getUserMessageDtos().size());
        Assert.assertEquals(msg3.getEntityId(), messagesForArchiving.getUserMessageDtos().get(0).getEntityId());
    }

    @Test
    @Transactional
    public void testFindMessagesForArchiving_rest() {
        UserMessageLog msg1 = userMessageLogDao.findByMessageId("msg1");

        ListUserMessageDto messagesForArchiving = userMessageLogDao.findMessagesForArchivingDesc(msg1.getEntityId(), 5);
        Assert.assertEquals(2, messagesForArchiving.getUserMessageDtos().size());
    }
}
