package eu.domibus.common.dao;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.*;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.JPAConstants;
import eu.domibus.core.message.*;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public class SignalMessageLogDaoIT extends AbstractIT {

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    @Autowired
    SignalMessageDao signalMessageDao;

    @Autowired
    UserMessageDao userMessageDao;

    @Autowired
    MessagePropertyDao propertyDao;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected MessageStatusDao messageStatusDao;

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

        createSignalMessageLog("msg1", now);
        createSignalMessageLog("msg2", now);
        createSignalMessageLog("msg3", old);
    }

    @Test
    @Transactional
    public void testCountAllInfo() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        int count = signalMessageLogDao.countAllInfo(true, filters);

        Assert.assertEquals(2, count);
    }

    @Test
    @Transactional
    public void testCountAllInfoWithFilters() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {"mshRole", MSHRole.RECEIVING},
                {"messageStatus", MessageStatus.RECEIVED},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        int count = signalMessageLogDao.countAllInfo(true, filters);

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


    private void createSignalMessageLog(String msgId, Date received) {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(msgId);
        userMessage.setConversationId("conversation-" + msgId);

        MessageProperty messageProperty1 = propertyDao.findOrCreateProperty("originalSender", "originalSender1", "");
        MessageProperty messageProperty2 = propertyDao.findOrCreateProperty("finalRecipient", "finalRecipient2", "");
        userMessage.setMessageProperties(new HashSet<>(Arrays.asList(messageProperty1, messageProperty2)));

        SignalMessage signal = new SignalMessage();
        signal.setUserMessage(userMessage);
        signal.setSignalMessageId("signal-" + msgId);
        signalMessageDao.create(signal);

        SignalMessageLog signalMessageLog = new SignalMessageLog();
        signalMessageLog.setReceived(received);
        signalMessageLog.setMshRole(mshRoleDao.findOrCreate(MSHRole.RECEIVING));
        signalMessageLog.setMessageStatus(messageStatusDao.findOrCreate(MessageStatus.RECEIVED));

        signalMessageLog.setSignalMessage(signal);
        signalMessageLogDao.create(signalMessageLog);
    }
}
