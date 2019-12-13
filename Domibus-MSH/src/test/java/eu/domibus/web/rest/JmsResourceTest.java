package eu.domibus.web.rest;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.services.AuditService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.web.rest.ro.*;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class JmsResourceTest {

    @Tested
    JmsResource jmsResource;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    private AuditService auditService;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Test
    public void testDestinations() {
        // Given
        final SortedMap<String, JMSDestination> resultMap = new TreeMap<>();
        JMSDestination destination1 = new JMSDestination();
        destination1.setName("destination1");
        resultMap.put("test1", destination1);
        new Expectations() {{
            jmsManager.getDestinations();
            result = resultMap;
        }};

        // When
        ResponseEntity<DestinationsResponseRO> responseEntity = jmsResource.destinations();
        DestinationsResponseRO destinations = responseEntity.getBody();

        // Then
        Assert.assertNotNull(destinations);
        Assert.assertEquals(1, destinations.getJmsDestinations().size());
        JMSDestination jmsDestinationTest1 = destinations.getJmsDestinations().get("test1");
        Assert.assertEquals("destination1", jmsDestinationTest1.getName());
    }

    @Test
    public void testMessages() {
        // Given
        JmsFilterRequestRO requestRO = new JmsFilterRequestRO();

        final List<JmsMessage> jmsMessageList = new ArrayList<>();
        JmsMessage jmsMessage = new JmsMessage();
        jmsMessage.setId("jmsMessageId");
        jmsMessage.setType("type1");
        jmsMessage.setContent("content1");
        jmsMessage.setProperty("prop1", "value1");
        jmsMessage.setTimestamp(new Date());
        jmsMessageList.add(jmsMessage);

        new Expectations() {{
            jmsManager.browseMessages(anyString, anyString, (Date) any, (Date) any, anyString);
            result = jmsMessageList;
        }};

        // When
        ResponseEntity<MessagesResponseRO> messages = jmsResource.messages(requestRO);

        // Then
        Assert.assertNotNull(messages);
        Assert.assertEquals(jmsMessageList, messages.getBody().getMessages());
    }

    @Test
    public void testActionMoveNoValidParam() {
        // Given
        SortedMap<String, JMSDestination> dests = new TreeMap<>();
        dests.put("domibus.queue1", new JMSDestination());
        new Expectations() {{
            jmsManager.getDestinations();
            result = dests;
        }};

        List<String> selectedMessages = new ArrayList<>();
        selectedMessages.add("message1");
        MessagesActionRequestRO request = new MessagesActionRequestRO();
        request.setAction(MessagesActionRequestRO.Action.MOVE);
        request.setSource("source1");
        request.setDestination("domibus.queue2");
        request.setSelectedMessages(selectedMessages);

        // When
        ResponseEntity<MessagesActionResponseRO> responseEntity = jmsResource.action(request);

        // Then
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testActionMove() {
        SortedMap<String, JMSDestination> dests = new TreeMap<>();
        dests.put("domibus.queue1", new JMSDestination() {{ setName("domibus.queue1"); }});
        new Expectations() {{
            jmsManager.getDestinations();
            result = dests;
        }};
        testAction(MessagesActionRequestRO.Action.MOVE);
    }

    @Test
    public void testActionRemove() {
        testAction(MessagesActionRequestRO.Action.REMOVE);
    }

    private void testAction(MessagesActionRequestRO.Action action) {
        // Given
        List<String> selectedMessages = new ArrayList<>();
        selectedMessages.add("message1");
        MessagesActionRequestRO request = new MessagesActionRequestRO();
        request.setAction(action);
        request.setSource("source1");
        request.setDestination("domibus.queue1");
        request.setSelectedMessages(selectedMessages);

        // When
        ResponseEntity<MessagesActionResponseRO> responseEntity = jmsResource.action(request);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals("application/json", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertEquals("Success", responseEntity.getBody().getOutcome());
    }

    @Test
    public void testActionException() {
        // Given
        MessagesActionRequestRO request = new MessagesActionRequestRO();
        request.setSelectedMessages(null); // force runtime exception

        // When
        ResponseEntity<MessagesActionResponseRO> responseEntity = jmsResource.action(request);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assert.assertEquals("application/json", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertNull(responseEntity.getBody().getOutcome());
    }


//    @Test
//    public void testGetCsv() throws EbMS3Exception {
//        // Given
//        String source = "";
//        String jmsType = null;
//        Long fromDate = LocalDate.now().plusDays(-10).toEpochDay();
//        Long toDate = LocalDate.now().toEpochDay();
//        String selector = "";
//        JmsMessage jmsMessage = new JmsMessage();
//        List<JmsMessage> jmsMessageList = Arrays.asList(jmsMessage);
//        String mockCsvResult = "csv";
//
//        new Expectations(jmsResource) {{
//            jmsManager.browseMessages(source, jmsType, (Date) any, (Date) any, selector);
//            result = jmsMessageList;
//            csvServiceImpl.exportToCSV(jmsMessageList, JmsMessage.class, (Map<String, String>) any, (List<String>) any);
//            result = mockCsvResult;
//        }};
//
//        // When
//        final ResponseEntity<String> csv = jmsResource.getCsv(source, jmsType, fromDate, toDate, selector);
//
//        // Then
//        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
//        Assert.assertEquals(mockCsvResult, csv.getBody());
//    }

}
