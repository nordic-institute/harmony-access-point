package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.services.MessageSubmitterExtService;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageSubmitter;
import org.springframework.stereotype.Service;

@Service
public class MessageSubmitterServiceDelegate implements MessageSubmitterExtService {

    private final MessageSubmitter messageSubmitter;

    public MessageSubmitterServiceDelegate(MessageSubmitter messageSubmitter) {
        this.messageSubmitter = messageSubmitter;
    }

    @Override
    public String submit(Submission messageData, String submitterName) throws MessagingProcessingException {
        return messageSubmitter.submit(messageData, submitterName);
    }
}
