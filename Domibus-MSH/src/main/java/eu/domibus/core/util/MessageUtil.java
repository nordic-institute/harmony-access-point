package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.util.xml.XMLUtilImpl;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessageUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageUtil.class);

    public static final String LOCAL_NAME = "Id";
    public static final String MESSAGE_INFO = "MessageInfo";
    public static final String TIMESTAMP = "Timestamp";
    public static final String MESSAGE_ID = "MessageId";
    public static final String REF_TO_MESSAGE_ID = "RefToMessageId";
    public static final String RECEIPT = "Receipt";
    public static final String PULL_REQUEST = "PullRequest";
    public static final String NON_REPUDIATION_INFORMATION = "NonRepudiationInformation";
    public static final String ERROR_DETAIL = "ErrorDetail";
    public static final String DESCRIPTION = "Description";
    public static final String LANG = "lang";
    public static final String SHORT_DESCRIPTION = "shortDescription";
    public static final String SEVERITY = "severity";
    public static final String REF_TO_MESSAGE_IN_ERROR = "refToMessageInError";
    public static final String ORIGIN = "origin";
    public static final String ERROR_CODE = "errorCode";
    public static final String CATEGORY = "category";
    public static final String ERROR = "Error";
    public static final String SIGNAL_MESSAGE = "SignalMessage";
    public static final String USER_MESSAGE = "UserMessage";
    public static final String PARTY_INFO = "PartyInfo";
    public static final String ROLE = "Role";
    public static final String FROM = "From";
    public static final String PARTY_ID = "PartyId";
    public static final String TO = "To";
    public static final String COLLABORATION_INFO = "CollaborationInfo";
    public static final String CONVERSATION_ID = "ConversationId";
    public static final String ACTION = "Action";
    public static final String MESSAGE_PROPERTIES = "MessageProperties";
    public static final String PAYLOAD_INFO = "PayloadInfo";
    public static final String PART_INFO = "PartInfo";
    public static final String PROPERTY = "Property";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String HREF = "href";
    public static final String PART_PROPERTIES = "PartProperties";

    protected final JAXBContext jaxbContext;

    protected final JAXBContext jaxbContextMessageFragment;

    protected final DomibusDateFormatter domibusDateFormatter;

    protected final SoapUtil soapUtil;

    public MessageUtil(@Qualifier("jaxbContextEBMS") JAXBContext jaxbContext,
                       @Qualifier("jaxbContextMessageFragment") JAXBContext jaxbContextMessageFragment,
                       DomibusDateFormatter domibusDateFormatter,
                       SoapUtil soapUtil) {
        this.jaxbContext = jaxbContext;
        this.jaxbContextMessageFragment = jaxbContextMessageFragment;
        this.domibusDateFormatter = domibusDateFormatter;
        this.soapUtil = soapUtil;
    }

    @SuppressWarnings("unchecked")
    public Messaging getMessaging(final SOAPMessage soapMessage) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the SOAPMessage");

        final Node messagingXml = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }

    /**
     * Extract the Messaging object using DOM API instead of JAXB
     *
     * @throws SOAPException in the case of a Technical error while parsing the {@link SOAPMessage}
     * @throws EbMS3Exception in the case of a Business error while parsing the {@link SOAPMessage}
     */
    public Messaging getMessagingWithDom(final SOAPMessage soapMessage) throws SOAPException, EbMS3Exception {
        final Node messagingNode = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        return getMessagingWithDom(messagingNode);
    }

    public Messaging getMessagingWithDom(final Node messagingNode) throws SOAPException, EbMS3Exception {
        LOG.debug("Creating the Messaging instance from the SOAPMessage using DOM processing");

        if (messagingNode == null) {
            throw new SOAPException("Could not found Messaging node");
        }

        try {
            Messaging messaging = new Messaging();

            final SignalMessage signalMessage = createSignalMessage(messagingNode);
            messaging.setSignalMessage(signalMessage);

            final UserMessage userMessage = createUserMessage(messagingNode);
            messaging.setUserMessage(userMessage);

            final Map<QName, String> otherAttributes = getOtherAttributes(messagingNode);
            if (otherAttributes != null) {
                messaging.getOtherAttributes().putAll(otherAttributes);
            }

            LOG.debug("Finished creating the Messaging instance from the SOAPMessage using DOM processing");
            return messaging;
        } catch (DomibusCoreException e) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getMessage(), null, e);
        }
    }

    protected UserMessage createUserMessage(Node messagingNode) {
        final Node userMessageNode = getFirstChild(messagingNode, USER_MESSAGE);
        if (userMessageNode == null) {
            LOG.debug("UserMessage is null");
            return null;
        }
        UserMessage result = new UserMessage();

        final String mpc = getAttribute(userMessageNode, "mpc");
        result.setMpc(mpc);

        final MessageInfo messageInfo = createMessageInfo(userMessageNode);
        result.setMessageInfo(messageInfo);

        PartyInfo partyInfo = createPartyInfo(userMessageNode);
        result.setPartyInfo(partyInfo);

        CollaborationInfo collaborationInfo = createCollaborationInfo(userMessageNode);
        result.setCollaborationInfo(collaborationInfo);

        MessageProperties messageProperties = createMessageProperties(userMessageNode);
        result.setMessageProperties(messageProperties);

        PayloadInfo payloadInfo = createPayloadInfo(userMessageNode);
        result.setPayloadInfo(payloadInfo);

        return result;
    }

    protected PayloadInfo createPayloadInfo(Node userMessageNode) {
        final Node payloadInfoNode = getFirstChild(userMessageNode, PAYLOAD_INFO);
        if (payloadInfoNode == null) {
            LOG.debug("PayloadInfo is null");
            return null;
        }
        LOG.debug("Creating PayloadInfo");

        PayloadInfo result = new PayloadInfo();

        final List<Node> partInfoNodes = getChildren(payloadInfoNode, PART_INFO);
        for (Node partInfoNode : partInfoNodes) {
            PartInfo partInfo = createPartInfo(partInfoNode);
            result.getPartInfo().add(partInfo);
        }

        return result;
    }

    private PartInfo createPartInfo(Node partInfoNode) {
        PartInfo result = new PartInfo();

        final String href = getAttribute(partInfoNode, HREF);
        result.setHref(href);
        PartProperties partProperties = createPartProperties(partInfoNode);
        result.setPartProperties(partProperties);

        return result;
    }

    protected PartProperties createPartProperties(Node partInfoNode) {
        final Node partPropertiesNode = getFirstChild(partInfoNode, PART_PROPERTIES);
        if (partPropertiesNode == null) {
            LOG.debug("PartProperties is null");
            return null;
        }
        LOG.debug("Creating createPartProperties");

        PartProperties result = new PartProperties();
        Set<Property> properties = new HashSet<>();


        final List<Node> propertyNodes = getChildren(partPropertiesNode, PROPERTY);
        for (Node propertyNode : propertyNodes) {
            final Property property = createProperty(propertyNode);
            properties.add(property);
        }
        result.setProperty(properties);

        return result;
    }

    protected MessageProperties createMessageProperties(Node userMessageNode) {
        final Node messagePropertiesNode = getFirstChild(userMessageNode, MESSAGE_PROPERTIES);
        if (messagePropertiesNode == null) {
            LOG.debug("MessageProperties is null");
            return null;
        }
        LOG.debug("Creating MessageProperties");

        MessageProperties result = new MessageProperties();

        final List<Node> propertyNodes = getChildren(messagePropertiesNode, PROPERTY);
        for (Node propertyNode : propertyNodes) {
            final Property property = createProperty(propertyNode);
            result.getProperty().add(property);
        }

        return result;
    }

    protected Property createProperty(Node propertyNode) {
        Property result = new Property();
        final String name = getAttribute(propertyNode, NAME);
        final String type = getAttribute(propertyNode, TYPE);
        final String value = getTextContent(propertyNode);

        result.setName(name);
        result.setType(type);
        result.setValue(value);

        return result;
    }

    protected CollaborationInfo createCollaborationInfo(Node userMessageNode) {
        final Node collaborationInfoNode = getFirstChild(userMessageNode, COLLABORATION_INFO);
        if (collaborationInfoNode == null) {
            LOG.debug("CollaborationInfo is null");
            return null;
        }
        LOG.debug("Creating CollaborationInfo");

        CollaborationInfo result = new CollaborationInfo();
        eu.domibus.ebms3.common.model.Service service = createService(collaborationInfoNode);
        result.setService(service);

        final String conversationId = getFirstChildValue(collaborationInfoNode, CONVERSATION_ID);
        result.setConversationId(conversationId);

        final String action = getFirstChildValue(collaborationInfoNode, ACTION);
        result.setAction(action);

        AgreementRef agreement = createAgreementRef(collaborationInfoNode);
        result.setAgreementRef(agreement);

        return result;
    }

    protected AgreementRef createAgreementRef(Node collaborationInfoNode) {
        final Node agreementRefNode = getFirstChild(collaborationInfoNode, "AgreementRef");
        if (agreementRefNode == null) {
            LOG.debug("AgreementRef is null");
            return null;
        }
        LOG.debug("Creating AgreementRef");

        final String serviceType = getAttribute(agreementRefNode, "type");
        final String serviceValue = getTextContent(agreementRefNode);

        AgreementRef result = new AgreementRef();
        result.setValue(serviceValue);
        result.setType(serviceType);
        return result;
    }

    protected eu.domibus.ebms3.common.model.Service createService(Node collaborationInfoNode) {
        final Node serviceNode = getFirstChild(collaborationInfoNode, "Service");
        if (serviceNode == null) {
            LOG.debug("Service is null");
            return null;
        }
        LOG.debug("Creating Service");

        final String serviceType = getAttribute(serviceNode, "type");
        final String serviceValue = getTextContent(serviceNode);

        eu.domibus.ebms3.common.model.Service result = new eu.domibus.ebms3.common.model.Service();
        result.setValue(serviceValue);
        result.setType(serviceType);
        return result;
    }

    protected PartyInfo createPartyInfo(Node userMessageNode) {
        final Node partyInfoNode = getFirstChild(userMessageNode, PARTY_INFO);
        if (partyInfoNode == null) {
            LOG.debug("PartyInfo is null");
            return null;
        }
        LOG.debug("Creating PartyInfo");

        PartyInfo result = new PartyInfo();

        final From from = createFrom(partyInfoNode);
        result.setFrom(from);

        final To to = createTo(partyInfoNode);
        result.setTo(to);

        return result;
    }

    protected To createTo(Node partyInfoNode) {
        final Node toNode = getFirstChild(partyInfoNode, TO);
        if (toNode == null) {
            LOG.debug("To is null");
            return null;
        }
        To result = new To();
        final String role = getFirstChildValue(toNode, ROLE);
        result.setRole(role);
        final Set<PartyId> partyIds = createPartyIds(toNode);
        result.getPartyId().addAll(partyIds);

        return result;
    }

    protected From createFrom(final Node partyInfoNode) {
        final Node fromNode = getFirstChild(partyInfoNode, FROM);
        if (fromNode == null) {
            LOG.debug("From is null");
            return null;
        }
        From result = new From();
        final String role = getFirstChildValue(fromNode, ROLE);
        result.setRole(role);
        final Set<PartyId> partyIds = createPartyIds(fromNode);
        result.getPartyId().addAll(partyIds);


        return result;
    }

    protected Set<PartyId> createPartyIds(Node parent) {
        final List<Node> partyIdNodes = getChildren(parent, PARTY_ID);

        Set<PartyId> result = new HashSet<>();
        for (Node partyIdNode : partyIdNodes) {
            final PartyId partyId = createPartyId(partyIdNode);
            result.add(partyId);
        }

        return result;
    }

    protected PartyId createPartyId(Node partyIdNode) {
        final String partyType = getAttribute(partyIdNode, TYPE);
        final String partyValue = getTextContent(partyIdNode);

        PartyId result = new PartyId();
        result.setType(partyType);
        result.setValue(partyValue);

        return result;
    }

    protected SignalMessage createSignalMessage(final Node messagingNode) throws SOAPException {
        final Node signalNode = getFirstChild(messagingNode, SIGNAL_MESSAGE);
        if (signalNode == null) {
            LOG.debug("SignalMessage is null");
            return null;
        }
        LOG.debug("Creating SignalMessage");

        SignalMessage result = new SignalMessage();

        final MessageInfo messageInfo = createMessageInfo(signalNode);
        result.setMessageInfo(messageInfo);

        final Receipt receipt = createReceipt(signalNode);
        result.setReceipt(receipt);

        PullRequest pullRequest = createPullRequest(signalNode);
        result.setPullRequest(pullRequest);

        Set<Error> error = createErrors(signalNode);
        if (CollectionUtils.isNotEmpty(error)) {
            result.getError().addAll(error);
        }
        return result;
    }

    protected Map<QName, String> getOtherAttributes(final Node messagingNode) {
        Map<QName, String> result = new HashMap<>();

        final NamedNodeMap attributes = messagingNode.getAttributes();
        if (attributes == null) {
            LOG.debug("Messaging node attributes is empty");
            return null;
        }

        final Node namedItemNS = attributes.getNamedItemNS(WSConstants.WSU_NS, LOCAL_NAME);
        if (namedItemNS == null) {
            LOG.debug("No named item found with namespace [{}] and local name [{}]", WSConstants.WSU_NS, LOCAL_NAME);
            return null;
        }

        final String nodeValue = namedItemNS.getNodeValue();
        LOG.debug("Value for named item [{}] with namespace [{}] is [{}]", LOCAL_NAME, WSConstants.WSU_NS, nodeValue);

        final QName name = new QName(WSConstants.WSU_NS, LOCAL_NAME, WSConstants.WSU_PREFIX);
        result.put(name, nodeValue);
        return result;
    }

    protected Set<Error> createErrors(Node signalNode) {
        Set<Error> result = new HashSet<>();

        final List<Node> errorNodeList = getChildren(signalNode, ERROR);
        if (CollectionUtils.isEmpty(errorNodeList)) {
            LOG.debug("Errors node is null");
            return result;
        }

        LOG.debug("Creating Errors");

        for (Node errorNode : errorNodeList) {
            Error error = createError(errorNode);
            result.add(error);
        }

        return result;
    }

    protected Error createError(Node errorNode) {
        Error result = new Error();
        final String category = getAttribute(errorNode, CATEGORY);
        result.setCategory(category);
        final String errorCode = getAttribute(errorNode, ERROR_CODE);
        result.setErrorCode(errorCode);
        final String origin = getAttribute(errorNode, ORIGIN);
        result.setOrigin(origin);
        final String refToMessageInError = getAttribute(errorNode, REF_TO_MESSAGE_IN_ERROR);
        result.setRefToMessageInError(refToMessageInError);
        final String severity = getAttribute(errorNode, SEVERITY);
        result.setSeverity(severity);
        final String shortDescription = getAttribute(errorNode, SHORT_DESCRIPTION);
        result.setShortDescription(shortDescription);

        final Description description = createDescription(errorNode);
        if (description != null) {
            result.setDescription(description);
        }
        final String errorDetail = getErrorDetail(errorNode);
        result.setErrorDetail(errorDetail);

        return result;
    }

    protected String getTextContent(Node node) {
        return StringUtils.trim(node.getTextContent());
    }

    protected String getErrorDetail(Node errorNode) {
        final Node errorDetailNode = getFirstChild(errorNode, ERROR_DETAIL);
        return getTextContent(errorDetailNode);
    }

    protected Description createDescription(Node errorNode) {
        final Node description = getFirstChild(errorNode, DESCRIPTION);
        if (description == null) {
            return null;
        }
        final String lang = getAttribute(errorNode, LANG);
        final String textContent = getTextContent(description);
        Description result = new Description();
        result.setLang(lang);
        result.setValue(textContent);

        return result;
    }

    protected String getAttribute(Node referenceNode, String attributeName) {
        final NamedNodeMap attributes = referenceNode.getAttributes();
        if (attributes == null) {
            return null;
        }
        final Node uri = attributes.getNamedItem(attributeName);
        if (uri == null) {
            return null;
        }
        return getTextContent(uri);
    }

    protected Receipt createReceipt(final Node signalNode) throws SOAPException {
        final Node receiptNode = getFirstChild(signalNode, RECEIPT);
        if (receiptNode == null) {
            LOG.debug("Receipt node is null");
            return null;
        }

        LOG.debug("Creating Receipt");
        Receipt receipt = new Receipt();

        final String nonRepudiationInformation = getNonRepudiationInformationFromReceipt(receiptNode);
        if (StringUtils.isNotEmpty(nonRepudiationInformation)) {
            LOG.debug("Adding [{}] to the Receipt", NON_REPUDIATION_INFORMATION);
            receipt.getAny().add(nonRepudiationInformation);
        }
        String userMessageFromReceipt = getUserMessageFromReceipt(receiptNode);
        if (StringUtils.isNotEmpty(userMessageFromReceipt)) {
            LOG.debug("Adding [{}] to the Receipt", USER_MESSAGE);
            receipt.getAny().add(userMessageFromReceipt);
        }

        return receipt;
    }

    protected String getNonRepudiationInformationFromReceipt(final Node receiptNode) throws SOAPException {
        final Node nonRepudiationInformationNode = getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
        if (nonRepudiationInformationNode == null) {
            LOG.debug("No [{}] found", NON_REPUDIATION_INFORMATION);
            return null;
        }

        try {
            return nodeToString(nonRepudiationInformationNode);
        } catch (TransformerException e) {
            throw new SOAPException("Error while getting NonRepudiationInformation", e);
        }
    }

    protected String getUserMessageFromReceipt(final Node receiptNode) throws SOAPException {
        final Node userMessageNode = getFirstChild(receiptNode, USER_MESSAGE);
        if (userMessageNode == null) {
            LOG.debug("No [{}] found", USER_MESSAGE);
            return null;
        }

        try {
            return nodeToString(userMessageNode);
        } catch (TransformerException e) {
            throw new SOAPException("Error while getting UserMessage", e);
        }
    }

    protected PullRequest createPullRequest(Node signalNode) {
        final Node pullRequestNode = getFirstChild(signalNode, PULL_REQUEST);
        if (pullRequestNode == null) {
            LOG.debug("PullRequest is null");
            return null;
        }
        LOG.debug("Creating PullRequest");

        PullRequest result = new PullRequest();
        final String mpc = getAttribute(pullRequestNode, "mpc");
        result.setMpc(mpc);

        return result;
    }

    protected String nodeToString(final Node node) throws TransformerException {
        final StringWriter sw = new StringWriter();
        final Transformer t = XMLUtilImpl.getTransformerFactory().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    protected MessageInfo createMessageInfo(final Node signalNode) {
        final Node messageInfoNode = getFirstChild(signalNode, MESSAGE_INFO);
        if (messageInfoNode == null) {
            LOG.debug("MessageInfo is null");
            return null;
        }
        LOG.debug("Creating MessageInfo");

        MessageInfo messageInfo = new MessageInfo();
        final String timestampString = getFirstChildValue(messageInfoNode, TIMESTAMP);
        if (timestampString != null) {
            final Date date = domibusDateFormatter.fromString(timestampString);
            messageInfo.setTimestamp(date);
        }

        final String messageId = getFirstChildValue(messageInfoNode, MESSAGE_ID);
        messageInfo.setMessageId(messageId);

        final String refToMessageId = getFirstChildValue(messageInfoNode, REF_TO_MESSAGE_ID);
        messageInfo.setRefToMessageId(refToMessageId);

        return messageInfo;
    }

    protected List<Node> getChildren(Node parent, String childName) {
        List<Node> result = new ArrayList<>();

        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            final Node item = childNodes.item(i);
            if (childName.equals(item.getLocalName())) {
                result.add(item);
            }
        }

        return result;
    }

    protected Node getFirstChild(Node parent, String childName) {
        final List<Node> nodes = getChildren(parent, childName);
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.get(0);
        }

        return null;
    }

    protected String getFirstChildValue(Node parent, String childName) {
        final Node firstChild = getFirstChild(parent, childName);
        if (firstChild == null) {
            LOG.debug("Child [{}] could not be found in parent [{}]", childName, parent.getNodeName());
            return null;
        }
        return getTextContent(firstChild);

    }

    @SuppressWarnings("unchecked")
    public MessageFragmentType getMessageFragment(final SOAPMessage request) {
        try {
            LOG.debug("Unmarshalling the MessageFragmentType instance from the request");
            final Iterator iterator = request.getSOAPHeader().getChildElements(eu.domibus.ebms3.common.model.mf.ObjectFactory._MessageFragment_QNAME);
            if (!iterator.hasNext()) {
                return null;
            }

            final Node messagingXml = (Node) iterator.next();
            final Unmarshaller unmarshaller = jaxbContextMessageFragment.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
            final JAXBElement<MessageFragmentType> root = (JAXBElement<MessageFragmentType>) unmarshaller.unmarshal(messagingXml);
            return root.getValue();
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException("Not possible to get the MessageFragmentType", e);
        }
    }

    public Messaging getMessage(SOAPMessage request) {
        Messaging messaging;
        try {
            messaging = getMessaging(request);
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException("Not possible to getMessage", e);
        }
        return messaging;
    }

}
