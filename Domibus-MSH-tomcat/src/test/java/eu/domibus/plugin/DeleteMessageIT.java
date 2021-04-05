package eu.domibus.plugin;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.common.MessageDBUtil;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author idragusa
 * @since 5.0
 */
@DirtiesContext
@Rollback
public abstract class DeleteMessageIT extends AbstractBackendWSIT {

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    @Autowired
    MessageDBUtil messageDBUtil;

    protected static List<String> tablesToExclude;

    @BeforeClass
    public static void initTablesToExclude() {
        tablesToExclude = new ArrayList<>(Arrays.asList(
                "TB_EVENT",
                "TB_EVENT_ALERT",
                "TB_EVENT_PROPERTY",
                "TB_ALERT"
        ));
    }

    protected void receiveMessageToDelete() throws SOAPException, IOException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
    }

    protected void deleteMessages() {
        messageRetentionService.deleteExpiredMessages();
    }
}
