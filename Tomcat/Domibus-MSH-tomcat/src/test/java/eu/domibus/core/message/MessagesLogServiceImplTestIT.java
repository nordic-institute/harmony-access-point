package eu.domibus.core.message;

import eu.domibus.AbstractIT;
import eu.domibus.ITTestsService;
import eu.domibus.api.model.*;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.plugin.routing.RoutingService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.MessageLogRO;
import eu.domibus.web.rest.ro.MessageLogResultRO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.api.model.MessageStatus.RECEIVED;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Transactional
public class MessagesLogServiceImplTestIT extends AbstractIT {
    public static final String MPC = "UserMessageLogDaoITMpc";

    @Autowired
    MessagesLogServiceImpl messagesLogService;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    ITTestsService itTestsService;

    @Autowired
    protected RoutingService routingService;

    @Autowired
    DateUtil dateUtil;

    private Date before;
    private Date timeT;
    private Date after;
    private Date old;

    private final String deletedNoProperties = randomUUID().toString();
    private final String deletedWithProperties = randomUUID().toString();
    private final String receivedNoProperties = randomUUID().toString();
    private final String receivedWithProperties = randomUUID().toString();
    private final String downloadedNoProperties = randomUUID().toString();
    private final String downloadedWithProperties = randomUUID().toString();
    private final String waitingForRetryNoProperties = randomUUID().toString();
    private final String waitingForRetryWithProperties = randomUUID().toString();
    private final String sendFailureNoProperties = randomUUID().toString();
    private final String sendFailureWithProperties = randomUUID().toString();
    private final String testDate = randomUUID().toString();
    private long maxEntityId;
    private UserMessageLog msg1;
    private UserMessageLog msg2;
    private UserMessageLog msg3;

    @Before
    @Transactional
    public void before() throws IOException, XmlProcessingException {
        uploadPmode();

        addMessages();

        BackendFilter backendFilter = Mockito.mock(BackendFilter.class);
        Mockito.when(routingService.getMatchingBackendFilter(Mockito.any(UserMessage.class))).thenReturn(backendFilter);
    }

    private void addMessages() {
        before = dateUtil.fromString("2019-01-01T12:00:00Z");
        timeT = dateUtil.fromString("2020-01-01T12:00:00Z");
        after = dateUtil.fromString("2021-01-01T12:00:00Z");
        old = Date.from(before.toInstant().minusSeconds(60 * 60 * 24)); // one day older than "before"

        msg1 = messageDaoTestUtil.createUserMessageLog("msg1-" + UUID.randomUUID(), timeT);
        msg2 = messageDaoTestUtil.createUserMessageLog("msg2-" + randomUUID(), timeT);
        msg3 = messageDaoTestUtil.createUserMessageLog("msg3-" + UUID.randomUUID(), old);

        messageDaoTestUtil.createUserMessageLog(testDate, Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()), MSHRole.RECEIVING, MessageStatus.NOT_FOUND, true, MPC, new Date());

        messageDaoTestUtil.createUserMessageLog(deletedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(receivedNoProperties, timeT, MSHRole.SENDING, RECEIVED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(downloadedNoProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(waitingForRetryNoProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, false, MPC, new Date());
        messageDaoTestUtil.createUserMessageLog(sendFailureNoProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, false, MPC, new Date());

        messageDaoTestUtil.createUserMessageLog(deletedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DELETED, "sender1", "recipient1");
        messageDaoTestUtil.createUserMessageLog(receivedWithProperties, timeT, MSHRole.SENDING, RECEIVED, "sender1", "recipient1");
        messageDaoTestUtil.createUserMessageLog(downloadedWithProperties, timeT, MSHRole.SENDING, MessageStatus.DOWNLOADED, "sender1", "recipient1");
        messageDaoTestUtil.createUserMessageLog(waitingForRetryWithProperties, timeT, MSHRole.SENDING, MessageStatus.WAITING_FOR_RETRY, "sender1", "recipient1");
        messageDaoTestUtil.createUserMessageLog(sendFailureWithProperties, timeT, MSHRole.SENDING, MessageStatus.SEND_FAILURE, "sender1", "recipient1");
    }

    @Test
    public void countMessages() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        long count = messagesLogService.countMessages(MessageType.USER_MESSAGE, filters);
        assertEquals(12, count);
    }

    @Test
    public void countAndFindPaged1() {
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Collections.emptyList());
        assertEquals(12, res.getMessageLogEntries().size());
        assertNull(res.getMessageLogEntries().get(0).getAction());
        assertNull(res.getMessageLogEntries().get(0).getServiceType());
    }

