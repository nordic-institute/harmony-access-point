package eu.domibus.ext.services;

import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;

public interface MessageSubmitterExtService {
    /**
     * Submits a message to Domibus to be processed.
     *
     * @param messageData the message to be processed
     * @param submitterName the name of the submitter
     * @return the messageId of the submitted message
     * @throws MessagingProcessingException if the message was rejected by the Domibus MSH
     */
    String submit(Submission messageData, String submitterName) throws MessagingProcessingException;
}
