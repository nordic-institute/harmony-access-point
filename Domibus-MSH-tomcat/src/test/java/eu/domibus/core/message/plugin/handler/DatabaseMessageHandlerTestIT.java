package eu.domibus.core.message.plugin.handler;

import eu.domibus.AbstractIT;
import eu.domibus.core.plugin.handler.DatabaseMessageHandler;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DatabaseMessageHandlerTestIT extends AbstractIT {

    @Autowired
    DatabaseMessageHandler databaseMessageHandler;

    @Test
    public void submit() throws MessagingProcessingException {
        Submission submission = new Submission();
        databaseMessageHandler.submit(submission, "mybackend");

    }
}
