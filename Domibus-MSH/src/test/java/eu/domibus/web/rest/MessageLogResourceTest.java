package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.message.MessageLog;
import eu.domibus.core.message.MessageLogInfo;
import eu.domibus.core.message.MessagesLogService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.message.testservice.TestService;
import eu.domibus.core.message.testservice.TestServiceException;
import eu.domibus.core.party.PartyDao;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.replication.UIMessageDao;
import eu.domibus.core.replication.UIMessageService;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.rest.validators.FieldBlacklistValidator;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.web.rest.ro.*;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(Parameterized.class)
public class MessageLogResourceTest {

    private static final String CSV_TITLE = "Conversation Id, From Party Id, To Party Id, Original Sender, Final Recipient, ref To Message Id, Message Id, Message Status, Notification Status, " +
            "MSH Role, Message Type, Deleted, Received, Send Attempts, Send Attempts Max, Next Attempt, Failed, Restored, Message Subtype";

    @Tested
    MessageLogResource messageLogResource;

    @Injectable
    TestService testService;

    @Injectable
    PartyDao partyDao;

    @Injectable
    DateUtil dateUtil;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Injectable
    private UIMessageService uiMessageService;

    @Injectable
    private UIMessageDao uiMessageDao;

    @Injectable
    private MessagesLogService messagesLogService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Parameterized.Parameter(0)
    public MessageType messageType;

    @Parameterized.Parameter(1)
    public MessageLog messageLog;

    @Parameterized.Parameter(2)
    public MessageSubtype messageSubtype;

    @Parameterized.Parameter(3)
    public boolean useFlatTable;

    @Mocked
    SignalMessage signalMessage;

