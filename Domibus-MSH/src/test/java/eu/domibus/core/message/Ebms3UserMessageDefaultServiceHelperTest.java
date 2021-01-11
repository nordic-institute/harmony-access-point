package eu.domibus.core.message;

import eu.domibus.api.model.MessageInfo;
import eu.domibus.api.model.MessageProperties;
import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.PartyInfo;
import eu.domibus.api.model.Property;
import eu.domibus.api.model.To;
import eu.domibus.api.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static eu.domibus.core.message.Ebms3UserMessageDefaultServiceHelperTest.PartyIdBuilder.aPartyId;
import static eu.domibus.core.message.Ebms3UserMessageDefaultServiceHelperTest.PropertyBuilder.aProperty;
import static org.junit.Assert.assertEquals;

/**
 * @author Sebastian-Ion TINCU
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class Ebms3UserMessageDefaultServiceHelperTest {

    public static final String ORIGINAL_SENDER = "sender";

    public static final String FINAL_RECIPIENT = "receiver";

    @Tested
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    private String originalSender;

    private String finalRecipient;

    private String partyTo;

    private UserMessage userMessage;

    private MessageProperties messageProperties;

    private boolean sameOriginalSender;

    private boolean sameFinalRecipient;

    @Test
    public void getProperties_noMsgProps() {
        givenUserMessage();

        Map<String, String> properties = userMessageDefaultServiceHelper.getProperties(userMessage);

        new FullVerifications(){};

        assertEquals( 0, properties.size());
    }

    @Test
    public void getProperties_noMsgProps2() {
        givenUserMessage();
        givenMessageProperties();

        Map<String, String> properties = userMessageDefaultServiceHelper.getProperties(userMessage);

        new FullVerifications(){};

        assertEquals( 0, properties.size());
    }

    @Test
    public void getProperties_null() {
        givenNullUserMessage();

        Map<String, String> properties = userMessageDefaultServiceHelper.getProperties(userMessage);

        new FullVerifications(){};

        assertEquals( 0, properties.size());
    }

    @Test
    public void getProperties() {
        givenUserMessage();
        givenMessageProperties();
        givenProperties(
                aProperty().withName("dummy").withValue("dummy").build(),
                aProperty().withName(MessageConstants.FINAL_RECIPIENT).withValue("recipient").build(),
                aProperty().withName(MessageConstants.ORIGINAL_SENDER).withValue(ORIGINAL_SENDER).build(),
                aProperty().withName("another_dummy").withValue("another_dummy").build()
        );

        Map<String, String> properties = userMessageDefaultServiceHelper.getProperties(userMessage);

        new FullVerifications(){};

        assertEquals( "dummy", properties.get("dummy"));
        assertEquals( "recipient", properties.get(MessageConstants.FINAL_RECIPIENT));
        assertEquals( ORIGINAL_SENDER, properties.get(MessageConstants.ORIGINAL_SENDER));
        assertEquals( "another_dummy", properties.get("another_dummy"));
    }

    @Test
    public void returnsNullWhenRetrievingTheOriginalSenderAndTheUserMessageIsNull() {
        givenNullUserMessage();

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsNull("The original sender should have been null when the user message is null");
    }

    @Test
    public void returnsNullWhenRetrievingTheOriginalSenderForUserMessagesHavingNullMessageProperties() {
        givenUserMessage();
        givenNullMessageProperties();

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsNull("The original sender should have been null when the user message has null message properties holder");
    }

    @Test
    public void returnsTheOriginalSenderPropertyValueIgnoringOtherPropertiesWhenRetrievingTheOriginalSenderForUserMessagesContainingTheCorrectProperty() {
        givenUserMessage();
        givenMessageProperties();
        givenProperties(
                aProperty().withName("dummy").withValue("dummy").build(),
                aProperty().withName(MessageConstants.FINAL_RECIPIENT).withValue("recipient").build(),
                aProperty().withName(MessageConstants.ORIGINAL_SENDER).withValue(ORIGINAL_SENDER).build(),
                aProperty().withName("another_dummy").withValue("another_dummy").build()
        );

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsEqualTo(ORIGINAL_SENDER, "The original sender should have been correctly set when the user message contains the original sender property");
    }

    @Test
    public void returnsNullWhenRetrievingTheFinalRecipientAndTheUserMessageIsNull() {
        givenNullUserMessage();

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsNull("The final recipient should have been null when the user message is null");
    }

    @Test
    public void returnsNullWhenRetrievingTheFinalRecipientForUserMessagesHavingNullMessageProperties() {
        givenUserMessage();
        givenNullMessageProperties();

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsNull("The final recipient should have been null when the user message has null message properties holder");
    }

    @Test
    public void returnsTheFinalRecipientPropertyValueIgnoringOtherPropertiesWhenRetrievingTheFinalRecipientForUserMessagesContainingTheCorrectProperty() {
        givenUserMessage();
        givenMessageProperties();
        givenProperties(
                aProperty().withName("dummy").withValue("dummy").build(),
                aProperty().withName(MessageConstants.ORIGINAL_SENDER).withValue(ORIGINAL_SENDER).build(),
                aProperty().withName(MessageConstants.FINAL_RECIPIENT).withValue("recipient").build(),
                aProperty().withName("another_dummy").withValue("another_dummy").build()
        );

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsEqualTo("recipient", "The final recipient should have been correctly set when the user message contains the final recipient property");
    }

    @Test
    public void returnsNullWhenRetrievingTheReceivingPartyForUserMessagesHavingNoPartyIdentifiersForTheReceivingParty() {
        givenUserMessage();
        givenEmptyPartyIdentifiers();

        whenRetrievingTheToParty();

        assertNull("The receiving party should have been null when the user message has no party identifiers for the receiving party", partyTo);
    }

    @Test
    public void returnsFirsFoundPartyIdentifierWhenRetrievingTheReceivingPartyForUserMessagesHavingMultiplePartyIdentifiersForTheReceivingParty() {
        givenUserMessage();
        givenPartyIdentifiers(
                aPartyId().withValue("first").build());

        whenRetrievingTheToParty();

        assertEquals("The receiving party should have been found when the user message has party identifiers for the receiving party", "first", partyTo);
    }

    @Test
    public void returnsFalseIfTheProvidedOriginalSenderIsEmptyWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenEmptyOriginalSender();

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        Assert.assertFalse("Should have returned false when checking if an empty original sender is contained inside the user message", sameOriginalSender);
    }

    @Test
    public void returnsFalseIfTheProvidedOriginalSenderDoesNotMatchTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenOriginalSender("notMatchingSender");
        givenUserMessageOriginalSender();

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        Assert.assertFalse("Should have returned false when checking if a non-matching original sender is contained inside the user message", sameOriginalSender);
    }

    @Test
    public void returnsTrueIfTheProvidedOriginalSenderMatchesTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenOriginalSender(ORIGINAL_SENDER);
        givenUserMessageOriginalSender();

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        Assert.assertTrue("Should have returned true when checking if a matching original sender is contained inside the user message", sameOriginalSender);
    }

    @Test
    public void returnsFalseIfTheProvidedFinalRecipientIsEmptyWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenEmptyFinalRecipient();

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        Assert.assertFalse("Should have returned false when checking if an empty final recipient is contained inside the user message", sameFinalRecipient);
    }

    @Test
    public void returnsFalseIfTheProvidedFinalRecipientDoesNotMatchTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenFinalRecipient("notMatchingReceiver");
        givenUserMessageFinalRecipient();

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        Assert.assertFalse("Should have returned false when checking if a non-matching final recipient is contained inside the user message", sameFinalRecipient);
    }

    @Test
    public void returnsTrueIfTheProvidedFinalRecipientMatchesTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenFinalRecipient(FINAL_RECIPIENT);
        givenUserMessageFinalRecipient();

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        Assert.assertTrue("Should have returned true when checking if a matching final recipient is contained inside the user message", sameFinalRecipient);
    }

    private void givenNullUserMessage() {
        this.userMessage = null;
    }

    private void givenUserMessage() {
        this.userMessage = new UserMessage();
    }

    private void givenUserMessageHavingMessageInfo() {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageInfo(new MessageInfo());
        this.userMessage = userMessage;
    }

    private void givenNullMessageProperties() {
        givenMessageProperties(null);
    }

    private void givenMessageProperties() {
        givenMessageProperties(new MessageProperties());
    }

    private void givenMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
        this.userMessage.setMessageProperties(messageProperties);
    }

    private void givenProperties(Property... properties) {
        givenProperties(Arrays.asList(properties));
    }

    private void givenProperties(Collection<Property> properties) {
        messageProperties.getProperty().addAll(properties);
    }

    private void givenEmptyPartyIdentifiers() {
        givenPartyIdentifiers();
    }

    private void givenPartyIdentifiers(PartyId... properties) {
        givenPartyIdentifiers(Arrays.asList(properties));
    }

    private void givenPartyIdentifiers(Collection<PartyId> properties) {
        To to = new To();
        to.getPartyId().addAll(properties);
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
    }

    private void givenEmptyOriginalSender() {
        givenOriginalSender("");
    }

    private void givenOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    private void givenUserMessageOriginalSender() {
        new Expectations(userMessageDefaultServiceHelper) {{
            userMessageDefaultServiceHelper.getOriginalSender(userMessage); result = ORIGINAL_SENDER;
        }};
    }

    private void givenEmptyFinalRecipient() {
        givenFinalRecipient("");
    }

    private void givenFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    private void givenUserMessageFinalRecipient() {
        new Expectations(userMessageDefaultServiceHelper) {{
            userMessageDefaultServiceHelper.getFinalRecipient(userMessage); result = FINAL_RECIPIENT;
        }};
    }

    private void whenRetrievingTheOriginalSender() {
        originalSender = userMessageDefaultServiceHelper.getOriginalSender(userMessage);
    }

    private void whenRetrievingTheFinalRecipient() {
        finalRecipient = userMessageDefaultServiceHelper.getFinalRecipient(userMessage);
    }

    private void whenRetrievingTheToParty() {
        partyTo = userMessageDefaultServiceHelper.getPartyTo(userMessage);
    }

    private void whenCheckingTheUserMessageForContainingTheOriginalSender() {
        sameOriginalSender = userMessageDefaultServiceHelper.isSameOriginalSender(userMessage, originalSender);
    }

    private void whenCheckingTheUserMessageForContainingTheFinalRecipient() {
        sameFinalRecipient = userMessageDefaultServiceHelper.isSameFinalRecipient(userMessage, finalRecipient);
    }

    private void thenOriginalSenderIsNull(String message) {
        thenOriginalSenderIsEqualTo(null, message);
    }

    private void thenOriginalSenderIsEqualTo(String expected, String message) {
        assertEquals(message, expected, originalSender);
    }

    private void thenFinalRecipientIsNull(String message) {
        thenFinalRecipientIsEqualTo(null, message);
    }

    private void thenFinalRecipientIsEqualTo(String expected, String message) {
        assertEquals(message, expected, finalRecipient);
    }

    @Test
    public void getService(@Mocked UserMessage userMessage) {
        String service = "my service";

        new Expectations() {{
            userMessage.getCollaborationInfo().getService().getValue();
            result = service;
        }};

        String value = userMessageDefaultServiceHelper.getService(userMessage);
        assertEquals(service, value);
    }

    @Test
    public void getAction(@Mocked UserMessage userMessage) {
        String action = "my action";

        new Expectations() {{
            userMessage.getCollaborationInfo().getAction();
            result = action;
        }};

        String value = userMessageDefaultServiceHelper.getAction(userMessage);
        assertEquals(action, value);
    }

    public static final class PropertyBuilder {

        protected String value;
        protected String name;
        protected String type;

        private PropertyBuilder() {
        }

        public static PropertyBuilder aProperty() {
            return new PropertyBuilder();
        }

        public PropertyBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public PropertyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public Property build() {
            Property property = new Property();
            property.setValue(value);
            property.setName(name);
            property.setType(type);
            return property;
        }
    }

    public static final class PartyIdBuilder {
        protected String value;
        protected String type;

        private PartyIdBuilder() {
        }

        public static PartyIdBuilder aPartyId() {
            return new PartyIdBuilder();
        }

        public PartyIdBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public PartyId build() {
            PartyId partyId = new PartyId();
            partyId.setValue(value);
            partyId.setType(type);
            return partyId;
        }
    }

}