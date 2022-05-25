package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.notification.NotifyMessageCreator;
import eu.domibus.plugin.webService.generated.ListPendingMessagesResponse;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This JUNIT implements the Test cases List Pending Messages-01 and List Pending Messages-02.
 *
 * @author martifp
 * @deprecated to be removed when deprecated endpoint /backend is removed
 */
@Deprecated
public class PendingMessagesListIT extends AbstractBackendWSIT {

    @Autowired
    JMSManager jmsManager;


    @Test
    @Ignore("[EDELIVERY-8828] WSPLUGIN: tests for rest methods ignored")
    public void testListPendingMessagesOk() {
        Random random = new Random();
        List<Pair<Long, String>> messageIds = new ArrayList<>();
        messageIds.add(Pair.of(random.nextLong(), UUID.randomUUID()+"@domibus.eu"));
        messageIds.add(Pair.of(random.nextLong(), UUID.randomUUID()+"@domibus.eu"));
        messageIds.add(Pair.of(random.nextLong(), UUID.randomUUID()+"@domibus.eu"));

        for (Pair<Long, String> messageId : messageIds) {
            final JmsMessage message = new NotifyMessageCreator(messageId.getLeft(), messageId.getRight(), NotificationType.MESSAGE_RECEIVED, new HashMap<>()).createMessage();
            jmsManager.sendMessageToQueue(message, WS_NOT_QUEUE);
        }

        waitForMessages(3);
        String request = "<listPendingMessagesRequest></listPendingMessagesRequest>";
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);


        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertFalse(response.getMessageID().isEmpty());
        Assert.assertTrue(response.getMessageID().containsAll(messageIds.stream().map(Pair::getRight).collect(Collectors.toList())));

    }

    @Test
    @Ignore("[EDELIVERY-8828] WSPLUGIN: tests for rest methods ignored")
    public void testListPendingMessagesNOk() {

        String request = "<listPendingMessagesRequest>1</listPendingMessagesRequest>";
        ListPendingMessagesResponse response = backendWebService.listPendingMessages(request);

        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getMessageID().isEmpty());
    }

}
