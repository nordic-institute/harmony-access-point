package eu.domibus.plugin;


import eu.domibus.AbstractBackendWSIT;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.SubmitRequest;
import eu.domibus.plugin.ws.generated.body.SubmitResponse;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
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
public class DeleteMessageIT extends AbstractBackendWSIT {

    @PersistenceContext(unitName = "domibusJTA")
    private EntityManager entityManager;

    @Autowired
    MessageRetentionDefaultService messageRetentionService;

    @Autowired
    Provider<SOAPMessage> mshWebserviceTest;

    private static final List<String> tablesToExclude = new ArrayList<>(Arrays.asList(
            "TB_EVENT",
            "TB_EVENT_ALERT",
            "TB_EVENT_PROPERTY",
            "TB_ALERT"
    ));

    /**
     * Test to delete a sent failed message
     */
    @Test
    public void testDeleteFailedMessage() throws SubmitMessageFault, IOException, XmlProcessingException {
        updatePmodeForSendFailure();
        sendDeleteMessage(MessageStatus.SEND_FAILURE);
    }

    /**
     * Test to delete a sent success message
     */
    @Test
    public void testDeleteSentMessage() throws SubmitMessageFault, IOException, XmlProcessingException {
        updatePmodeForAcknowledged();
        sendDeleteMessage(MessageStatus.ACKNOWLEDGED);
    }

    /**
     * Test to delete a received message
     */
    @Test
    public void testReceiveDeleteMessage() throws SOAPException, IOException, ParserConfigurationException, SAXException, XmlProcessingException {
        uploadPmode(wireMockRule.port());
        receiveDeleteMessage();
    }

    protected void receiveDeleteMessage() throws SOAPException, IOException, ParserConfigurationException, SAXException{
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";

        Map<String, Integer> initialMap = createTableRownumMap();
        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebserviceTest.invoke(soapMessage);

        waitUntilMessageIsReceived(messageId);

        Map<String, Integer> beforeDeletionMap = createTableRownumMap();

        messageRetentionService.deleteExpiredMessages();

        Map<String, Integer> finalMap = createTableRownumMap();

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }

    protected void updatePmodeForAcknowledged() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("security=\"eDeliveryAS4Policy\"", "security=\"noSigNoEnc\"");
        uploadPmode(wireMockRule.port(), toReplace);
    }

    protected void updatePmodeForSendFailure() throws IOException, XmlProcessingException {
        Map<String, String> toReplace = new HashMap<>();
        toReplace.put("retry=\"12;4;CONSTANT\"", "retry=\"1;0;CONSTANT\"");
        uploadPmode(wireMockRule.port(), toReplace);
    }

    protected void sendDeleteMessage(MessageStatus status) throws SubmitMessageFault {
        String payloadHref = "cid:message";
        SubmitRequest submitRequest = createSubmitRequestWs(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeaderWs(payloadHref);

        Map<String, Integer> initialMap = createTableRownumMap();

        super.prepareSendMessage("validAS4Response.xml");
        SubmitResponse response = webServicePluginInterface.submitMessage(submitRequest, ebMSHeaderInfo);

        final List<String> messageID = response.getMessageID();
        assertNotNull(response);
        assertNotNull(messageID);
        assertEquals(1, messageID.size());
        final String messageId = messageID.iterator().next();

        waitUntilMessageHasStatus(messageId, status);

        Map<String, Integer> beforeDeletionMap = createTableRownumMap();

        verify(postRequestedFor(urlMatching("/domibus/services/msh")));

        messageRetentionService.deleteExpiredMessages();

        Map<String, Integer> finalMap = createTableRownumMap();

        Assert.assertTrue(initialMap.size() > 0);
        Assert.assertTrue(beforeDeletionMap.size() > 0);
        Assert.assertTrue(CollectionUtils.isEqualCollection(initialMap.entrySet(), finalMap.entrySet()));
        Assert.assertFalse(CollectionUtils.isEqualCollection(initialMap.entrySet(), beforeDeletionMap.entrySet()));
    }

    protected Map<String, Integer> createTableRownumMap() {
        Map<String, Integer> rownums = new HashMap<>();
        Query query = entityManager.createNativeQuery("SELECT table_name FROM INFORMATION_SCHEMA.TABLES where table_name like 'TB_%';");
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
        String selectStr = "select count(*) from " + tableName +";";
        Query query = entityManager.createNativeQuery(selectStr);
        BigInteger counter = (BigInteger)query.getSingleResult();

        return counter.intValue();
    }
}
