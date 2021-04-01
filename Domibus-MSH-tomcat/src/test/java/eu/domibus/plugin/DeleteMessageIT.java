package eu.domibus.plugin;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 5.0
 */
@DirtiesContext
@Rollback
public abstract class DeleteMessageIT extends AbstractBackendWSIT {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    private static List<String> tablesToExclude;

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

        Map<String, Integer> initialMap = getTableCounts();
        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);
    }

    protected void deleteMessages() {
        messageRetentionService.deleteExpiredMessages();
    }

    protected void sendMessageToDelete(MessageStatus status) throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequestWs(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeaderWs(payloadHref);

        Map<String, Integer> initialMap = getTableCounts();

        super.prepareSendMessage("validAS4Response.xml");
        SubmitResponse response = webServicePluginInterface.submitMessage(submitRequest, ebMSHeaderInfo);

        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        waitUntilMessageHasStatus(messageId, status);

        verify(postRequestedFor(urlMatching("/domibus/services/msh")));

    }

    protected Map<String, Integer> getTableCounts() {
        Map<String, Integer> rownums = new HashMap<>();
        Query query = entityManager.createNativeQuery("SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_name LIKE 'TB_%'");
        try {
            List<String> tableNames = query.getResultList();
            tableNames.stream().forEach(tableName -> rownums.put(tableName, getCounter(tableName)));
        } catch (NoResultException nrEx) {
            return null;
        }
        tablesToExclude.stream().forEach(tableName -> rownums.remove(tableName));
        return rownums;
    }

    protected Integer getCounter(String tableName) {
        String selectStr = "SELECT count(*) from " + tableName;
        Query query = entityManager.createNativeQuery(selectStr);
        BigInteger counter = (BigInteger)query.getSingleResult();

        return counter.intValue();
    }
}
