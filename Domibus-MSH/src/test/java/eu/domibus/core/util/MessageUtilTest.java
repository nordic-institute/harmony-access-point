package eu.domibus.core.util;

import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.*;

/**
 * @author azhikso
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MessageUtilTest {

    @Tested
    MessageUtil messageUtil;

    @Injectable
    JAXBContext jaxbContextEBMS;

    @Injectable
    JAXBContext jaxbContextMessageFragment;

    @Injectable
    DomibusDateFormatter domibusDateFormatter;

    @Injectable
    SoapUtil soapUtil;

    @Test
    public void getMessagingTest(@Injectable SOAPMessage soapMessage,
                                 @Injectable Node node,
                                 @Injectable Unmarshaller unmarshaller,
                                 @Injectable JAXBElement<Messaging> root
    ) throws SOAPException, JAXBException {
        new Expectations() {{
            soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = node;
            jaxbContextEBMS.createUnmarshaller();
            result = unmarshaller;
            unmarshaller.unmarshal(node);
            result = root;
        }};

        Assert.assertEquals(root.getValue(), messageUtil.getMessaging(soapMessage));
        new Verifications() {{
            unmarshaller.unmarshal(node);
            times = 1;
        }};
    }

    @Test
    public void getMessagingWithDomTest(@Injectable SOAPMessage soapMessage,
                                        @Injectable Node messagingNode,
                                        @Injectable Messaging messaging) throws SOAPException {
        new Expectations(messageUtil) {{
            soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = messagingNode;
            messageUtil.getMessagingWithDom(messagingNode);
            result = messaging;
        }};

        messageUtil.getMessagingWithDom(soapMessage);
        new Verifications() {{
            messageUtil.getMessagingWithDom(messagingNode);
            times = 1;
        }};
    }

    @Test
    public void getNodeMessagingWithDomTest(@Injectable Node messagingNode,
                                            @Injectable Messaging messaging,
                                            @Injectable SignalMessage signalMessage,
                                            @Injectable UserMessage userMessage,
                                            @Injectable QName qName) throws SOAPException {
        final Map<QName, String> otherAttributes = new HashMap<>();
        new Expectations(messageUtil) {{
            messageUtil.createSignalMessage(messagingNode);
            result = signalMessage;
            messageUtil.createUserMessage(messagingNode);
            result = userMessage;
            messageUtil.getOtherAttributes(messagingNode);
            result = otherAttributes;
        }};

        Assert.assertNotNull(messageUtil.getMessagingWithDom(messagingNode));
    }

    @Test
    public void createUserMessageTest(@Injectable Node messagingNode,
                                      @Injectable Messaging messaging,
                                      @Injectable Node userMessageNode,
                                      @Injectable UserMessage userMessage,
                                      @Injectable MessageInfo messageInfo,
                                      @Injectable PartyInfo partyInfo,
                                      @Injectable CollaborationInfo collaborationInfo,
                                      @Injectable MessageProperties messageProperties,
                                      @Injectable PayloadInfo payloadInfo) {
        final String USER_MESSAGE = "UserMessage";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(messagingNode, USER_MESSAGE);
            result = userMessageNode;
            messageUtil.getAttribute(userMessageNode, "mpc");
            result = "mpc";
            messageUtil.createMessageInfo(userMessageNode);
            result = messageInfo;
            messageUtil.createPartyInfo(userMessageNode);
            result = partyInfo;
            messageUtil.createCollaborationInfo(userMessageNode);
            result = collaborationInfo;
            messageUtil.createMessageProperties(userMessageNode);
            result = messageProperties;
            messageUtil.createPayloadInfo(userMessageNode);
            result = payloadInfo;
        }};

        Assert.assertNotNull(messageUtil.createUserMessage(messagingNode));
    }

    @Test
    public void createPayloadInfoTest(@Injectable Node payloadInfoNode,
                                      @Injectable Node userMessageNode
    ) {
        final String PAYLOAD_INFO = "PayloadInfo";
        final String PART_INFO = "PartInfo";
        final List<Node> partInfoNodes = new ArrayList<>();
        partInfoNodes.add(userMessageNode);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, PAYLOAD_INFO);
            result = payloadInfoNode;
            messageUtil.getChildren(payloadInfoNode, PART_INFO);
            result = partInfoNodes;
        }};

        Assert.assertNotNull(messageUtil.createPayloadInfo(userMessageNode));
    }

    @Test
    public void createPartPropertiesTest(@Injectable Node partInfoNode,
                                         @Injectable Node partPropertiesNode,
                                         @Injectable Property property) {
        final String PART_PROPERTIES = "PartProperties";
        final String PROPERTY = "Property";
        final List<Node> propertyNodes = new ArrayList<>();
        propertyNodes.add(partInfoNode);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(partInfoNode, PART_PROPERTIES);
            result = partPropertiesNode;
            messageUtil.getChildren(partPropertiesNode, PROPERTY);
            result = propertyNodes;
            messageUtil.createProperty(propertyNodes.get(0));
            result = property;
        }};

        Assert.assertNotNull(messageUtil.createPartProperties(partInfoNode));
    }

    @Test
    public void createPropertyTest(@Injectable Node propertyNode,
                                   @Injectable Property result) {
        final String NAME = "name";
        final String TYPE = "type";
        new Expectations(messageUtil) {{
            messageUtil.getAttribute(propertyNode, NAME);
            result = "name1";
            messageUtil.getAttribute(propertyNode, TYPE);
            result = "type1";
            messageUtil.getTextContent(propertyNode);
            result = "value1";
        }};

        Assert.assertNotNull(messageUtil.createProperty(propertyNode));
    }

    @Test
    public void createMessagePropertiesTest(@Injectable Node messagePropertiesNode,
                                            @Injectable Node userMessageNode,
                                            @Injectable MessageProperties messageProperties,
                                            @Injectable Property property) {
        final String MESSAGE_PROPERTIES = "MessageProperties";
        final String PROPERTY = "Property";
        final List<Node> propertyNodes = new ArrayList<>();
        propertyNodes.add(userMessageNode);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, MESSAGE_PROPERTIES);
            result = messagePropertiesNode;
            messageUtil.getChildren(messagePropertiesNode, PROPERTY);
            result = propertyNodes;
            messageUtil.createProperty(propertyNodes.get(0));
            result = property;
        }};

        Assert.assertNotNull(messageUtil.createMessageProperties(userMessageNode));
    }

    @Test
    public void createCollaborationInfoTest(@Injectable Node collaborationInfoNode,
                                            @Injectable Node userMessageNode,
                                            @Injectable eu.domibus.ebms3.common.model.Service service,
                                            @Injectable CollaborationInfo collaborationInfo,
                                            @Injectable AgreementRef agreement) {
        final String COLLABORATION_INFO = "CollaborationInfo";
        final String CONVERSATION_ID = "ConversationId";
        final String ACTION = "Action";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, COLLABORATION_INFO);
            result = collaborationInfoNode;
            messageUtil.createService(collaborationInfoNode);
            result = service;
            messageUtil.getFirstChildValue(collaborationInfoNode, CONVERSATION_ID);
            result = "id";
            messageUtil.getFirstChildValue(collaborationInfoNode, ACTION);
            result = "action1";
            messageUtil.createAgreementRef(collaborationInfoNode);
            result = agreement;
        }};
        Assert.assertNotNull(messageUtil.createCollaborationInfo(userMessageNode));
    }

    @Test
    public void createAgreementRefTest(@Injectable Node collaborationInfoNode,
                                       @Injectable Node agreementRefNode) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(collaborationInfoNode, "AgreementRef");
            result = agreementRefNode;
            messageUtil.getAttribute(agreementRefNode, "type");
            result = "serviceType";
            messageUtil.getTextContent(agreementRefNode);
            result = "serviceValue";
        }};
        Assert.assertNotNull(messageUtil.createAgreementRef(collaborationInfoNode));
    }

    @Test
    public void createServiceTest(@Injectable Node collaborationInfoNode,
                                  @Injectable Node serviceNode,
                                  @Injectable eu.domibus.ebms3.common.model.Service service) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(collaborationInfoNode, "Service");
            result = serviceNode;
            messageUtil.getAttribute(serviceNode, "type");
            result = "serviceType";
            messageUtil.getTextContent(serviceNode);
            result = "serviceValue";
        }};
        Assert.assertNotNull(messageUtil.createService(collaborationInfoNode));
    }

    @Test
    public void createPartyInfoTest(@Injectable Node userMessageNode,
                                    @Injectable Node partyInfoNode,
                                    @Injectable From from,
                                    @Injectable To to) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, "PartyInfo");
            result = partyInfoNode;
            messageUtil.createFrom(partyInfoNode);
            result = from;
            messageUtil.createTo(partyInfoNode);
            result = to;
        }};
        Assert.assertNotNull(messageUtil.createPartyInfo(userMessageNode));
    }

    @Test
    public void createToTest(@Injectable Node partyInfoNode,
                             @Injectable Node toNode,
                             @Injectable From from,
                             @Injectable To to,
                             @Injectable PartyId partyId) {
        final Set<PartyId> partyIds = new HashSet<>();
        partyIds.add(partyId);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(partyInfoNode, "To");
            result = toNode;
            messageUtil.getFirstChildValue(toNode, "Role");
            result = "UserRole";
            messageUtil.createPartyIds(toNode);
            result = partyIds;
        }};
        Assert.assertNotNull(messageUtil.createTo(partyInfoNode));
    }

    @Test
    public void createFromTest(@Injectable Node partyInfoNode,
                               @Injectable Node fromNode,
                               @Injectable From from,
                               @Injectable To to,
                               @Injectable PartyId partyId) {
        final Set<PartyId> partyIds = new HashSet<>();
        partyIds.add(partyId);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(partyInfoNode, "From");
            result = fromNode;
            messageUtil.getFirstChildValue(fromNode, "Role");
            result = "UserRole";
            messageUtil.createPartyIds(fromNode);
            result = partyIds;
        }};
        Assert.assertNotNull(messageUtil.createFrom(partyInfoNode));
    }

    @Test
    public void createPartyIdsTest(@Injectable Node parent,
                                   @Injectable Node partyIdNode,
                                   @Injectable PartyId partyId) {
        final List<Node> partyIdNodes = new ArrayList<>();
        partyIdNodes.add(partyIdNode);
        new Expectations(messageUtil) {{
            messageUtil.getChildren(parent, "PartyId");
            result = partyIdNodes;
            messageUtil.createPartyId(partyIdNode);
            result = partyId;
        }};
        Assert.assertNotNull(messageUtil.createPartyIds(parent));
    }

    @Test
    public void createPartyIdTest(@Injectable Node partyIdNode,
                                  @Injectable PartyId partyId) {
        new Expectations(messageUtil) {{
            messageUtil.getAttribute(partyIdNode, "type");
            result = "partyType";
            messageUtil.getTextContent(partyIdNode);
            result = "partyValue";
        }};
        Assert.assertNotNull(messageUtil.createPartyId(partyIdNode));
    }

    @Test
    public void createSignalMessageTest(@Injectable Node messagingNode,
                                        @Injectable Node signalNode,
                                        @Injectable MessageInfo messageInfo,
                                        @Injectable SignalMessage signalMessage,
                                        @Injectable Receipt receipt,
                                        @Injectable PullRequest pullRequest,
                                        @Injectable Error error) throws SOAPException {
        Set<Error> errors = new HashSet<>();
        errors.add(error);
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(messagingNode, "SignalMessage");
            result = signalNode;
            messageUtil.createMessageInfo(signalNode);
            result = messageInfo;
            messageUtil.createReceipt(signalNode);
            result = receipt;
            messageUtil.createPullRequest(signalNode);
            result = pullRequest;
            messageUtil.createErrors(signalNode);
            result = errors;
        }};
        Assert.assertNotNull(messageUtil.createSignalMessage(messagingNode));
    }

    @Test
    public void getOtherAttributesTest(@Injectable Node messagingNode,
                                       @Injectable NamedNodeMap attributes,
                                       @Injectable Node namedItemNS,
                                       @Injectable QName name) {
        final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        new Expectations(messageUtil) {{
            messagingNode.getAttributes();
            result = attributes;
            attributes.getNamedItemNS(WSU_NS, "Id");
            result = namedItemNS;
        }};
        Assert.assertNotNull(messageUtil.getOtherAttributes(messagingNode));
    }

    @Test
    public void createErrorsTest(@Injectable Node signalNode,
                                 @Injectable Node errorNode,
                                 @Injectable Error error) {
        final List<Node> errorNodeList = new ArrayList<>();
        errorNodeList.add(errorNode);
        new Expectations(messageUtil) {{
            messageUtil.getChildren(signalNode, "Error");
            result = errorNodeList;
            messageUtil.createError(errorNode);
            result = error;
        }};
        Assert.assertNotNull(messageUtil.createErrors(signalNode));
    }

    @Test
    public void createErrorTest(@Injectable Node signalNode,
                                @Injectable Node errorNode,
                                @Injectable Error error,
                                @Injectable Description description) {
        final List<Node> errorNodeList = new ArrayList<>();
        final String CATEGORY = "category";
        final String ERROR_CODE = "errorCode";
        final String ORIGIN = "origin";
        final String REF_TO_MESSAGE_IN_ERROR = "refToMessageInError";
        final String SEVERITY = "severity";
        final String SHORT_DESCRIPTION = "shortDescription";
        errorNodeList.add(errorNode);
        new Expectations(messageUtil) {{
            messageUtil.getAttribute(errorNode, CATEGORY);
            result = "category1";
            messageUtil.getAttribute(errorNode, ERROR_CODE);
            result = "Error_001";
            messageUtil.getAttribute(errorNode, ORIGIN);
            result = "origin1";
            messageUtil.getAttribute(errorNode, REF_TO_MESSAGE_IN_ERROR);
            times = 1;
            messageUtil.getAttribute(errorNode, SEVERITY);
            times = 1;
            messageUtil.getAttribute(errorNode, SHORT_DESCRIPTION);
            times = 1;
            messageUtil.createDescription(errorNode);
            result = description;
            messageUtil.getErrorDetail(errorNode);
            result = "error1";
        }};
        Assert.assertNotNull(messageUtil.createError(errorNode));
    }

    @Test
    public void getErrorDetailTest(@Injectable Node errorNode,
                                   @Injectable Node errorDetailNode) {
        final String ERROR_DETAIL = "ErrorDetail";
        final String textContent = "Error1";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(errorNode, ERROR_DETAIL);
            result = errorDetailNode;
            messageUtil.getTextContent(errorDetailNode);
            result = textContent;
        }};
        Assert.assertEquals(textContent, messageUtil.getErrorDetail(errorNode));
    }

    @Test
    public void createDescriptionTest(@Injectable Node errorNode,
                                      @Injectable Node description) {
        final String DESCRIPTION = "Description";
        final String LANG = "lang";
        final String textContent = "Error1";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(errorNode, DESCRIPTION);
            result = description;
            messageUtil.getAttribute(errorNode, LANG);
            result = "English";
            messageUtil.getTextContent(description);
            result = textContent;
        }};
        Assert.assertNotNull(messageUtil.createDescription(errorNode));
    }

    @Test
    public void getAttributeTest(@Injectable Node referenceNode,
                                 @Injectable NamedNodeMap attributes,
                                 @Injectable Node uri) {
        String attributeName = "attribute1";
        new Expectations(messageUtil) {{
            referenceNode.getAttributes();
            result = attributes;
            attributes.getNamedItem(attributeName);
            result = uri;
        }};
        messageUtil.getAttribute(referenceNode, attributeName);
        new Verifications() {{
            messageUtil.getTextContent(uri);
            times = 1;
        }};
    }

    @Test
    public void createReceiptTest(@Injectable Node signalNode,
                                  @Injectable Node receiptNode) throws SOAPException {
        final String RECEIPT = "Receipt";
        String nonRepudiationInformation = "nonRepudiationInfo";
        String userMessageFromReceipt = "userMessageFromReceipt";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, RECEIPT);
            result = receiptNode;
            messageUtil.getNonRepudiationInformationFromReceipt(receiptNode);
            result = nonRepudiationInformation;
            messageUtil.getUserMessageFromReceipt(receiptNode);
            result = userMessageFromReceipt;
        }};
        Assert.assertNotNull(messageUtil.createReceipt(signalNode));
    }

    @Test(expected = SOAPException.class)
    public void getNonRepudiationInformationFromReceiptTest(@Injectable Node nonRepudiationInformationNode,
                                                            @Injectable Node receiptNode) throws TransformerException, SOAPException {
        final String NON_REPUDIATION_INFORMATION = "NonRepudiationInformation";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
            result = nonRepudiationInformationNode;
            messageUtil.nodeToString(nonRepudiationInformationNode);
            result = new TransformerException("Error when transform Node to String");
        }};

        messageUtil.getNonRepudiationInformationFromReceipt(receiptNode);

        new Verifications() {{
            messageUtil.nodeToString(nonRepudiationInformationNode);
            times = 1;
        }};
    }

    @Test(expected = SOAPException.class)
    public void getUserMessageFromReceiptTest(@Injectable Node userMessageNode,
                                              @Injectable Node receiptNode) throws TransformerException, SOAPException {
        final String USER_MESSAGE = "UserMessage";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, USER_MESSAGE);
            result = userMessageNode;
            messageUtil.nodeToString(userMessageNode);
            result = new TransformerException("Error when transform Node to String");
        }};

        messageUtil.getUserMessageFromReceipt(receiptNode);

        new Verifications() {{
            messageUtil.nodeToString(userMessageNode);
            times = 1;
        }};
    }

    @Test
    public void createPullRequestTest(@Injectable Node signalNode,
                                      @Injectable Node pullRequestNode) {
        final String PULL_REQUEST = "PullRequest";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, PULL_REQUEST);
            result = pullRequestNode;
            messageUtil.getAttribute(pullRequestNode, "mpc");
            result = "mpc";
        }};
        Assert.assertNotNull(messageUtil.createPullRequest(signalNode));
    }

    /* @Test
     public void nodeToStringTest(@Injectable final Node node,
                                  @Injectable Transformer transformer,
                                  @Injectable TransformerFactory transformerFactory) throws TransformerException {
         new Expectations() {{
             XMLUtilImpl.getTransformerFactory();
             result=transformerFactory;
             transformerFactory.newTransformer();
             result = transformer;
         }};
         Assert.assertNotNull(messageUtil.nodeToString(node));
     }*/
    @Test
    public void createMessageInfoTest(@Injectable Node signalNode,
                                      @Injectable Node messageInfoNode) {
        final String MESSAGE_INFO = "MessageInfo";
        final String TIMESTAMP = "Timestamp";
        String timestampString = "1573735205377";
        final String MESSAGE_ID = "MessageId";
        String messageId = "502572a9-0eb5-490e-bc3f-412a1bf41914";
        String refToMessageId = "refMessageId";
        final String REF_TO_MESSAGE_ID = "RefToMessageId";
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, MESSAGE_INFO);
            result = messageInfoNode;
            messageUtil.getFirstChildValue(messageInfoNode, TIMESTAMP);
            result = timestampString;
            messageUtil.getFirstChildValue(messageInfoNode, MESSAGE_ID);
            result = messageId;
            messageUtil.getFirstChildValue(messageInfoNode, REF_TO_MESSAGE_ID);
            result = refToMessageId;
        }};
        Assert.assertNotNull(messageUtil.createMessageInfo(signalNode));

    }

    @Test
    public void getMessageFragmentTest(@Injectable SOAPMessage soapMessage,
                                       @Injectable QName qName,
                                       @Injectable Iterator iterator,
                                       @Injectable Node messagingXml,
                                       @Injectable Unmarshaller unmarshaller,
                                       @Injectable JAXBElement<MessageFragmentType> root) throws SOAPException, JAXBException {
        final QName _MessageFragment_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", "MessageFragment");

        new Expectations(messageUtil) {{
            soapMessage.getSOAPHeader().getChildElements(_MessageFragment_QNAME);
            result = iterator;
            iterator.hasNext();
            result = true;
            iterator.next();
            result = messagingXml;
            jaxbContextMessageFragment.createUnmarshaller();
            result = unmarshaller;
            unmarshaller.unmarshal(messagingXml);
            result = root;
        }};
        Assert.assertNotNull(messageUtil.getMessageFragment(soapMessage));

    }
}