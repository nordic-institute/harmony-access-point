package eu.domibus.core.message;

import eu.domibus.api.model.MessageType;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessagesLogEbms3ServiceImplTest {

    @Tested
    private MessagesLogServiceImpl messagesLogServiceImpl;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private DomibusCoreMapper coreMapper;


    @Test
    public void countAndFindPagedTest1() {
        int from = 1, max = 20;
        String column = "col1";
        boolean asc = true;
        MessageType messageType = MessageType.USER_MESSAGE;
        HashMap<String, Object> filters = new HashMap<>();
        long numberOfUserMessageLogs = 1;
        MessageLogInfo item1 = new MessageLogInfo();
        List<MessageLogInfo> resultList = Arrays.asList(item1);

        new Expectations() {{
            userMessageLogDao.countAllInfo(asc, filters);
            result = numberOfUserMessageLogs;
            userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            result = resultList;
        }};

        MessageLogResultRO res = messagesLogServiceImpl.countAndFindPaged(messageType, from, max, column, asc, filters);

        new Verifications() {{
            userMessageLogDao.countAllInfo(asc, filters);
            times = 1;
            userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            times = 1;
        }};

        Assert.assertEquals(Long.valueOf(numberOfUserMessageLogs), res.getCount());
    }

    @Test
    public void countAndFindPagedTest2() {
        int from = 2, max = 30;
        String column = "col1";
        boolean asc = true;
        MessageType messageType = MessageType.SIGNAL_MESSAGE;
        HashMap<String, Object> filters = new HashMap<>();
        long numberOfLogs = 2;
        MessageLogInfo item1 = new MessageLogInfo();
        List<MessageLogInfo> resultList = Arrays.asList(item1);

        new Expectations() {{
            signalMessageLogDao.countAllInfo(asc, filters);
            result = numberOfLogs;
            signalMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            result = resultList;
        }};

        MessageLogResultRO res = messagesLogServiceImpl.countAndFindPaged(messageType, from, max, column, asc, filters);

        new Verifications() {{
            signalMessageLogDao.countAllInfo(asc, filters);
            times = 1;
            signalMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            times = 1;
        }};

        Assert.assertEquals(Long.valueOf(numberOfLogs), res.getCount());
    }

    @Test
    public void convertMessageLogInfo() {
    }

    @Test
    public void findUserMessageById() {
        String userMessageId = "id1";
        int from = 0, max = 1;
        String column = null;
        boolean asc = true;
        HashMap<String, Object> filters = new HashMap<>();
        long numberOfLogs = 1;
        MessageLogInfo item1 = new MessageLogInfo();
        MessageLogRO converted = new MessageLogRO();
        filters.put("messageId", userMessageId);
        List<MessageLogInfo> resultList = Arrays.asList(item1);

        new Expectations() {{
            userMessageLogDao.countAllInfo(asc, filters);
            result = numberOfLogs;
            userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            result = resultList;
            domainConverter.convert(item1, MessageLogRO.class);
            result = converted;
        }};

        MessageLogRO res = messagesLogServiceImpl.findUserMessageById(userMessageId);

        Assert.assertEquals(converted, res);
    }
}