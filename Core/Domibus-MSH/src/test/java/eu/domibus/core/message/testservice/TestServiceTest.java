package eu.domibus.core.message.testservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.common.model.configuration.Agreement;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.dictionary.ActionDictionaryService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.activation.DataSource;
import java.io.IOException;

/**
 * @author Sebastian-Ion TINCU
 */
@SuppressWarnings({"ConstantConditions", "SameParameterValue", "ResultOfMethodCallIgnored", "unused"})
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
    private ErrorLogService errorLogService;

    @Injectable
    private MessageSubmitter messageSubmitter;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    ActionDictionaryService actionDictionaryService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mocked
    private ObjectMapper gson;

    @Mocked
    SignalMessage signalMessage;

    private String sender;

    private String receiver;

    // TODO Is the receiverType the same as the receiverPartyId?
    private String receiverType;

    private final Submission submission = new Submission();

    private Submission returnedSubmission;

    private String senderPartyId;

    private String receiverPartyId;

    private String serviceType;

    private String initiatorRole;

    private String responderRole;

    private Agreement agreement;

    private String messageId, returnedMessageId;

    private final String partyId = "test";

    private final String userMessageId = "testmessageid";

    @Before
    public void setUp() throws IOException {
        new Expectations() {{
            new ObjectMapper();
            result = gson;

            gson.readValue(anyString, Submission.class);
            result = submission;
        }};
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectPayload() throws IOException {
        givenSenderAndInitiatorCorrectlySet();

        whenCreatingTheSubmissionMessageData();

        thenThePayloadIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectInitiatorParty() throws IOException {
        givenSenderAndInitiatorCorrectlySet();
        givenSenderPartyId("partyId");

        whenCreatingTheSubmissionMessageData();

        thenTheInitiatorPartyIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectServiceType() throws IOException {
        givenSenderAndInitiatorCorrectlySet();
        givenServiceType("serviceType");

        whenCreatingTheSubmissionMessageData();

        thenTheServiceTypeIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectInitiatorRole() throws IOException {
        givenSenderCorrectlySet();
        givenInitiatorRole("initiator");

        whenCreatingTheSubmissionMessageData();

        thenTheInitiatorRoleIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectResponderRole() throws IOException {
        givenSenderAndInitiatorCorrectlySet();
        givenResponderRole("responder");

        whenCreatingTheSubmissionMessageData();

        thenTheResponderRoleIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectAgreementReference() throws IOException {
        givenSenderAndInitiatorCorrectlySet();
        Agreement agreement = new Agreement();
        agreement.setValue("agreement");
        givenAgreementReference(agreement);

        whenCreatingTheSubmissionMessageData();

        thenTheAgreementReferenceIsCorrectlyDefined();
    }

    @Test
    public void createsTheMessageDataToSubmitHavingTheCorrectConversationIdentifier() throws IOException {
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

    private void givenAgreementReference(Agreement agreement) {
        this.agreement = agreement;
        new Expectations() {{
            pModeProvider.getAgreementRef(Ebms3Constants.TEST_SERVICE);
            result = agreement;
        }};
    }

    private void givenTheMessageIdentifier(String messageId) throws MessagingProcessingException {
        this.messageId = messageId;
        new Expectations() {{
            messageSubmitter.submit(submission, BACKEND_NAME);
            result = messageId;
        }};
    }

    private void givenFinalRecipientMessagePropertyContainsInitialValue(String finalRecipient) {
        submission.addMessageProperty(MESSAGE_PROPERTY_KEY_FINAL_RECIPIENT, finalRecipient);
    }

    private void whenCreatingTheSubmissionMessageData() throws IOException {
        returnedSubmission = testService.createSubmission(sender);
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
        Assert.assertEquals("The agreement reference should have been correctly defined", agreement.getValue(), returnedSubmission.getAgreementRef());
    }

    private void thenTheConversationIdentifierIsCorrectlyDefined() {
        Assert.assertEquals("The conversation identifier should have been correctly defined since it's required and the Access Point MUST set its value to \"1\" " +
                "according to section 4.3 of the [ebMS3CORE] specification", "1", returnedSubmission.getConversationId());
    }
}
