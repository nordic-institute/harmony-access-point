package eu.domibus.test.ebms3;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

@Service
public class Ebms3MessagingDocumentParser {

    public Ebms3Messaging parseMessaging(InputStream inputStream, String namespacePrefix) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseFileDocument = documentBuilder.parse(inputStream);
        final Node messagingNode = responseFileDocument.getElementsByTagName(namespacePrefix + ":Messaging").item(0);
        return JAXBContext.newInstance(Ebms3Messaging.class).createUnmarshaller().unmarshal(messagingNode, Ebms3Messaging.class).getValue();
    }
}
