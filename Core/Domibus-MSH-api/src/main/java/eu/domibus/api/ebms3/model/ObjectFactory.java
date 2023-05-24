package eu.domibus.api.ebms3.model;


import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the
 * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704 package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 *
 * @author Apache CXF
 * @version 1.0
 * @since 3.0
 */

@SuppressWarnings("ConstantNamingConvention")
@XmlRegistry
public class ObjectFactory {

    public static final QName _Messaging_QNAME = new QName(
            "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
            "Messaging");

    public static final QName _UserMessage_QNAME = new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/",
            "UserMessage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package:
     * org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Ebms3Messaging }
     * @return a new instance of {@link Ebms3Messaging }
     */
    public Ebms3Messaging createMessaging() {
        return new Ebms3Messaging();
    }

    /**
     * Create an instance of {@link Ebms3MessageInfo }
     * @return a new instance of {@link Ebms3MessageInfo }
     */
    public Ebms3MessageInfo createMessageInfo() {
        return new Ebms3MessageInfo();
    }

    /**
     * Create an instance of {@link Ebms3Description }
     * @return a new instance of {@link Ebms3Description }
     */
    public Ebms3Description createDescription() {
        return new Ebms3Description();
    }

    /**
     * Create an instance of {@link Ebms3Service }
     * @return a new instance of {@link Ebms3Service }
     */
    public Ebms3Service createService() {
        return new Ebms3Service();
    }

    /**
     * Create an instance of {@link Ebms3PartyId }
     * @return a new instance of {@link Ebms3PartyId }
     */
    public Ebms3PartyId createPartyId() {
        return new Ebms3PartyId();
    }

    /**
     * Create an instance of {@link Ebms3CollaborationInfo }
     * @return a new instance of {@link Ebms3CollaborationInfo }
     */
    public Ebms3CollaborationInfo createCollaborationInfo() {
        return new Ebms3CollaborationInfo();
    }

    /**
     * Create an instance of {@link Ebms3To }
     * @return a new instance of {@link Ebms3To }
     */
    public Ebms3To createTo() {
        return new Ebms3To();
    }

    /**
     * Create an instance of {@link Ebms3PullRequest }
     * @return a new instance of {@link Ebms3PullRequest }
     */
    public Ebms3PullRequest createPullRequest() {
        return new Ebms3PullRequest();
    }

    /**
     * Create an instance of {@link Ebms3AgreementRef }
     * @return a new instance of {@link Ebms3AgreementRef }
     */
    public Ebms3AgreementRef createAgreementRef() {
        return new Ebms3AgreementRef();
    }

    /**
     * Create an instance of {@link Ebms3PartProperties }
     * @return a new instance of {@link Ebms3PartProperties }
     */
    public Ebms3PartProperties createPartProperties() {
        return new Ebms3PartProperties();
    }

    /**
     * Create an instance of {@link Ebms3Property }
     * @return a new instance of {@link Ebms3Property }
     */
    public Ebms3Property createProperty() {
        return new Ebms3Property();
    }

    /**
     * Create an instance of {@link Ebms3PartyInfo }
     * @return a new instance of {@link Ebms3PartyInfo }
     */
    public Ebms3PartyInfo createPartyInfo() {
        return new Ebms3PartyInfo();
    }

    /**
     * Create an instance of {@link Ebms3MessageProperties }
     * @return a new instance of {@link Ebms3MessageProperties }
     */
    public Ebms3MessageProperties createMessageProperties() {
        return new Ebms3MessageProperties();
    }

    /**
     * Create an instance of {@link Ebms3Error }
     * @return a new instance of {@link Ebms3Error }
     */
    public Ebms3Error createError() {
        return new Ebms3Error();
    }

    /**
     * Create an instance of {@link Ebms3PayloadInfo }
     * @return a new instance of {@link Ebms3PayloadInfo }
     */
    public Ebms3PayloadInfo createPayloadInfo() {
        return new Ebms3PayloadInfo();
    }

    /**
     * Create an instance of {@link Ebms3SignalMessage }
     * @return a new instance of {@link Ebms3SignalMessage }
     */
    public Ebms3SignalMessage createSignalMessage() {
        return new Ebms3SignalMessage();
    }

    /**
     * Create an instance of {@link Ebms3PartInfo }
     * @return a new instance of {@link Ebms3PartInfo }
     */
    public Ebms3PartInfo createPartInfo() {
        return new Ebms3PartInfo();
    }

    /**
     * Create an instance of {@link Ebms3UserMessage }
     * @return a new instance of {@link Ebms3UserMessage }
     */
    public Ebms3UserMessage createUserMessage() {
        return new Ebms3UserMessage();
    }

    /**
     * Create an instance of {@link Ebms3Receipt }
     * @return a new instance of {@link Ebms3Receipt }
     */
    public Ebms3Receipt createReceipt() {
        return new Ebms3Receipt();
    }

    /**
     * Create an instance of {@link Ebms3From }
     * @return a new instance of {@link Ebms3From }
     */
    public Ebms3From createFrom() {
        return new Ebms3From();
    }

    /**
     * Create an instance of
     * {@link JAXBElement }{@code <}{@link Ebms3Messaging }{@code >}}
     *
     * @param value the {@link Ebms3Messaging } value
     *
     * @return a new instance of {@link JAXBElement }{@code <}{@link Ebms3Messaging }{@code >}}
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", name = "Messaging")
    public JAXBElement<Ebms3Messaging> createMessaging(final Ebms3Messaging value) {
        return new JAXBElement<>(ObjectFactory._Messaging_QNAME, Ebms3Messaging.class, null, value);
    }

    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", name = "UserMessage")
    public JAXBElement<Ebms3UserMessage> createUserMessage(final Ebms3UserMessage value) {
        return new JAXBElement<>(ObjectFactory._UserMessage_QNAME, Ebms3UserMessage.class, null, value);
    }
}
