package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.*;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

import static eu.domibus.common.ErrorCode.EBMS_0001;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestMessageValidatorTest {
    String pmodeKey = "pmodeKey";

    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

    @Tested
    TestMessageValidator testMessageValidator;

    @Test
    public void testCheckTestMessage_false() {
        UserMessage userMessage = createSampleUserMessage();

        Assert.assertFalse("Expecting false for test message as valid data message is supplied ",
                testMessageValidator.checkTestMessage(userMessage));
    }

    @Test
    public void testCheckTestMessage_true() {

        UserMessage userMessage = createSampleUserMessage();
        userMessage.getService().setValue(Ebms3Constants.TEST_SERVICE);
        userMessage.getAction().setValue(Ebms3Constants.TEST_ACTION);

        Assert.assertTrue("Expecting true for Check Test Message with modified data",
                testMessageValidator.checkTestMessage(userMessage));
    }

    @Test
    public void checkTestMessageTest(@Injectable LegConfiguration legConfiguration) {

        new Expectations() {{
            legConfiguration.getService().getValue();
            result = Ebms3Constants.TEST_SERVICE;

            legConfiguration.getAction().getValue();
            result = Ebms3Constants.TEST_ACTION;
        }};

        assertTrue(testMessageValidator.checkTestMessage(legConfiguration));

        new FullVerifications() {
        };
    }

    @Test
    public void checkTestMessageTest_true(@Injectable LegConfiguration legConfiguration) {

        new Expectations() {{
            legConfiguration.getService().getValue();
            result = "service";

            legConfiguration.getAction().getValue();
            result = "action";
        }};

        assertFalse(testMessageValidator.checkTestMessage(legConfiguration));

        new FullVerifications() {
        };
    }

    @Test
    public void checkTestMessageTest_noLeg() {
        assertFalse(testMessageValidator.checkTestMessage((LegConfiguration) null));
        new FullVerifications() {
        };
    }

    protected UserMessage createSampleUserMessage() {
        UserMessage userMessage = new UserMessage();

        ServiceEntity service1 = new ServiceEntity();
        service1.setValue("bdx:noprocess");
        service1.setType("tc1");
        userMessage.setService(service1);

        ActionEntity action = new ActionEntity();
        action.setValue("TC1Leg1");
        userMessage.setAction(action);

        AgreementRefEntity agreementRef1 = new AgreementRefEntity();
        agreementRef1.setValue("");
        agreementRef1.setType("");
        userMessage.setAgreementRef(agreementRef1);

        HashSet<MessageProperty> messageProperties = new HashSet<>();
        messageProperties.add(createMessageProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1"));
        messageProperties.add(createMessageProperty("finalRecipient", FINAL_RECEIPIENT_VALUE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        PartyRole role = getPartyRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        from.setFromRole(role);

        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        from.setFromPartyId(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setToRole(getPartyRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder"));

        PartyId receiver = new PartyId();
        receiver.setValue(RED);
        receiver.setType(DEF_PARTY_TYPE);
        to.setToPartyId(receiver);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        return userMessage;
    }

    private PartyRole getPartyRole(String value) {
        PartyRole role = new PartyRole();
        role.setValue(value);
        return role;
    }

    protected MessageProperty createMessageProperty(String name, String value) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(STRING_TYPE);
        return aProperty;
    }
}
