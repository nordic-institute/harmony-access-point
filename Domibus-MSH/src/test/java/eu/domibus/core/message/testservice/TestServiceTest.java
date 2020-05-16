package eu.domibus.core.message.testservice;

import com.thoughtworks.xstream.XStream;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.Ebms3Constants;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.web.rest.ro.TestServiceMessageInfoRO;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.activation.DataSource;
import java.io.InputStream;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class TestServiceTest {

    private static final String MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT = Deencapsulation.getField(TestService.class, "MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT");

    private static final String BACKEND_NAME = Deencapsulation.getField(TestService.class, "BACKEND_NAME");

    @Tested
    private TestService testService;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private UserMessageLog userMessageLog;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    private DatabaseMessageHandler databaseMessageHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mocked
    private XStream xStream;

    @Mocked
    SignalMessage signalMessage;

    private String sender;

    private String receiver;

    // TODO Is the receiverType the same as the receiverPartyId?
    private String receiverType;

    private Submission submission = new Submission();

    private Submission returnedSubmission;

    private String senderPartyId;

    private String receiverPartyId;

    private String serviceType;

    private String initiatorRole;

    private String responderRole;

    private String agreement;

    private String messageId, returnedMessageId;

    private String partyId = "test";

    private String userMessageId = "testmessageid";

    @Before
    public void setUp() {
        new Expectations() {{
            new XStream();
            result = xStream;

            xStream.fromXML((InputStream) any);
            result = submission;
        }};
    }

    @Test
    public void failsToCreateTheMessageDataToSubmitWhenTheSenderIsNull() {
        givenSender(null);

        // Expected exception
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("partyId must not be empty");

        whenCreatingTheSubmissionMessageData();
    }

    @Test
    public void failsToCreateTheMessageDataToSubmitWhenTheInitiatorRoleIsNull() {
        givenSenderCorrectlySet();
        givenInitiatorRole(null);

        // Expected exception
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("from role must not be empty");

        whenCreatingTheSubmissionMessageData();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectPayload() {
        givenSenderAndInitiatorCorrectlySet();

        whenCreatingTheSubmissionMessageData();

        thenThePayloadIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectInitiatorParty() {
        givenSenderAndInitiatorCorrectlySet();
        givenSenderPartyId("partyId");

        whenCreatingTheSubmissionMessageData();

        thenTheInitiatorPartyIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectServiceType() {
        givenSenderAndInitiatorCorrectlySet();
        givenServiceType("serviceType");

        whenCreatingTheSubmissionMessageData();

        thenTheServiceTypeIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectInitiatorRole() {
        givenSenderCorrectlySet();
        givenInitiatorRole("initiator");

        whenCreatingTheSubmissionMessageData();

        thenTheInitiatorRoleIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectResponderRole() {
        givenSenderAndInitiatorCorrectlySet();
        givenResponderRole("responder");

        whenCreatingTheSubmissionMessageData();

        thenTheResponderRoleIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectAgreementReference() {
        givenSenderAndInitiatorCorrectlySet();
        givenAgreementReference("agreement");

        whenCreatingTheSubmissionMessageData();

        thenTheAgreementReferenceIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectConversationIdentifier() {
        givenSenderAndInitiatorCorrectlySet();

        whenCreatingTheSubmissionMessageData();

        thenTheConversationIdentifierIsCorrectlyDefined();
    }


    @Test
    public void populatesTheReceiverInsideTheReceivingPartiesWhenSubmittingTheTestMessageNormallyWithoutDynamicDiscovery() throws Exception {
        givenSenderAndInitiatorCorrectlySet();
        givenReceiver("receiver");
        givenReceiverPartyId("receiverPartyId");

        whenSubmittingTheTestMessageNormallyWithoutDynamicDiscovery();

        thenTheReceiverPartyIsCorrectlyDefinedInsideTheReceivingPartiesCollection();
    }

    @Test
    public void populatesTheReceiverAsMessagePropertyWhenSubmittingTheTestMessageWithDynamicDiscovery() throws Exception {
        givenSenderAndInitiatorCorrectlySet();
        givenReceiver("receiver");
        givenReceiverType("receiverType");
        givenFinalRecipientMessagePropertyContainsInitialValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4");

        whenSubmittingTheTestMessageWithDynamicDiscovery();

        thenTheReceiverPartyIsCorrectlyDefinedInsideTheMessagePropertiesReplacingTheInitialValue();
    }

    @Test
    public void returnsTheMessageIdentifierWhenSubmittingTheTestMessageNormallyWithoutDynamicDiscovery() throws Exception {
        givenSenderAndInitiatorCorrectlySet();
        givenReceiver("receiver");
        givenReceiverPartyId("receiverPartyId");
        givenTheMessageIdentifier("messageId");

        whenSubmittingTheTestMessageNormallyWithoutDynamicDiscovery();

        thenTheMessageIdentifierIsCorrectlyReturned();
    }

    @Test
    public void returnsTheMessageIdentifierWhenSubmittingTheTestMessageWithDynamicDiscovery() throws Exception {
        givenSenderAndInitiatorCorrectlySet();
        givenReceiver("receiver");
        givenReceiverType("receiverType");
        givenTheMessageIdentifier("messageId");

        whenSubmittingTheTestMessageWithDynamicDiscovery();

        thenTheMessageIdentifierIsCorrectlyReturned();
    }

    private void givenSender(String sender) {
        this.sender = sender;
    }

    private void givenReceiver(String receiver) {
        this.receiver = receiver;
    }

    private void givenReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    private void givenSenderCorrectlySet() {
        givenSender("sender");
    }

    private void givenSenderAndInitiatorCorrectlySet() {
        givenSenderCorrectlySet();
        givenInitiatorRole("initiator");
    }

    private void givenSenderPartyId(String partyId) {
        this.senderPartyId = partyId;
        new Expectations() {{
            pModeProvider.getPartyIdType(sender);
            result = partyId;
        }};
    }

    private void givenReceiverPartyId(String partyId) {
        this.receiverPartyId = partyId;
        new Expectations() {{
            pModeProvider.getPartyIdType(receiver);
            result = partyId;
        }};
    }

    private void givenServiceType(String serviceType) {
        this.serviceType = serviceType;
        new Expectations() {{
            pModeProvider.getServiceType(Ebms3Constants.TEST_SERVICE);
            result = serviceType;
        }};
    }

    private void givenInitiatorRole(String initiatorRole) {
        this.initiatorRole = initiatorRole;
        new Expectations() {{
            pModeProvider.getRole("INITIATOR", Ebms3Constants.TEST_SERVICE);
            result = initiatorRole;
        }};
    }

    private void givenResponderRole(String responderRole) {
        this.responderRole = responderRole;
        new Expectations() {{
            pModeProvider.getRole("RESPONDER", Ebms3Constants.TEST_SERVICE);
            result = responderRole;
        }};
    }

    private void givenAgreementReference(String agreement) {
        this.agreement = agreement;
        new Expectations() {{
            pModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE);
            result = agreement;
        }};
    }

    private void givenTheMessageIdentifier(String messageId) throws MessagingProcessingException {
        this.messageId = messageId;
        new Expectations() {{
            databaseMessageHandler.submit(submission, BACKEND_NAME);
            result = messageId;
        }};
    }

    private void givenFinalRecipientMessagePropertyContainsInitialValue(String finalRecipient) {
        submission.addMessageProperty(MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT, finalRecipient);
    }

    private void whenCreatingTheSubmissionMessageData() {
        returnedSubmission = Deencapsulation.invoke(testService, "createSubmission", new Class[]{String.class}, sender);
    }

    private void whenSubmittingTheTestMessageNormallyWithoutDynamicDiscovery() throws Exception {
        returnedMessageId = testService.submitTest(sender, receiver);
    }

    private void whenSubmittingTheTestMessageWithDynamicDiscovery() throws Exception {
        returnedMessageId = testService.submitTestDynamicDiscovery(sender, receiver, receiverType);
    }

    private void thenThePayloadIsCorrectlyDefined() {
        Assert.assertEquals("There should be only one payload", 1, returnedSubmission.getPayloads().size());

        Submission.Payload payload = returnedSubmission.getPayloads().iterator().next();
        Assert.assertEquals("The content id should have been correctly defined", "cid:message", payload.getContentId());

        Assert.assertTrue("The 'MimeType' payload property should have been correctly defined", payload.getPayloadProperties().contains(new Submission.TypedProperty("MimeType", "text/xml")));

        DataSource dataSource = payload.getPayloadDatahandler().getDataSource();
        byte[] source = Deencapsulation.getField(dataSource, "source");
        Assert.assertArrayEquals("The payload content should have been correctly defined", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world</hello>".getBytes(), source);
        Assert.assertEquals("The payload content type should have been correctly defined", "text/xml", dataSource.getContentType());
    }

    private void thenTheInitiatorPartyIsCorrectlyDefined() {
        Assert.assertEquals("There should be only one initiator party", 1, returnedSubmission.getFromParties().size());
        Assert.assertEquals("The initiator party should have been correctly defined", new Submission.Party(sender, senderPartyId), returnedSubmission.getFromParties().iterator().next());
    }

    private void thenTheReceiverPartyIsCorrectlyDefinedInsideTheReceivingPartiesCollection() {
        Assert.assertEquals("There should be only one receiver party", 1, submission.getToParties().size());
        Assert.assertEquals("The receiver party should have been correctly defined", new Submission.Party(receiver, receiverPartyId), submission.getToParties().iterator().next());
    }

    private void thenTheReceiverPartyIsCorrectlyDefinedInsideTheMessagePropertiesReplacingTheInitialValue() {
        Assert.assertEquals("There should be only one message property", 1, submission.getMessageProperties().size());
        Assert.assertEquals("The receiver party should have been correctly defined inside the message properties",
                new Submission.TypedProperty(MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT, receiver, receiverType), submission.getMessageProperties().iterator().next());
    }

    private void thenTheMessageIdentifierIsCorrectlyReturned() {
        Assert.assertEquals("The message identifier should have been correctly returned", messageId, returnedMessageId);
    }

    private void thenTheServiceTypeIsCorrectlyDefined() {
        Assert.assertEquals("The service type should have been correctly defined", serviceType, returnedSubmission.getServiceType());
    }

    private void thenTheInitiatorRoleIsCorrectlyDefined() {
        Assert.assertEquals("The initiator role should have been correctly defined", initiatorRole, returnedSubmission.getFromRole());
    }

    private void thenTheResponderRoleIsCorrectlyDefined() {
        Assert.assertEquals("The responder role should have been correctly defined", responderRole, returnedSubmission.getToRole());
    }

    private void thenTheAgreementReferenceIsCorrectlyDefined() {
        Assert.assertEquals("The agreement reference should have been correctly defined", agreement, returnedSubmission.getAgreementRef());
    }

    private void thenTheConversationIdentifierIsCorrectlyDefined() {
        Assert.assertEquals("The conversation identifier should have been correctly defined since it's required and the Access Point MUST set its value to \"1\" " +
                "according to section 4.3 of the [ebMS3CORE] specification", "1", returnedSubmission.getConversationId());
    }

    @Test
    public void testGetLastTestSent() {
        new Expectations() {{
            new XStream();
            times = 0;
            xStream.fromXML((InputStream) any);
            times = 0;
            userMessageLogDao.findLastTestMessageId(partyId);
            result = userMessageId;
            userMessageLogDao.findByMessageId(userMessageId);
            result = userMessageLog;
        }};
        TestServiceMessageInfoRO lastTestSent = testService.getLastTestSentWithErrors(partyId);
        Assert.assertEquals(partyId, lastTestSent.getPartyId());
    }

    @Test(expected = TestServiceException.class)
    public void testGetLastTestSent_NotFound() throws TestServiceException {
        // Given
        new Expectations() {{
            new XStream();
            times = 0;
            xStream.fromXML((InputStream) any);
            times = 0;
            userMessageLogDao.findLastTestMessageId(anyString);
            result = userMessageId;
            userMessageLogDao.findByMessageId(userMessageId);
            result = null;
        }};

        // When
        testService.getLastTestSentWithErrors(partyId);
    }

    @Test
    public void testGetLastTestReceivedWithUserMessageId(@Injectable Messaging messaging, @Injectable Party party) throws TestServiceException {
        // Given
        new Expectations() {{
            party.getEndpoint();
            result = "testEndpoint";
            new XStream();
            times = 0;
            xStream.fromXML((InputStream) any);
            times = 0;
            messagingDao.findMessageByMessageId(anyString);
            result = messaging;
            messaging.getSignalMessage();
            result = signalMessage;
            pModeProvider.getPartyByIdentifier(partyId);
            result = party;
        }};

        // When
        TestServiceMessageInfoRO lastTestReceived = testService.getLastTestReceivedWithErrors(partyId, userMessageId);

        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestReceived;
        Assert.assertEquals(testServiceMessageInfoRO.getMessageId(), signalMessage.getMessageInfo().getMessageId());
        Assert.assertEquals(testServiceMessageInfoRO.getPartyId(), partyId);
        Assert.assertEquals(testServiceMessageInfoRO.getTimeReceived(), signalMessage.getMessageInfo().getTimestamp());
        Assert.assertEquals(testServiceMessageInfoRO.getAccessPoint(), party.getEndpoint());
    }

    @Test(expected = Exception.class)
    public void testGetLastTestReceived_NotFound(@Injectable Messaging messaging) throws Exception {
        // Given
        new Expectations() {{
            new XStream();
            times = 0;
            xStream.fromXML((InputStream) any);
            times = 0;
            messagingDao.findMessageByMessageId(anyString);
            result = messaging;
            messaging.getSignalMessage();
            result = null;
        }};

        testService.getLastTestReceivedWithErrors(partyId, userMessageId);
    }

    @Test
    public void testGetLastTestReceived(@Injectable Party party) throws TestServiceException {
        // Given
        new Expectations() {{
            party.getEndpoint();
            result = "testEndpoint";
            new XStream();
            times = 0;
            xStream.fromXML((InputStream) any);
            times = 0;
            signalMessageLogDao.findLastTestMessageId(partyId);
            result = "signalMessageId";
            messagingDao.findSignalMessageByMessageId("signalMessageId");
            result = signalMessage;
            pModeProvider.getPartyByIdentifier(partyId);
            result = party;
        }};

        // When
        TestServiceMessageInfoRO lastTestReceived = testService.getLastTestReceived(partyId, null);

        // Then
        TestServiceMessageInfoRO testServiceMessageInfoRO = lastTestReceived;
        Assert.assertEquals(testServiceMessageInfoRO.getMessageId(), signalMessage.getMessageInfo().getMessageId());
        Assert.assertEquals(testServiceMessageInfoRO.getPartyId(), partyId);
        Assert.assertEquals(testServiceMessageInfoRO.getTimeReceived(), signalMessage.getMessageInfo().getTimestamp());
        Assert.assertEquals(testServiceMessageInfoRO.getAccessPoint(), party.getEndpoint());
    }

    protected void testGetErrorsDetails() {
        String userMessageId = "mess_id_1", errorDetails = "DOM005-Cannot find party";

        new Expectations(testService) {{
            testService.getErrorsForMessage(userMessageId);
            returns(null, errorDetails);
        }};

        String result = testService.getErrorsDetails(userMessageId);
        Assert.assertTrue(result.equals("Please call the method again to see the details."));

        result = testService.getErrorsDetails(userMessageId);
        Assert.assertTrue(result.equals("Error details are: " + errorDetails));
    }
}