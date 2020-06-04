package eu.domibus.web.rest;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.ro.*;
import mockit.*;
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
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class JmsResourceTest {

    public static final List<String> MESSAGES_IDS = Arrays.asList("message1", "message2");
    public static final String DOMIBUS_QUEUE_1 = "domibus.queue1";
    public static final String SOURCE_1 = "source1";

    @Mocked
    private MessagesActionRequestRO messagesActionRequestRO;

    @Tested
    private JmsResource jmsResource;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private AuditService auditService;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Injectable
    private ErrorHandlerService errorHandlerService;

    @Test
    public void testDestinations() {
        SortedMap<String, JMSDestination> dests = new TreeMap<>();
        // Given
        new Expectations() {{
            jmsManager.getDestinations();
            result = dests;
        }};

        // When
        DestinationsResponseRO destinations = jmsResource.destinations();

        // Then
        Assert.assertNotNull(destinations);
        Assert.assertEquals(dests, destinations.getJmsDestinations());

        new FullVerifications() {
        };
    }

    @Test
    public void testMessages(final @Mocked JmsFilterRequestRO requestRO) {
        // Given
        final List<JmsMessage> jmsMessageList = new ArrayList<>();

        new Expectations() {{
            requestRO.getSource();
            times = 1;
            requestRO.getJmsType();
            times = 1;
            requestRO.getFromDate();
            times = 1;
            requestRO.getToDate();
            times = 1;
            requestRO.getSelector();
            times = 1;
            jmsManager.browseMessages(anyString, anyString, (Date) any, (Date) any, anyString);
            result = jmsMessageList;
        }};

        // When
        MessagesResponseRO messages = jmsResource.messages(requestRO);

        // Then
        Assert.assertNotNull(messages);
        Assert.assertEquals(jmsMessageList, messages.getMessages());
        new FullVerifications() {
        };
    }

    @Test
    public void testAction_BlankIds() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = Arrays.asList("", "");
            times = 1;
        }};

        // When
        try {
            jmsResource.action(messagesActionRequestRO);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            //do nothing
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testAction_nullIds() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = Arrays.asList(null, null);
            times = 1;
        }};

        // When
        try {
            jmsResource.action(messagesActionRequestRO);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            //do nothing
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testAction_noId() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = new ArrayList<>();
            times = 1;
        }};

        // When
        try {
            jmsResource.action(messagesActionRequestRO);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            //do nothing
        }

        new FullVerifications() {
        };
    }

    @Test
    public void testActionMove_ok(final @Mocked SortedMap<String, JMSDestination> dests,
                                  final @Mocked JMSDestination queue1,
                                  final @Mocked JMSDestination queue2) {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = MESSAGES_IDS;

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.MOVE;

            messagesActionRequestRO.getDestination();
            result = DOMIBUS_QUEUE_1;

            messagesActionRequestRO.getSource();
            result = SOURCE_1;

            jmsManager.getDestinations();
            result = dests;

            dests.values();
            result = Arrays.asList(queue2, queue1);

            queue1.getName();
            result = DOMIBUS_QUEUE_1;

            queue2.getName();
            result = "domibus.queue2";
        }};
        // When
        MessagesActionResponseRO responseEntity = jmsResource.action(messagesActionRequestRO);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals("Success", responseEntity.getOutcome());

        new FullVerifications() {{
            jmsManager.moveMessages(SOURCE_1, DOMIBUS_QUEUE_1, MESSAGES_IDS.toArray(new String[0]));
            times = 1;
        }};
    }

    @Test
    public void testActionMove_wrongQueue(final @Mocked SortedMap<String, JMSDestination> dests,
                                          final @Mocked JMSDestination queue2) {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = MESSAGES_IDS;

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.MOVE;

            messagesActionRequestRO.getDestination();
            result = DOMIBUS_QUEUE_1;

            dests.values();
            result = Collections.singletonList(queue2);

            queue2.getName();
            result = "domibus.queue2.not.found";

            jmsManager.getDestinations();
            result = dests;
        }};

        //When
        try {
            jmsResource.action(messagesActionRequestRO);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            //do nothing
        }
        //Then
        new FullVerifications() {
        };
    }

    @Test
    public void testActionRemove() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = MESSAGES_IDS;

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.REMOVE;

            messagesActionRequestRO.getSource();
            result = "source1";
        }};

        // When
        MessagesActionResponseRO responseEntity = jmsResource.action(messagesActionRequestRO);

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals("Success", responseEntity.getOutcome());

        new FullVerifications() {{
            jmsManager.deleteMessages(anyString, (String[]) any);
            times = 1;
        }};
    }

    @Test
    public void testActionRemove_InternalJMSException() {
        // Given
        new Expectations() {{
            messagesActionRequestRO.getSelectedMessages();
            result = Arrays.asList("message1", "message2");

            messagesActionRequestRO.getAction();
            result = MessagesActionRequestRO.Action.REMOVE;

            messagesActionRequestRO.getSource();
            result = "source1";

            jmsManager.deleteMessages(anyString, (String[]) any);
            times = 1;
            result = new InternalJMSException();
        }};
        // When
        try {
            jmsResource.action(messagesActionRequestRO);
        } catch (InternalJMSException e) {
            //Do nothing
        }
        // Then
        new FullVerifications() {
        };
    }

    @Test
    public void testGetCsv(@Injectable JmsMessage jmsMessage) {
        // Given
        String source = "source";
        String jmsType = null;
        String selector = "selector";
        List<JmsMessage> jmsMessageList = new ArrayList<>();
        String mockCsvResult = "csv";

        new Expectations(jmsResource) {{
            jmsManager.browseMessages(source, jmsType, (Date) any, (Date) any, selector);
            result = jmsMessageList;
            csvServiceImpl.exportToCSV(jmsMessageList, JmsMessage.class, (Map<String, String>) any, (List<String>) any);
            result = mockCsvResult;
        }};

        // When
        final ResponseEntity<String> csv = jmsResource.getCsv(new JmsFilterRequestRO() {{
            setSource(source);
            setSelector(selector);
            setJmsType(jmsType);
        }});

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(mockCsvResult, csv.getBody());
    }

}
