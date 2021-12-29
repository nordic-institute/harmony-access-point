package eu.domibus.test.common;

import eu.domibus.api.model.*;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class MessageTestUtility {

    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
    private static final String STRING_TYPE = "string";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";

    public UserMessage createSampleUserMessage() {
        UserMessage userMessage = new UserMessage();
        userMessage.setConversationId("123456");
        userMessage.setTimestamp(new Date());
        userMessage.setRefToMessageId("321");
        userMessage.setCreationTime(new Date());
        userMessage.setModificationTime(new Date());
        userMessage.setModifiedBy("baciuco");
        userMessage.setCreatedBy("baciuco");
        userMessage.setMessageId("id123456");
        userMessage.setConversationId("123");

        ActionEntity actionEntity = createActionEntity();
        userMessage.setAction(actionEntity);

        AgreementRefEntity agreementRef = createAgreementRefEntity();
        userMessage.setAgreementRef(agreementRef);

        ServiceEntity service = createServiceEntity();
        userMessage.setService(service);

        MpcEntity mpcEntity = createMpcEntity();
        userMessage.setMpc(mpcEntity);

        Set<MessageProperty> messageProperties = new HashSet<>();
//        messageProperties.add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
//        messageProperties.add(createProperty("finalRecipient", FINAL_RECEIPIENT_VALUE, STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);

        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);

        From from = new From();
        PartyRole fromRole = createSenderPartyRole();
        from.setFromRole(fromRole);

        PartyId sender = createSenderPartyId();
        from.setFromPartyId(sender);
        partyInfo.setFrom(from);

        To to = new To();
        PartyRole toRole = createReceiverPartyRole();
        to.setToRole(toRole);

        PartyId receiver = createReceiverPartyId();
        to.setToPartyId(receiver);
        partyInfo.setTo(to);

        return userMessage;
    }

    public PartyId createSenderPartyId() {
        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        return sender;
    }

    public PartyId createReceiverPartyId() {
        PartyId receiver = new PartyId();
        receiver.setValue(RED);
        receiver.setType(DEF_PARTY_TYPE);
        return receiver;
    }

    public PartyRole createReceiverPartyRole() {
        PartyRole toRole = new PartyRole();
        toRole.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        return toRole;
    }

    public PartyRole createSenderPartyRole() {
        PartyRole fromRole = new PartyRole();
        fromRole.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        return fromRole;
    }

    public MpcEntity createMpcEntity() {
        MpcEntity mpcEntity = new MpcEntity();
        mpcEntity.setValue("myMpc");
        return mpcEntity;
    }

    public ServiceEntity createServiceEntity() {
        ServiceEntity service = new ServiceEntity();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        return service;
    }

    public AgreementRefEntity createAgreementRefEntity() {
        AgreementRefEntity agreementRef = new AgreementRefEntity();
        agreementRef.setValue("agreement1");
        agreementRef.setType("agreementType");
        return agreementRef;
    }

    public ActionEntity createActionEntity() {
        ActionEntity actionEntity = new ActionEntity();
        actionEntity.setValue("TC1Leg1");
        return actionEntity;
    }

    public List<PartInfo> createPartInfoList(UserMessage userMessage) {
        List<PartInfo> partyInfoList = new ArrayList<>();
        PartInfo partInfo = new PartInfo();
        partInfo.setUserMessage(userMessage);

        partInfo.setHref("cid:message");
        partInfo.setBinaryData("test".getBytes());
        partInfo.setFileName("myFilename");
        partInfo.setInBody(true);
        partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource("test".getBytes(), "text/xml")));
        partInfo.setMime("application/gzip");
        partInfo.setPartOrder(3);

        final PartProperty partProperty = createPartProperty("text/xml", "MimeType", STRING_TYPE);
        Set<PartProperty> partProperties = new HashSet<>();
        partProperties.add(partProperty);
        partInfo.setPartProperties(partProperties);
        partyInfoList.add(partInfo);

        return partyInfoList;
    }


    protected MessageProperty createProperty(String name, String value, String type) {
        MessageProperty aProperty = new MessageProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    protected PartProperty createPartProperty(String name, String value, String type) {
        PartProperty aProperty = new PartProperty();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    public static Document readDocument(String name) throws XMLStreamException, ParserConfigurationException {
        InputStream inStream = MessageTestUtility.class.getResourceAsStream(name);
        return StaxUtils.read(inStream);
    }
}
