package eu.domibus.test.common;

import eu.domibus.api.model.*;

import java.util.HashSet;

public class UserMessageSampleUtil {

    private static final String DOMIBUS_GREEN = "domibus-green";
    private static final String DOMIBUS_RED = "domibus-red";

    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String STRING_TYPE = "string";

    protected static MessageProperty createStringProperty(String name, String value) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(STRING_TYPE);
        return aProperty;
    }

    public static UserMessage createUserMessage() {
        UserMessage userMessage = new UserMessage();
        ActionEntity action = new ActionEntity();
        action.setValue("TC2Leg1");
        userMessage.setAction(action);

        AgreementRefEntity agreementRef1 = new AgreementRefEntity();
        agreementRef1.setValue("");
        userMessage.setAgreementRef(agreementRef1);

        ServiceEntity service1 = new ServiceEntity();
        service1.setValue("bdx:noprocess");
        service1.setType("tc1");
        userMessage.setService(service1);

        HashSet<MessageProperty> messageProperties1 = new HashSet<>();
        messageProperties1.add(createStringProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1"));
        messageProperties1.add(createStringProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4"));
        userMessage.setMessageProperties(messageProperties1);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        from.setFromRole(getRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator"));

        from.setFromPartyId(new PartyId());
        from.getFromPartyId().setValue(DOMIBUS_GREEN);
        from.getFromPartyId().setType(DEF_PARTY_TYPE);
        partyInfo.setFrom(from);

        To to = new To();
        to.setToRole(getRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder"));
        to.setToPartyId(new PartyId());
        to.getToPartyId().setValue(DOMIBUS_RED);
        to.getToPartyId().setType(DEF_PARTY_TYPE);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        return userMessage;
    }

    private static PartyRole getRole(String value) {
        PartyRole partyRole = new PartyRole();
        partyRole.setValue(value);
        return partyRole;
    }


}
