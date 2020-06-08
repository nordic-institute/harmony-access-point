package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.*;

import static eu.domibus.core.util.MessageUtil.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@RunWith(JMockit.class)
public class MessageUtilTest {

//    public static final String PAYLOAD_INFO = "PayloadInfo";
//    public static final String MESSAGE_PROPERTIES = "MessageProperties";
//    public static final String COLLABORATION_INFO = "CollaborationInfo";
//    public static final String DESCRIPTION = "Description";
//    public static final String RECEIPT = "Receipt";
//    public static final String NON_REPUDIATION_INFORMATION = "NonRepudiationInformation";
//    public static final String USER_MESSAGE = "UserMessage";
//    public static final String MESSAGE_INFO = "MessageInfo";
    public static final String RESULT = "RESULT";
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
    public void getMessaging(@Injectable SOAPMessage soapMessage,
                             @Injectable Node node,
                             @Injectable Unmarshaller unmarshaller,
                             @Injectable JAXBElement<Messaging> root
    ) throws SOAPException, JAXBException {
        Messaging expectedMessaging = new Messaging();
        new Expectations() {{
            soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = node;
            jaxbContextEBMS.createUnmarshaller();
            result = unmarshaller;
            unmarshaller.unmarshal(node);
            result = root;
            root.getValue();
            result = expectedMessaging;
        }};

        Assert.assertEquals(expectedMessaging, messageUtil.getMessaging(soapMessage));

        new FullVerifications() {};
    }

    @Test
    public void getMessagingWithDom(@Injectable SOAPMessage soapMessage,
                                    @Injectable Node messagingNode,
                                    @Injectable Messaging messaging) throws SOAPException, EbMS3Exception {
        new Expectations(messageUtil) {{
            soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = messagingNode;
            messageUtil.getMessagingWithDom(messagingNode);
            result = messaging;
        }};

        messageUtil.getMessagingWithDom(soapMessage);

        new FullVerifications() {{
            messageUtil.getMessagingWithDom(messagingNode);
            times = 1;
        }};
    }

    @Test
    public void getMessagingWithDom_soapException() throws EbMS3Exception {

        try {
            messageUtil.getMessagingWithDom((Node) null);
            fail();
        } catch (SOAPException e) {
            // nothing to check
        }
    }

    @Test
    public void getNodeMessagingWithDom(@Injectable Node messagingNode,
                                        @Injectable Messaging messaging,
                                        @Injectable SignalMessage signalMessage,
                                        @Injectable UserMessage userMessage,
                                        @Injectable QName qName) throws SOAPException, EbMS3Exception {
        final Map<QName, String> otherAttributes = new HashMap<>();

        new Expectations(messageUtil) {{
            messageUtil.createSignalMessage(messagingNode);
            result = signalMessage;
            messageUtil.createUserMessage(messagingNode);
            result = userMessage;
            messageUtil.getOtherAttributes(messagingNode);
            result = otherAttributes;
        }};

        assertNotNull(messageUtil.getMessagingWithDom(messagingNode));
    }

