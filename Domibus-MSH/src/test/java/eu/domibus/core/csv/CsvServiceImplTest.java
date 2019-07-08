package eu.domibus.core.csv;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.model.logging.MessageLogInfo;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class CsvServiceImplTest {

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    CsvServiceImpl csvServiceImpl;

    @Test
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(new ArrayList<>(), null, null, null);

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(null, null, null, null);

        // Then
        Assert.assertTrue(exportToCSV.trim().isEmpty());
    }

    @Test
    public void testExportToCsv() throws CsvException {
        testExportCsvBySubtype(null);
    }

    @Test
    public void testExportToCsvTest() throws CsvException {
        testExportCsvBySubtype(MessageSubtype.TEST);
    }

    private void testExportCsvBySubtype(MessageSubtype messageSubtype) {
        // Given
        Date date = new Date();
        List<MessageLogInfo> messageLogInfoList = getMessageList(MessageType.USER_MESSAGE, date, messageSubtype);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'GMT'Z");
        ZonedDateTime d = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        String csvDate = d.format(f);

        // When
        final String exportToCSV = csvServiceImpl.exportToCSV(messageLogInfoList, MessageLogInfo.class, null, null);

        // Then
        Assert.assertTrue(exportToCSV.contains("Message Id,From Party Id,To Party Id,Message Status,Notification Status,Received,Msh Role,Send Attempts,Send Attempts Max,Next Attempt,Conversation Id,Message Type,Message Subtype,Deleted,Original Sender,Final Recipient,Ref To Message Id,Failed,Restored"));
        Assert.assertTrue(exportToCSV.contains("messageId,fromPartyId,toPartyId,ACKNOWLEDGED,NOTIFIED," + csvDate + ",RECEIVING,1,5," + csvDate + ",conversationId,USER_MESSAGE," + (messageSubtype != null ? messageSubtype.name() : "") + "," + csvDate + ",originalSender,finalRecipient,refToMessageId," + csvDate + "," + csvDate));
    }

    @Test
    public void testExportJmsToCsv() {
        List<JmsMessage> jmsMessageList = getJmsMessageList();

        final String exportToCSV = csvServiceImpl.exportToCSV(jmsMessageList, JmsMessage.class,
                CsvCustomColumns.JMS_RESOURCE.getCustomColumns(), CsvExcludedItems.JMS_RESOURCE.getExcludedItems());
        Assert.assertTrue(exportToCSV.contains("ID,JMS Type,Time,Content,Custom prop,JMS prop"));
        Assert.assertTrue(exportToCSV.contains("DOMAIN"));
        Assert.assertTrue(exportToCSV.contains("originalQueue"));
    }

    @Test
    public void testGetResponseEntity() {
        List<JmsMessage> jmsMessageList = getJmsMessageList();

        final String exportToCSV = csvServiceImpl.exportToCSV(jmsMessageList, JmsMessage.class,
                CsvCustomColumns.JMS_RESOURCE.getCustomColumns(), CsvExcludedItems.JMS_RESOURCE.getExcludedItems());

       final ResponseEntity<String> responseEntity = csvServiceImpl.getResponseEntity(exportToCSV, "test");
       Assert.assertNotNull(responseEntity);
       Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
       Assert.assertEquals(MediaType.parseMediaType(CsvServiceImpl.APPLICATION_EXCEL_STR), responseEntity.getHeaders().getContentType());
       Assert.assertEquals(exportToCSV, responseEntity.getBody());
    }

    private List<MessageLogInfo> getMessageList(MessageType messageType, Date date, MessageSubtype messageSubtype) {
        List<MessageLogInfo> result = new ArrayList<>();
        MessageLogInfo messageLog = new MessageLogInfo("messageId", MessageStatus.ACKNOWLEDGED,
                NotificationStatus.NOTIFIED, MSHRole.RECEIVING, messageType, date, date, 1, 5, date,
                "conversationId", "fromPartyId", "toPartyId", "originalSender", "finalRecipient",
                "refToMessageId", date, date, messageSubtype, false, false);
        result.add(messageLog);
        return result;
    }

    private List<JmsMessage> getJmsMessageList() {
        List<JmsMessage> result = new ArrayList<>();

        JmsMessage jmsMessage = new JmsMessage();
        jmsMessage.setId("ID:localhost-10762-1561728161168-6:48:4:1:1");
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("DOMAIN", "default");
        customProperties.put("dlqDeliveryFailureCause", "java.lang.Throwable: Delivery[1] exceeds redelivery policy limit:RedeliveryPolicy {destination = null, collisionAvoidanceFactor = 0.15, maximumRedeliveries = 0, maximumRedeliveryDelay = -1, initialRedeliveryDelay = 1000, useCollisionAvoidance = false, useExponentialBackOff = false, backOffMultiplier = 5.0, redeliveryDelay = 1000, preDispatchCheck = true}, cause:null");
        customProperties.put("originalExpiration", new Long(0));
        customProperties.put("originalQueue", "domibus.fsplugin.send.queue");
        customProperties.put("FILE_NAME", "/home/edelivery/domibus/fs_plugin_data/MAIN/OUT/test.txt");

        Map<String, Object> jmsProperties = new HashMap<>();
        jmsProperties.put("JMSMessageID", " -> ID:localhost-10762-1561728161168-6:48:4:1:1");
        jmsProperties.put("JMSDestination", "queue://domibus.DLQ");
        jmsProperties.put("JMSDeliveryMode", "PERSISTENT");

        jmsMessage.setCustomProperties(customProperties);
        jmsMessage.setProperties(jmsProperties);

        result.add(jmsMessage);

        return result;

    }
}
