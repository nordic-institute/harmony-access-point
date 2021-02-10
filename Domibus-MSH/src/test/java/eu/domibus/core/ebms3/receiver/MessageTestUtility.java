package eu.domibus.core.ebms3.receiver;

import eu.domibus.api.model.*;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.Date;

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
        final MessageInfo messageInfo = new MessageInfo();
        messageInfo.setTimestamp(new Date());
        messageInfo.setRefToMessageId("321");
        messageInfo.setEntityId(1);
        messageInfo.setCreationTime(new Date());
        messageInfo.setModificationTime(new Date());
        messageInfo.setModifiedBy("baciuco");
        messageInfo.setCreatedBy("baciuco");
        messageInfo.setMessageId("id123456");
        userMessage.setMessageInfo(messageInfo);
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setConversationId("123");
        collaborationInfo.setAction("TC1Leg1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("agreement1");
        agreementRef.setType("agreementType");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", FINAL_RECEIPIENT_VALUE, STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        from.getPartyId().add(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

        PartyId receiver = new PartyId();
        receiver.setValue(RED);
        receiver.setType(DEF_PARTY_TYPE);
        to.getPartyId().add(receiver);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");
        partInfo.setBinaryData("test".getBytes());
        partInfo.setFileName("myFilename");
        partInfo.setInBody(true);
        partInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource("test".getBytes(), "text/xml")));
        partInfo.setMime("application/gzip");
        partInfo.setPartOrder(3);

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("text/xml", "MimeType", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
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