    @Injectable
    FieldBlacklistValidator fieldBlacklistValidator;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Parameterized.Parameters(name = "{index}: messageType=\"{0}\" messageSubtype=\"{2}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {MessageType.USER_MESSAGE, new UserMessageLog(), null, false},
                {MessageType.USER_MESSAGE, new UserMessageLog(), MessageSubtype.TEST, false},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), null, false},
                {MessageType.SIGNAL_MESSAGE, new SignalMessageLog(), MessageSubtype.TEST, false},
        });
    }

    @Test
    public void testMessageLog() {
        // Given
        final MessageLogRO messageLogRO = createMessageLog(messageType, messageSubtype);
        final List<MessageLogRO> resultList = Collections.singletonList(messageLogRO);
        MessageLogResultRO expectedMessageLogResult = new MessageLogResultRO();
        expectedMessageLogResult.setMessageLogEntries(resultList);

        new Expectations() {{
            messagesLogService.countAndFindPaged(messageType, anyInt, anyInt, anyString, anyBoolean, (HashMap<String, Object>) any);
            result = expectedMessageLogResult;
        }};

        // When
        final MessageLogResultRO messageLogResultRO = getMessageLog(messageType, messageSubtype);

        // Then
        Assert.assertNotNull(messageLogResultRO);
        Assert.assertEquals(1, messageLogResultRO.getMessageLogEntries().size());

        MessageLogRO actualMessageLogRO = messageLogResultRO.getMessageLogEntries().get(0);
        Assert.assertEquals(messageLogRO.getMessageId(), actualMessageLogRO.getMessageId());
        Assert.assertEquals(messageLogRO.getMessageStatus(), actualMessageLogRO.getMessageStatus());
        Assert.assertEquals(messageLogRO.getMessageType(), actualMessageLogRO.getMessageType());
        Assert.assertEquals(messageLogRO.getDeleted(), actualMessageLogRO.getDeleted());
        Assert.assertEquals(messageLogRO.getMshRole(), actualMessageLogRO.getMshRole());
        Assert.assertEquals(messageLogRO.getNextAttempt(), actualMessageLogRO.getNextAttempt());
        Assert.assertEquals(messageLogRO.getNotificationStatus(), actualMessageLogRO.getNotificationStatus());
        Assert.assertEquals(messageLogRO.getReceived(), actualMessageLogRO.getReceived());
        Assert.assertEquals(messageLogRO.getSendAttempts(), actualMessageLogRO.getSendAttempts());
        Assert.assertEquals(messageLogRO.getMessageSubtype(), actualMessageLogRO.getMessageSubtype());
    }

    @Test
    public void testMessageLogInfoGetCsv() throws CsvException {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageList = getMessageList(messageType, date, messageSubtype);

        new Expectations() {{
            messagesLogService.findAllInfoCSV(messageType, anyInt, "received", true, (HashMap<String, Object>) any);
            result = messageList;

            csvServiceImpl.exportToCSV(messageList, null, (Map<String, String>) any, (List<String>) any);
            result = CSV_TITLE +
                    "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," +
                    NotificationStatus.NOTIFIED + "," + MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," +
                    date + "," + date + "," + messageSubtype + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = messageLogResource.getCsv(new MessageLogFilterRequestRO() {{
            setOrderBy("received");
            setMessageType(messageType);
            setMessageSubtype(messageSubtype);
        }});

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(CSV_TITLE +
                        "conversationId,fromPartyId,toPartyId,originalSender,finalRecipient,refToMessageId,messageId," + MessageStatus.ACKNOWLEDGED + "," + NotificationStatus.NOTIFIED + "," +
                        MSHRole.RECEIVING + "," + messageType + "," + date + "," + date + ",1,5," + date + "," + date + "," + date + "," + messageSubtype + System.lineSeparator(),
                csv.getBody());
    }

    @Test
    public void testGetLastTestSent(@Injectable TestServiceMessageInfoRO testServiceMessageInfoResult) throws TestServiceException {
        // Given
        String partyId = "test";
        new Expectations() {{
            testService.getLastTestSentWithErrors(partyId);
            result = testServiceMessageInfoResult;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestSent = messageLogResource.getLastTestSent(
                new LatestOutgoingMessageRequestRO() {{
                    setPartyId(partyId);
                }});
        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestSent.getBody();
        Assert.assertEquals(testServiceMessageInfoResult.getPartyId(), testServiceMessageInfoRO.getPartyId());
    }

    @Test(expected = TestServiceException.class)
    public void testGetLastTestSent_NotFound() throws TestServiceException {
        // Given
        String partyId = "partyId";
        new Expectations() {{
            testService.getLastTestSentWithErrors(partyId);
            result = new TestServiceException("No User Message found. Error Details in error log");
        }};

        // When
        messageLogResource.getLastTestSent(
                new LatestOutgoingMessageRequestRO() {{
                    setPartyId(partyId);
                }});
    }

    @Test
    public void testGetLastTestReceived(@Injectable TestServiceMessageInfoRO testServiceMessageInfoResult, @Injectable Party party) throws TestServiceException {
        // Given
        String partyId = "partyId";
        String userMessageId = "userMessageId";
        new Expectations() {{
            testService.getLastTestReceivedWithErrors(partyId, userMessageId);
            result = testServiceMessageInfoResult;
        }};

        // When
        ResponseEntity<TestServiceMessageInfoRO> lastTestReceived = messageLogResource.getLastTestReceived(
                new LatestIncomingMessageRequestRO() {{
                    setPartyId(partyId);
                    setUserMessageId(userMessageId);
                }});
        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestReceived.getBody();
        Assert.assertEquals(testServiceMessageInfoRO.getPartyId(), testServiceMessageInfoResult.getPartyId());
        Assert.assertEquals(testServiceMessageInfoRO.getAccessPoint(), party.getEndpoint());
    }

    @Test(expected = TestServiceException.class)
    public void testGetLastTestReceived_NotFound() throws TestServiceException {
        // Given
        String partyId = "partyId";
        String userMessageId = "userMessageId";
        new Expectations() {{
            testService.getLastTestReceivedWithErrors(partyId, userMessageId);
            result = new TestServiceException("No Signal Message found. Error Details in error log");
        }};

        messageLogResource.getLastTestReceived(
                new LatestIncomingMessageRequestRO() {{
                    setPartyId(partyId);
                    setUserMessageId(userMessageId);
                }});
    }

    /**
     * Creates a {@link MessageLogRO} based on <code>messageType</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param messageSubtype Message Subtype
     * @return <code>MessageLog</code>
     */
    private static MessageLogRO createMessageLog(MessageType messageType, MessageSubtype messageSubtype) {

        MessageLogRO messageLogRO = new MessageLogRO();
        messageLogRO.setMessageId("messageId");
        messageLogRO.setMessageStatus(MessageStatus.ACKNOWLEDGED);
        messageLogRO.setNotificationStatus(NotificationStatus.REQUIRED);
        messageLogRO.setMshRole(MSHRole.RECEIVING);
        messageLogRO.setMessageType(messageType);
        messageLogRO.setDeleted(new Date());
        messageLogRO.setReceived(new Date());
        messageLogRO.setFromPartyId("fromPartyId");
        messageLogRO.setToPartyId("toPartyId");
        messageLogRO.setConversationId("conversationId");
        messageLogRO.setOriginalSender("originalSender");
        messageLogRO.setFinalRecipient("finalRecipient");
        messageLogRO.setRefToMessageId("refToMessageId");
        messageLogRO.setMessageSubtype(messageSubtype);


        return messageLogRO;
    }

    /**
     * Gets a MessageLog based on <code>messageType</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param messageSubtype Message Subtype
     * @return <code>MessageLogResultRO</code> object
     */
    private MessageLogResultRO getMessageLog(MessageType messageType, MessageSubtype messageSubtype) {
        return messageLogResource.getMessageLog(new MessageLogFilterRequestRO() {{
            setPage(1);
            setMessageId("MessageId");
            setMessageType(messageType);
            setMessageSubtype(messageSubtype);
        }});
    }

    /**
     * Get a MessageLogInfo List based on <code>messageInfo</code>, <code>date</code> and <code>messageSubtype</code>
     *
     * @param messageType    Message Type
     * @param date           Date
     * @param messageSubtype Message Subtype
     * @return <code>List</code> of <code>MessageLogInfo</code> objects
     */
    private List<MessageLogInfo> getMessageList(MessageType messageType, Date date, MessageSubtype messageSubtype) {
        List<MessageLogInfo> result = new ArrayList<>();
        MessageLogInfo messageLog = new MessageLogInfo("messageId", MessageStatus.ACKNOWLEDGED,
                NotificationStatus.NOTIFIED, MSHRole.RECEIVING, messageType, date, date, 1, 5, date,
                "conversationId", "fromPartyId", "toPartyId", "originalSender", "finalRecipient",
                "refToMessageId", date, date, messageSubtype, false, false);
        result.add(messageLog);
        return result;
    }
}
