package eu.domibus.plugin.webService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class TestUnmarshall {


    @Test
    public void testUnmarshall() throws SAXException, ParserConfigurationException, SOAPException, IOException {
        String filename = "noSignNoEncrypt.xml";
        SOAPMessage soapMessage = createSOAPMessage(filename);
        System.out.println(soapMessage);
    }


    public SOAPMessage createSOAPMessage(String dataset) throws SOAPException, IOException, ParserConfigurationException, SAXException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("dataset/as4/" + dataset);
        String responseAsString = IOUtils.toString(resourceAsStream, "UTF-8");
        responseAsString = StringUtils.replace(responseAsString, "SIGNAL_MESSAGE_ID_HOLDER", "123");
        InputSource inputSource = new InputSource(new StringReader(responseAsString));


        Document document = builder.parse(inputSource);
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        return message;
    }
}
