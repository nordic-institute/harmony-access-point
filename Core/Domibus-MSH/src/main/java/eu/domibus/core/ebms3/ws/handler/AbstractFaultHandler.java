package eu.domibus.core.ebms3.ws.handler;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.ObjectFactory;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.NoSuchElementException;

/**
 * Generic handler for SOAP Faults in context of ebMS3
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public abstract class AbstractFaultHandler implements SOAPHandler<SOAPMessageContext> {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractFaultHandler.class);

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @Autowired
    protected XMLUtil xmlUtil;

    /**
     * This method extracts a ebMS3 messaging header {@link Ebms3Messaging} from a {@link javax.xml.soap.SOAPMessage}
     * It is possible that the Messaging header is missing:
     *
     *      Reporting with Fault Sending: An MSH may generate a SOAP Fault for reporting ebMS processing errors of severity "failure",
     *      which prevent further message processing. This Fault must comply with SOAP Fault processing, i.e. be sent back as an HTTP
     *      response in case the message in error was over an HTTP request. In case of ebMS processing errors (see Section 6.7.1), the Fault
     *      message MUST also include the eb:SignalMessage/eb:Error element in the eb:Messaging header.
     *
     * @param soapMessage the SOAP message
     * @return a ebMS3 messaging header {@link Ebms3Messaging}
     */
    protected Ebms3Messaging extractMessaging(final SOAPMessage soapMessage) {
        Ebms3Messaging ebms3Messaging = null;
        try {
            Node node = (Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
            XMLStreamReader reader = xmlUtil.getXmlStreamReaderFromNode(node);

            ebms3Messaging = ((JAXBElement<Ebms3Messaging>) this.jaxbContext.createUnmarshaller().unmarshal(reader)).getValue();

            reader.close();
        } catch (JAXBException | SOAPException | NoSuchElementException | XMLStreamException | TransformerException e) {
            LOG.warn("Could not extract Messaging header from Soap Message.");
        }

        return ebms3Messaging;
    }
}
