package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import static eu.domibus.core.message.UserMessageDefaultServiceHelperTest.PartyIdBuilder.aPartyId;
import static eu.domibus.core.message.UserMessageDefaultServiceHelperTest.PropertyBuilder.aProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Sebastian-Ion TINCU
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserMessageDefaultServiceHelperTest {

    public static final String ORIGINAL_SENDER = "sender";

    public static final String FINAL_RECIPIENT = "receiver";

    @Tested
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    @Injectable
    UserMessageDao userMessageDao;

    private String originalSender;

    private String finalRecipient;

    private String partyTo;

    private UserMessage userMessage;

    private Set<MessageProperty> messageProperties = new HashSet<>();


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
//        givenNullMessageProperties();

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
                aPartyId().withValue("first").withType("type").build());

        whenRetrievingTheToParty();

        assertEquals("The receiving party should have been found when the user message has party identifiers for the receiving party", "first", partyTo);
    }

    private void givenNullUserMessage() {
        this.userMessage = null;
    }

    private void givenUserMessage() {
        this.userMessage = new UserMessage();
    }

    private void givenNullMessageProperties() {
        givenMessageProperties(null);
    }

    private void givenMessageProperties() {
        givenMessageProperties(new HashSet<>());
    }

    private void givenMessageProperties(HashSet<MessageProperty> messageProperties) {
        this.messageProperties = messageProperties;
        this.userMessage.setMessageProperties(messageProperties);
    }

    private void givenProperties(MessageProperty... properties) {
        givenProperties(Arrays.asList(properties));
    }

    private void givenProperties(Collection<MessageProperty> properties) {
        messageProperties.addAll(properties);
    }

    private void givenEmptyPartyIdentifiers() {
        givenPartyIdentifiers(null);
    }

    private void givenPartyIdentifiers(PartyId properties) {
        To to = new To();
        if(properties != null) {
            to.setToPartyId(new PartyId());
            to.getToPartyId().setType(properties.getType());
            to.getToPartyId().setValue(properties.getValue());
        }
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
    }


    private void whenRetrievingTheOriginalSender() {
        originalSender = userMessageDefaultServiceHelper.getOriginalSender(userMessage);
    }

    private void whenRetrievingTheFinalRecipient() {
        finalRecipient = userMessageDefaultServiceHelper.getFinalRecipient(userMessage);
    }

    private void whenRetrievingTheToParty() {
        partyTo = userMessageDefaultServiceHelper.getPartyToValue(userMessage);
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
    public void getService(@Injectable UserMessage userMessage,
                           @Injectable ServiceEntity service) {
        String serviceValue = "my service";

        new Expectations() {{

            userMessage.getService();
            result = service;

            service.getValue();
            result = serviceValue;
        }};

        String value = userMessageDefaultServiceHelper.getService(userMessage);
        assertEquals(serviceValue, value);
    }

    @Test
    public void getAction(@Injectable UserMessage userMessage) {
        String action = "my action";

        new Expectations() {{
            userMessage.getActionValue();
            result = action;
        }};

        String value = userMessageDefaultServiceHelper.getAction(userMessage);
        assertEquals(action, value);
    }

    public static final class PropertyBuilder {

        private String value;
        private String name;
        private String type;

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

        public MessageProperty build() {
            MessageProperty property = new MessageProperty();
            property.setValue(value);
            property.setName(name);
            property.setType(type);
            return property;
        }
    }

    public static final class PartyIdBuilder {
        private String value;
        private String type;

        private PartyIdBuilder() {
        }

        public static PartyIdBuilder aPartyId() {
            return new PartyIdBuilder();
        }

        public PartyIdBuilder withValue(String value) {
            this.value = value;
            return this;
        }
        public PartyIdBuilder withType(String type) {
            this.type = type;
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
