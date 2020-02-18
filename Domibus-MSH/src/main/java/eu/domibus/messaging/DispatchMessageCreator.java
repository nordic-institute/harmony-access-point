package eu.domibus.messaging;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;


/**
 * @author Christian Koch, Stefan Mueller
 */
public class DispatchMessageCreator {

    private final String messageId;
    private final Long messageIdPk;

    public DispatchMessageCreator(final String messageId, Long messageIdPk) {
        this.messageId = messageId;
        this.messageIdPk = messageIdPk;
    }

    public JmsMessage createMessage() {

        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.MESSAGE_ID_PK, messageIdPk)
                .build();
    }

    public JmsMessage createMessage(final int retryCount) {
        return JMSMessageBuilder
                .create()
                .property(MessageConstants.MESSAGE_ID, messageId)
                .property(MessageConstants.MESSAGE_ID_PK, messageIdPk)
                .property(MessageConstants.RETRY_COUNT, retryCount)
                .build();
    }

}
