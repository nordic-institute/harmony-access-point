package eu.domibus.core.jms;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.messaging.MessageConstants;


/**
 * @author Christian Koch, Stefan Mueller
 */
public class DispatchMessageCreator {

    private final String messageId;

    public DispatchMessageCreator(final String messageId) {
        this.messageId = messageId;
    }

    public JmsMessage createMessage() {

        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .build();
    }

    public JmsMessage createMessage(final int retryCount) {

        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.RETRY_COUNT, String.valueOf(retryCount))
                .build();
    }

}
