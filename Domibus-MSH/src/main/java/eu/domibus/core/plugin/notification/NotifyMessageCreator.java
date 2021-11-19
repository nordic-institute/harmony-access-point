package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;

import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator {

    private final long messageEntityId;
    private final String messageId;
    private NotificationType notificationType;
    private Map<String, String> properties;

    public NotifyMessageCreator(final long messageEntityId, final String messageId, final NotificationType notificationType, final Map<String, String> properties) {
        this.messageEntityId = messageEntityId;
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.properties = properties;
    }

    public JmsMessage createMessage() {
        final JMSMessageBuilder jmsMessageBuilder = JMSMessageBuilder.create();
        if (properties != null) {
            jmsMessageBuilder.properties(properties);
        }
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ENTITY_ID, String.valueOf(messageEntityId));
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ID, messageId);
        jmsMessageBuilder.property(MessageConstants.NOTIFICATION_TYPE, notificationType.name());

        return jmsMessageBuilder.build();
    }

    public long getMessageEntityId() {
        return messageEntityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
