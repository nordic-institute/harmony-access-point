package eu.domibus.core.util;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.mf.MessageFragmentType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class MessageUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageUtil.class);

    public static final String LOCAL_NAME = "Id";
    public static final String MESSAGE_INFO = "MessageInfo";
    public static final String TIMESTAMP = "Timestamp";
    public static final String MESSAGE_ID = "MessageId";
    public static final String REF_TO_MESSAGE_ID = "RefToMessageId";
    public static final String RECEIPT = "Receipt";
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

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @Qualifier("jaxbContextMessageFragment")
    @Autowired
    protected JAXBContext jaxbContextMessageFragment;

    @Autowired
    protected DomibusDateFormatter domibusDateFormatter;

    @Autowired
    protected SoapUtil soapUtil;


    @SuppressWarnings("unchecked")
    public Messaging getMessaging(final SOAPMessage soapMessage) throws SOAPException, JAXBException {
        LOG.debug("Unmarshalling the Messaging instance from the SOAPMessage");

        final Node messagingXml = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }

    public Messaging getMessagingWithDom(final SOAPMessage soapMessage) throws SOAPException {
        LOG.debug("Creating the Messaging instance from the SOAPMessage using DOM processing");

        Messaging messaging = new Messaging();

        final Node messagingNode = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        if (messagingNode == null) {
            throw new SOAPException("Could not found Messaging node");
        }
        final SignalMessage signalMessage = createSignalMessage(messagingNode);
        messaging.setSignalMessage(signalMessage);

        final Map<QName, String> otherAttributes = getOtherAttributes(messagingNode);
        if (otherAttributes != null) {
            messaging.getOtherAttributes().putAll(otherAttributes);
        }

        LOG.debug("Finished creating the Messaging instance from the SOAPMessage using DOM processing");
        return messaging;
    }

    protected SignalMessage createSignalMessage(final Node messagingNode) throws SOAPException {
        final Node signalNode = getFirstChild(messagingNode, SIGNAL_MESSAGE);
        if (signalNode == null) {
            LOG.debug("SignalMessage is null");
            return null;
        }
        SignalMessage result = new SignalMessage();

        final MessageInfo messageInfo = createMessageInfo(signalNode);
        result.setMessageInfo(messageInfo);

        final Receipt receipt = createReceipt(signalNode);
        result.setReceipt(receipt);

        Set<Error> error = createErrors(signalNode);
        if (CollectionUtils.isNotEmpty(error)) {
            result.getError().addAll(error);
        }
        return result;
    }

    protected Map<QName, String> getOtherAttributes(final Node messagingNode) {
        Map<QName, String> result = new HashMap<>();

        final NamedNodeMap attributes = messagingNode.getAttributes();
        final Node namedItemNS = attributes.getNamedItemNS(WSConstants.WSU_NS, LOCAL_NAME);
        final String nodeValue = namedItemNS.getNodeValue();
        LOG.debug("Value for named item [{}] with namespace [{}] is [{}]", LOCAL_NAME, WSConstants.WSU_NS, nodeValue);

        final QName name = new QName(WSConstants.WSU_NS, LOCAL_NAME, WSConstants.WSU_PREFIX);
        result.put(name, nodeValue);
        return result;
    }

    protected Set<Error> createErrors(Node signalNode) {
        LOG.debug("Creating Errors");

        Set<Error> result = new HashSet<>();

        final List<Node> errorNodeList = getNodes(signalNode, ERROR);
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

    protected String getErrorDetail(Node errorNode) {
        final Node errorDetailNode = getFirstChild(errorNode, ERROR_DETAIL);
        return errorDetailNode.getTextContent();
    }

    protected Description createDescription(Node errorNode) {
        final Node description = getFirstChild(errorNode, DESCRIPTION);
        if (description == null) {

            return null;
        }
        final String lang = getAttribute(errorNode, LANG);
        final String textContent = description.getTextContent();
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
        return uri.getTextContent();
    }

    protected Receipt createReceipt(final Node signalNode) throws SOAPException {
        LOG.debug("Creating Receipt");

        final Node receiptNode = getFirstChild(signalNode, RECEIPT);
        if (receiptNode == null) {
            LOG.debug("Could not find Receipt node");
            return null;
        }

        final Node nonRepudiationInformationNode = getFirstChild(receiptNode, NON_REPUDIATION_INFORMATION);
        try {
            final String nonRepudiationInformation = nodeToString(nonRepudiationInformationNode);
            Receipt receipt = new Receipt();
            receipt.getAny().add(nonRepudiationInformation);
            return receipt;
        } catch (TransformerException e) {
            throw new SOAPException("Error while creating Receipt", e);
        }
    }

    protected String nodeToString(final Node node) throws TransformerException {
        final StringWriter sw = new StringWriter();
        final Transformer t = soapUtil.createTransformerFactory().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    protected MessageInfo createMessageInfo(final Node signalNode) {
        LOG.debug("Creating MessageInfo");

        final Node messageInfoNode = getFirstChild(signalNode, MESSAGE_INFO);
        if (messageInfoNode == null) {
            LOG.debug("MessageInfo is null");
            return null;
        }

        MessageInfo messageInfo = new MessageInfo();
        final String timestampString = getNodeValue(messageInfoNode, TIMESTAMP);
        if (timestampString != null) {
            final Date date = domibusDateFormatter.fromString(timestampString);
            messageInfo.setTimestamp(date);
        }

        final String messageId = getNodeValue(messageInfoNode, MESSAGE_ID);
        messageInfo.setMessageId(messageId);

        final String refToMessageId = getNodeValue(messageInfoNode, REF_TO_MESSAGE_ID);
        messageInfo.setRefToMessageId(refToMessageId);

        return messageInfo;
    }

    protected Date toDate(String dateString) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        final LocalDateTime localDateTime = LocalDateTime.parse(dateString, dtf);
        return Date.from(localDateTime.atZone(ZoneOffset.UTC).toInstant());
    }

    protected List<Node> getNodes(Node parent, String childName) {
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
        final List<Node> nodes = getNodes(parent, childName);
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.get(0);
        }

        return null;
    }

    protected String getNodeValue(Node parent, String childName) {
        final Node firstChild = getFirstChild(parent, childName);
        if (firstChild == null) {
            LOG.debug("Child [{}] could not be found in parent [{}]", childName, parent.getNodeName());
            return null;
        }
        return firstChild.getTextContent();

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
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Not possible to get the MessageFragmentType", e);
        }
    }

    public Messaging getMessage(SOAPMessage request) {
        Messaging messaging;
        try {
            messaging = getMessaging(request);
        } catch (SOAPException | JAXBException e) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Not possible to getMessage", e);
        }
        return messaging;
    }

}
