package eu.domibus.test.common;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.pmode.PModeConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

@Service
public class SoapSampleUtil {

    public SOAPMessage createSOAPMessage(String dataset, String messageId) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        String datasetString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dataset/as4/" + dataset), StandardCharsets.UTF_8);
        datasetString = StringUtils.replace(datasetString, "MESSAGE_ID", messageId);

        Document document = builder.parse(new ByteArrayInputStream(datasetString.getBytes(StandardCharsets.UTF_8)));
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        AttachmentPart attachment = message.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("cid:message");
        message.addAttachmentPart(attachment);

        String pModeKey = composePModeKey("blue_gw", "red_gw", "testService1", "tc4Action", "", "pushTestcase1tc4Action");

        message.setProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        message.setProperty(DomainContextProvider.HEADER_DOMIBUS_DOMAIN, DomainService.DEFAULT_DOMAIN.getCode());

        return message;
    }

    public SoapMessage createSoapMessage(String filename, String messageId) throws IOException {
        String datasetString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("dataset/as4/" + filename), StandardCharsets.UTF_8);
        datasetString = StringUtils.replace(datasetString, "MESSAGE_ID", messageId);
        Reader reader = new StringReader(datasetString);
        XMLStreamReader xmlReader = null;
        XMLInputFactory factory = XMLInputFactory.newInstance(); // Or newFactory()
        try {
            xmlReader = factory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
           throw new IllegalArgumentException("Could not create XML", e);
        }

        SoapMessage soapMessage = new SoapMessage(Soap11.getInstance());
        soapMessage.setContent(XMLStreamReader.class, xmlReader);
        soapMessage.setInterceptorChain(new PhaseInterceptorChain(new TreeSet<>()));
        soapMessage.setExchange(new ExchangeImpl());
        return soapMessage;
    }

    public String composePModeKey(final String senderParty, final String receiverParty, final String service,
                                  final String action, final String agreement, final String legName) {
        return StringUtils.joinWith(PModeConstants.PMODEKEY_SEPARATOR, senderParty,
                receiverParty, service, action, agreement, legName);
    }
}