    @Test
    public void getNodeMessagingWithDom_domibusCoreException(@Injectable Node messagingNode) throws SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.createSignalMessage(messagingNode);
            result = new DomibusCoreException(DomibusCoreErrorCode.DOM_003, "Error");
        }};


        try {
            messageUtil.getMessagingWithDom(messagingNode);
            fail();
        } catch (EbMS3Exception e) {
            assertThat(e.getErrorCode(), is(ErrorCode.EbMS3ErrorCode.EBMS_0003));
        }
    }

    @Test
    public void createUserMessage(@Injectable Node messagingNode,
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

        assertNotNull(messageUtil.createUserMessage(messagingNode));
    }

    @Test
    public void createUserMessage_null(@Injectable Node messagingNode) {
        final String USER_MESSAGE = "UserMessage";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(messagingNode, USER_MESSAGE);
            result = null;
        }};

        assertNull(messageUtil.createUserMessage(messagingNode));
    }

    @Test
    public void createPayloadInfo(@Injectable Node payloadInfoNode,
                                  @Injectable Node userMessageNode
    ) {
        final String PART_INFO = "PartInfo";
        final List<Node> partInfoNodes = new ArrayList<>();
        partInfoNodes.add(userMessageNode);

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, PAYLOAD_INFO);
            result = payloadInfoNode;
            messageUtil.getChildren(payloadInfoNode, PART_INFO);
            result = partInfoNodes;
        }};

        assertNotNull(messageUtil.createPayloadInfo(userMessageNode));
    }

    @Test
    public void createPayloadInfo_null(@Injectable Node userMessageNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, PAYLOAD_INFO);
            result = null;
        }};

        assertNull(messageUtil.createPayloadInfo(userMessageNode));
    }

    @Test
    public void createPartProperties(@Injectable Node partInfoNode,
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

        assertNotNull(messageUtil.createPartProperties(partInfoNode));
    }

    @Test
    public void createProperty(@Injectable Node propertyNode,
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

        assertNotNull(messageUtil.createProperty(propertyNode));
    }

    @Test
    public void createMessageProperties(@Injectable Node messagePropertiesNode,
                                        @Injectable Node userMessageNode,
                                        @Injectable MessageProperties messageProperties,
                                        @Injectable Property property) {
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

        assertNotNull(messageUtil.createMessageProperties(userMessageNode));
    }

    @Test
    public void createMessageProperties_null(@Injectable Node userMessageNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, MESSAGE_PROPERTIES);
            result = null;
        }};

        assertNull(messageUtil.createMessageProperties(userMessageNode));
    }

    @Test
    public void createCollaborationInfo(@Injectable Node collaborationInfoNode,
                                        @Injectable Node userMessageNode,
                                        @Injectable eu.domibus.ebms3.common.model.Service service,
                                        @Injectable CollaborationInfo collaborationInfo,
                                        @Injectable AgreementRef agreement) {
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

        assertNotNull(messageUtil.createCollaborationInfo(userMessageNode));
    }

    @Test
    public void createCollaborationInfo_null(@Injectable Node userMessageNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, COLLABORATION_INFO);
            result = null;
        }};

        assertNull(messageUtil.createCollaborationInfo(userMessageNode));
    }

    @Test
    public void createAgreementRef(@Injectable Node collaborationInfoNode,
                                   @Injectable Node agreementRefNode) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(collaborationInfoNode, "AgreementRef");
            result = agreementRefNode;
            messageUtil.getAttribute(agreementRefNode, "type");
            result = "serviceType";
            messageUtil.getTextContent(agreementRefNode);
            result = "serviceValue";
        }};

        assertNotNull(messageUtil.createAgreementRef(collaborationInfoNode));
    }

    @Test
    public void createAgreementRef_null(@Injectable Node collaborationInfoNode) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(collaborationInfoNode, "AgreementRef");
            result = null;
        }};

        assertNull(messageUtil.createAgreementRef(collaborationInfoNode));
    }

    @Test
    public void createService(@Injectable Node collaborationInfoNode,
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

        assertNotNull(messageUtil.createService(collaborationInfoNode));
    }

    @Test
    public void createService_null(@Injectable Node collaborationInfoNode) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(collaborationInfoNode, "Service");
            result = null;
        }};

        assertNull(messageUtil.createService(collaborationInfoNode));
    }

    @Test
    public void createPartyInfo(@Injectable Node userMessageNode,
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

        assertNotNull(messageUtil.createPartyInfo(userMessageNode));
    }

    @Test
    public void createPartyInfo_null(@Injectable Node userMessageNode) {
        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(userMessageNode, "PartyInfo");
            result = null;
        }};

        assertNull(messageUtil.createPartyInfo(userMessageNode));
    }

    @Test
    public void createTo(@Injectable Node partyInfoNode,
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

        assertNotNull(messageUtil.createTo(partyInfoNode));
    }

    @Test
    public void createTo_null(@Injectable Node partyInfoNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(partyInfoNode, "To");
            result = null;
        }};

        assertNull(messageUtil.createTo(partyInfoNode));
    }

    @Test
    public void createFrom(@Injectable Node partyInfoNode,
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

        assertNotNull(messageUtil.createFrom(partyInfoNode));
    }

    @Test
    public void createFrom_null(@Injectable Node partyInfoNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(partyInfoNode, "From");
            result = null;
        }};

        assertNull(messageUtil.createFrom(partyInfoNode));
    }

    @Test
    public void createPartyIds(@Injectable Node parent,
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

        assertNotNull(messageUtil.createPartyIds(parent));
    }

    @Test
    public void createPartyId(@Injectable Node partyIdNode,
                              @Injectable PartyId partyId) {

        new Expectations(messageUtil) {{
            messageUtil.getAttribute(partyIdNode, "type");
            result = "partyType";
            messageUtil.getTextContent(partyIdNode);
            result = "partyValue";
        }};

        assertNotNull(messageUtil.createPartyId(partyIdNode));
    }

    @Test
    public void createSignalMessage(@Injectable Node messagingNode,
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

        assertNotNull(messageUtil.createSignalMessage(messagingNode));
    }

    @Test
    public void createSignalMessage_null(@Injectable Node messagingNode) throws SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(messagingNode, "SignalMessage");
            result = null;
        }};

        assertNull(messageUtil.createSignalMessage(messagingNode));
    }

    @Test
    public void getOtherAttributes(@Injectable Node messagingNode,
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

        assertNotNull(messageUtil.getOtherAttributes(messagingNode));
    }

    @Test
    public void getOtherAttributes_noAttribute(@Injectable Node messagingNode) {
        new Expectations(messageUtil) {{
            messagingNode.getAttributes();
            result = null;
        }};

        assertNull(messageUtil.getOtherAttributes(messagingNode));
    }

    @Test
    public void getOtherAttributes_noItem(@Injectable Node messagingNode,
                                          @Injectable NamedNodeMap attributes) {
        final String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

        new Expectations(messageUtil) {{
            messagingNode.getAttributes();
            result = attributes;
            attributes.getNamedItemNS(WSU_NS, "Id");
            result = null;
        }};

        assertNull(messageUtil.getOtherAttributes(messagingNode));
    }

    @Test
    public void createErrors(@Injectable Node signalNode,
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

        assertNotNull(messageUtil.createErrors(signalNode));
    }

    @Test
    public void createErrors_null(@Injectable Node signalNode,
                                  @Injectable Node errorNode,
                                  @Injectable Error error) {
        final List<Node> errorNodeList = new ArrayList<>();

        new Expectations(messageUtil) {{
            messageUtil.getChildren(signalNode, "Error");
            result = errorNodeList;
        }};

        assertTrue(messageUtil.createErrors(signalNode).isEmpty());
    }

    @Test
    public void createError(@Injectable Node signalNode,
                            @Injectable Node errorNode,
                            @Injectable Error error,
                            @Injectable Description description) {
        final String CATEGORY = "category";
        final String ERROR_CODE = "errorCode";
        final String ORIGIN = "origin";
        final String REF_TO_MESSAGE_IN_ERROR = "refToMessageInError";
        final String SEVERITY = "severity";
        final String SHORT_DESCRIPTION = "shortDescription";

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

        assertNotNull(messageUtil.createError(errorNode));
    }

    @Test
    public void getErrorDetail(@Injectable Node errorNode,
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
    public void createDescription(@Injectable Node errorNode,
                                  @Injectable Node description) {
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

        assertNotNull(messageUtil.createDescription(errorNode));
    }

    @Test
    public void createDescription_null(@Injectable Node errorNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(errorNode, DESCRIPTION);
            result = null;
        }};

        assertNull(messageUtil.createDescription(errorNode));
    }

    @Test
    public void getAttribute(@Injectable Node referenceNode,
                             @Injectable NamedNodeMap attributes,
                             @Injectable Node uri) {
        String attributeName = "attribute1";

        new Expectations(messageUtil) {{
            referenceNode.getAttributes();
            result = attributes;
            attributes.getNamedItem(attributeName);
            result = uri;
            messageUtil.getTextContent(uri);
            result = RESULT;
        }};

        assertThat(messageUtil.getAttribute(referenceNode, attributeName), is(RESULT));

        new FullVerifications() {};
    }

    @Test
    public void getAttribute_noAttribute(@Injectable Node referenceNode) {
        String attributeName = "attribute1";

        new Expectations(messageUtil) {{
            referenceNode.getAttributes();
            result = null;
        }};

        assertNull(messageUtil.getAttribute(referenceNode, attributeName));

        new FullVerifications() {};
    }

    @Test
    public void getAttribute_noUri(@Injectable Node referenceNode,
                                   @Injectable NamedNodeMap attributes) {
        String attributeName = "attribute1";

        new Expectations(messageUtil) {{
            referenceNode.getAttributes();
            result = attributes;
            attributes.getNamedItem(attributeName);
            result = null;
        }};

        messageUtil.getAttribute(referenceNode, attributeName);

        new FullVerifications() {};
    }

    @Test
    public void createReceipt(@Injectable Node signalNode,
                              @Injectable Node receiptNode) throws SOAPException {
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

        assertNotNull(messageUtil.createReceipt(signalNode));
    }

    @Test
    public void createReceipt_null(@Injectable Node signalNode) throws SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, RECEIPT);
            result = null;
        }};

        assertNull(messageUtil.createReceipt(signalNode));
    }

    @Test
    public void getNonRepudiationInformationFromReceipt_ok(@Injectable Node nonRepudiationInformationNode,
                                                           @Injectable Node receiptNode) throws TransformerException, SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
            result = nonRepudiationInformationNode;
            messageUtil.nodeToString(nonRepudiationInformationNode);
            result = NON_REPUDIATION_INFORMATION;
            times = 1;
        }};

        assertThat(messageUtil.getNonRepudiationInformationFromReceipt(receiptNode), is(NON_REPUDIATION_INFORMATION));

        new FullVerifications() {};
    }

    @Test
    public void getNonRepudiationInformationFromReceipt_null(@Injectable Node nonRepudiationInformationNode,
                                                             @Injectable Node receiptNode) throws  SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
            result = null;
        }};

        assertNull(messageUtil.getNonRepudiationInformationFromReceipt(receiptNode));

        new FullVerifications() {};
    }

    @Test(expected = SOAPException.class)
    public void getNonRepudiationInformationFromReceipt_exception(@Injectable Node nonRepudiationInformationNode,
                                                                  @Injectable Node receiptNode) throws TransformerException, SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
            result = nonRepudiationInformationNode;
            messageUtil.nodeToString(nonRepudiationInformationNode);
            result = new TransformerException("Error when transform Node to String");
        }};

        messageUtil.getNonRepudiationInformationFromReceipt(receiptNode);

        new FullVerifications() {};
    }

    @Test
    public void getUserMessageFromReceipt_ok(@Injectable Node userMessageNode,
                                             @Injectable Node receiptNode) throws TransformerException, SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, USER_MESSAGE);
            result = userMessageNode;
            messageUtil.nodeToString(userMessageNode);
            result = USER_MESSAGE;
        }};

        assertThat(messageUtil.getUserMessageFromReceipt(receiptNode), is(USER_MESSAGE));

        new FullVerifications() {};
    }

    @Test
    public void getUserMessageFromReceipt_null(@Injectable Node receiptNode) throws SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, USER_MESSAGE);
            result = null;
        }};

        assertNull(messageUtil.getUserMessageFromReceipt(receiptNode));

        new FullVerifications() {};
    }

    @Test(expected = SOAPException.class)
    public void getUserMessageFromReceipt(@Injectable Node userMessageNode,
                                          @Injectable Node receiptNode) throws TransformerException, SOAPException {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(receiptNode, USER_MESSAGE);
            result = userMessageNode;
            messageUtil.nodeToString(userMessageNode);
            result = new TransformerException("Error when transform Node to String");
        }};

        messageUtil.getUserMessageFromReceipt(receiptNode);

        new FullVerifications() {};
    }

    @Test
    public void createPullRequest(@Injectable Node signalNode,
                                  @Injectable Node pullRequestNode) {
        final String PULL_REQUEST = "PullRequest";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, PULL_REQUEST);
            result = pullRequestNode;
            messageUtil.getAttribute(pullRequestNode, "mpc");
            result = "mpc";
        }};

        assertNotNull(messageUtil.createPullRequest(signalNode));
        new FullVerifications() {};
    }

    @Test
    public void createPullRequest_null(@Injectable Node signalNode) {
        final String PULL_REQUEST = "PullRequest";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, PULL_REQUEST);
            result = null;
        }};

        assertNull(messageUtil.createPullRequest(signalNode));
        new FullVerifications() {};
    }

    @Test
    public void createMessageInfo(@Injectable Node signalNode,
                                  @Injectable Node messageInfoNode) {
        final String TIMESTAMP = "Timestamp";
        String timestampString = "1573735205377";
        final String MESSAGE_ID = "MessageId";
        String messageId = "502572a9-0eb5-490e-bc3f-412a1bf41914";
        String refToMessageId = "refMessageId";
        final String REF_TO_MESSAGE_ID = "RefToMessageId";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, MESSAGE_INFO);
            result = messageInfoNode;
            domibusDateFormatter.fromString(anyString);
            result = new Date();
            messageUtil.getFirstChildValue(messageInfoNode, TIMESTAMP);
            result = timestampString;
            messageUtil.getFirstChildValue(messageInfoNode, MESSAGE_ID);
            result = messageId;
            messageUtil.getFirstChildValue(messageInfoNode, REF_TO_MESSAGE_ID);
            result = refToMessageId;
        }};

        assertNotNull(messageUtil.createMessageInfo(signalNode));

        new FullVerifications() {};
    }

    @Test
    public void createMessageInfo_null(@Injectable Node signalNode) {

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(signalNode, MESSAGE_INFO);
            result = null;
        }};

        assertNull(messageUtil.createMessageInfo(signalNode));
        new FullVerifications() {};
    }

    @Test
    public void getMessageFragment(@Injectable SOAPMessage soapMessage,
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
            root.getValue();
            result = new MessageFragmentType();
        }};

        assertNotNull(messageUtil.getMessageFragment(soapMessage));
        new FullVerifications() {};
    }

    @Test
    public void getMessageFragment_null(@Injectable SOAPMessage soapMessage,
                                   @Injectable QName qName,
                                   @Injectable Iterator iterator) throws SOAPException {
        final QName _MessageFragment_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", "MessageFragment");

        new Expectations(messageUtil) {{
            soapMessage.getSOAPHeader().getChildElements(_MessageFragment_QNAME);
            result = iterator;
            iterator.hasNext();
            result = false;
        }};

        assertNull(messageUtil.getMessageFragment(soapMessage));

        new FullVerifications() {};
    }

    @Test
    public void getMessageFragment_exception(@Injectable SOAPMessage soapMessage,
                                   @Injectable QName qName,
                                   @Injectable Iterator iterator) throws SOAPException {
        final QName _MessageFragment_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ns/v3.0/mf/2010/04/", "MessageFragment");

        new Expectations(messageUtil) {{
            soapMessage.getSOAPHeader().getChildElements(_MessageFragment_QNAME);
            result = new SOAPException("ERROR");
        }};

        try {
            messageUtil.getMessageFragment(soapMessage);
            fail();
        } catch (MessagingException e) {
            // Nothing to check
        }

        new FullVerifications() {};
    }

    @Test
    public void getMessage_exception(@Injectable SOAPMessage request,
                                     @Injectable Messaging messaging) throws SOAPException, JAXBException {

        new Expectations(messageUtil) {{
            messageUtil.getMessaging(request);
            result = new JAXBException("Error marshalling the message", "DOM_001");
        }};

        try {
            messageUtil.getMessage(request);
            fail();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        new FullVerifications() {};

    }

    @Test
    public void getMessage_ok(@Injectable SOAPMessage request,
                              @Injectable Messaging messaging) throws SOAPException, JAXBException {

        new Expectations(messageUtil) {{
            messageUtil.getMessaging(request);
            result = new Messaging();
        }};

        assertNotNull(messageUtil.getMessage(request));

        new FullVerifications() {};
    }

    @Test
    public void getFirstChildValue(@Injectable Node parent,
                                   @Injectable Node firstChild) {
        String childName = "child1";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(parent, childName);
            result = firstChild;
            messageUtil.getTextContent(firstChild);
            result = RESULT;
        }};

        assertThat(messageUtil.getFirstChildValue(parent, childName), is(RESULT));

        new FullVerifications() {};
    }
    @Test
    public void getFirstChildValue_null(@Injectable Node parent) {
        String childName = "child1";

        new Expectations(messageUtil) {{
            messageUtil.getFirstChild(parent, childName);
            result = null;
            parent.getNodeName();
            result = "nodeName";
        }};

        assertNull(messageUtil.getFirstChildValue(parent, childName));

        new FullVerifications() {};
    }

    @Test
    public void getFirstChild_ok(@Injectable Node parent,
                                   @Injectable Node firstChild) {
        String childName = "child1";

        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(firstChild);

        new Expectations(messageUtil) {{
            messageUtil.getChildren(parent, childName);
            result = nodes;
        }};

        assertThat(messageUtil.getFirstChild(parent, childName), is(firstChild));

        new FullVerifications() {};
    }

    @Test
    public void getFirstChild_null(@Injectable Node parent) {
        String childName = "child1";

        ArrayList<Node> nodes = new ArrayList<>();

        new Expectations(messageUtil) {{
            messageUtil.getChildren(parent, childName);
            result = nodes;
        }};

        assertNull(messageUtil.getFirstChild(parent, childName));

        new FullVerifications() {};
    }

    @Test
    public void getChildren_ok(@Injectable Node parent,
                               @Injectable Node child,
                               @Injectable NodeList nodeList) {
        String childName = "child1";

        new Expectations(messageUtil) {{
            parent.getChildNodes();
            result = nodeList;
            nodeList.getLength();
            result = 1;
            nodeList.item(0);
            result = child;
            child.getLocalName();
            result = childName;
        }};

        assertThat(messageUtil.getChildren(parent, childName), CoreMatchers.hasItem(child));

        new FullVerifications() {};
    }

    @Test
    public void getChildren_empty(@Injectable Node parent,
                               @Injectable Node child,
                               @Injectable NodeList nodeList) {
        String childName = "child1";

        new Expectations(messageUtil) {{
            parent.getChildNodes();
            result = nodeList;
            nodeList.getLength();
            result = 0;
        }};

        assertTrue(messageUtil.getChildren(parent, childName).isEmpty());

        new FullVerifications() {};
    }

    @Test
    public void nodeToString() throws TransformerException {

        assertThat(messageUtil.nodeToString(null), is(""));

        new FullVerifications() {};
    }
}