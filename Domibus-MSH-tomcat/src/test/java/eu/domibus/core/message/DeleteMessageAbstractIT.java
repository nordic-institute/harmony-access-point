package eu.domibus.core.message;


import eu.domibus.AbstractIT;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.plugin.BackendConnectorProvider;
import eu.domibus.test.common.MessageDBUtil;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author idragusa
 * @since 5.0
 */
public abstract class DeleteMessageAbstractIT extends AbstractIT {

    @Autowired
    protected BackendConnectorProvider backendConnectorProvider;

    @Autowired
    protected MessageRetentionDefaultService messageRetentionService;

    @Autowired
    protected MSHWebservice mshWebserviceTest;

    @Autowired
    protected MessageDBUtil messageDBUtil;

    @Autowired
    protected SoapSampleUtil soapSampleUtil;

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
        String filename = "SOAPMessage4.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
    }

    protected void deleteMessages() {
        messageRetentionService.deleteExpiredMessages();
    }

}