    @Test
    public void countAndFindPagedWithOriginalSender() {
        String originalSenderFieldName = "originalSender";
        String originalSenderFieldValue = "sender1";
        int msgCount = 5;

        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {originalSenderFieldName, originalSenderFieldValue},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Collections.emptyList());
        assertEquals(msgCount, res.getMessageLogEntries().size());
        assertTrue(StringUtils.isBlank(res.getMessageLogEntries().get(0).getOriginalSender()));

        res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Arrays.asList(originalSenderFieldName));
        assertEquals(msgCount, res.getMessageLogEntries().size());
        assertEquals(originalSenderFieldValue, res.getMessageLogEntries().get(0).getOriginalSender());

        long count = messagesLogService.countMessages(MessageType.USER_MESSAGE, filters);
        assertEquals(count, res.getMessageLogEntries().size());
    }

    @Test
    public void countAndFindPagedWithFinalRecipient() {
        String fieldName = "finalRecipient";
        String fieldValue = "recipient1";
        int msgCount = 5;
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {fieldName, fieldValue},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Collections.emptyList());
        assertEquals(msgCount, res.getMessageLogEntries().size());

        res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Arrays.asList(fieldName));
        assertEquals(msgCount, res.getMessageLogEntries().size());
        assertEquals(fieldValue, res.getMessageLogEntries().get(0).getFinalRecipient());

        long count = messagesLogService.countMessages(MessageType.USER_MESSAGE, filters);
        assertEquals(count, res.getMessageLogEntries().size());
    }

    @Test
    public void countAndFindPagedWithAction() {
        String fieldName = "action";
        String fieldValue = "TC1Leg1";
        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {fieldName, fieldValue},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Collections.emptyList());
        assertEquals(12, res.getMessageLogEntries().size());
        assertNull(res.getMessageLogEntries().get(0).getAction());

        res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Arrays.asList(fieldName));
        assertEquals(12, res.getMessageLogEntries().size());
        assertEquals(fieldValue, res.getMessageLogEntries().get(0).getAction());
    }

    @Test
    public void countAndFindPagedWithService() {
        final String serviceValue = "bdx:noprocess";
        final String serviceType = "tc1";
        String serviceTypeFieldName = "serviceType";
        String serviceValueFieldName = "serviceValue";

        Map<String, Object> filters = Stream.of(new Object[][]{
                {"receivedFrom", before},
                {"receivedTo", after},
                {serviceTypeFieldName, serviceType},
                {serviceValueFieldName, serviceValue},
        }).collect(Collectors.toMap(data -> (String) data[0], data -> data[1]));

        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Collections.emptyList());
        assertEquals(12, res.getMessageLogEntries().size());
        assertNull(res.getMessageLogEntries().get(0).getServiceType());
        assertNull(res.getMessageLogEntries().get(0).getServiceValue());

        res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, 100, "received", false, filters, Arrays.asList(serviceTypeFieldName, serviceValueFieldName));
        assertEquals(12, res.getMessageLogEntries().size());
        assertEquals(serviceType, res.getMessageLogEntries().get(0).getServiceType());
        assertEquals(serviceValue, res.getMessageLogEntries().get(0).getServiceValue());
    }

    @Test
    public void testMax() {
        int max = 5;
        MessageLogResultRO res = messagesLogService.countAndFindPaged(MessageType.USER_MESSAGE, 0, max, "received", true, new HashMap<>(), Collections.emptyList());
        assertEquals(max, res.getMessageLogEntries().size());
    }

    @Test
    public void findAllInfoCSV() {
    }

}
