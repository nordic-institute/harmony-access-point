package eu.domibus.plugin.ws.webservice;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.generated.ListPendingMessagesFault;
import eu.domibus.plugin.ws.generated.body.ListPendingMessagesRequest;
import eu.domibus.plugin.ws.generated.body.ListPendingMessagesResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This JUNIT implements the Test cases List Pending Messages-01 and List Pending Messages-02.
 *
 * @author martifp
 */
public class PendingMessagesListIT extends AbstractBackendWSIT {

    @Autowired
    JMSManager jmsManager;

    @Test
    public void testListPendingMessagesNOk() throws ListPendingMessagesFault {
        ListPendingMessagesRequest request = new ListPendingMessagesRequest();
        ListPendingMessagesResponse response = webServicePluginInterface.listPendingMessages(request);

        // Verifies the response
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getMessageID().isEmpty());
    }

}
