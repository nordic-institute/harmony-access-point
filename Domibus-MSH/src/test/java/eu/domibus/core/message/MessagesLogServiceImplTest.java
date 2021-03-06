package eu.domibus.core.message;

import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.model.MessageType;
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

public class MessagesLogServiceImplTest {

    @Tested
    private MessagesLogServiceImpl messagesLogServiceImpl;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    MessagesLogServiceHelper messagesLogServiceHelper;

    @Test
    public void countAndFilter1() {
        int from = 1, max = 20;
        String column = "col1";
        boolean asc = true;
        HashMap<String, Object> filters = new HashMap<>();
        long numberOfUserMessageLogs = 1;
        MessageLogInfo item1 = new MessageLogInfo();
        List<MessageLogInfo> resultList = Arrays.asList(item1);
        MessageLogResultRO resultRo = new MessageLogResultRO();

        new Expectations() {{
            messagesLogServiceHelper.calculateNumberOfMessages((MessageLogDaoBase)any, filters, (MessageLogResultRO)any);
            result = numberOfUserMessageLogs;
            userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            result = resultList;
        }};

        List<MessageLogInfo> res = messagesLogServiceImpl.countAndFilter(userMessageLogDao, from, max, column, asc, filters, resultRo);

        new Verifications() {{
            userMessageLogDao.findAllInfoPaged(from, max, column, asc, filters);
            times = 1;
        }};

        Assert.assertEquals(numberOfUserMessageLogs, res.size());
    }

    @Test
    public void countAndFindPagedTest2() {
        int from = 2, max = 30;
        String column = "col1";
        boolean asc = true;
        MessageType messageType = MessageType.SIGNAL_MESSAGE;
        HashMap<String, Object> filters = new HashMap<>();
        MessageLogInfo item1 = new MessageLogInfo();
        List<MessageLogInfo> resultList = Arrays.asList(item1);

        new Expectations(messagesLogServiceImpl) {{
            messagesLogServiceImpl.countAndFilter((MessageLogDao)any, from, max, column, asc, filters, (MessageLogResultRO)any);
            result = resultList;
        }};

        MessageLogResultRO res = messagesLogServiceImpl.countAndFindPaged(messageType, from, max, column, asc, filters);

        new Verifications() {{
            messagesLogServiceImpl.getMessageLogDao(messageType);
            times = 1;
        }};

        Assert.assertEquals(resultList.size(), res.getMessageLogEntries().size());
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
            messagesLogServiceHelper.calculateNumberOfMessages((MessageLogDaoBase)any, filters, (MessageLogResultRO)any);
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