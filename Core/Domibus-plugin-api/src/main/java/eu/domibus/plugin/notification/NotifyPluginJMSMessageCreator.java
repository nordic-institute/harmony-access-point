package eu.domibus.plugin.notification;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.messaging.MessageConstants;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class NotifyPluginJMSMessageCreator {

    private final String messageId;
    private NotificationType notificationType;
    private Map<String, Object> properties;

    public NotifyPluginJMSMessageCreator(final String messageId, final NotificationType notificationType, final Map<String, Object> properties) {
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.properties = properties;
    }

    public JmsMessageDTO createMessage() {
        final JMSMessageDTOBuilder jmsMessageBuilder = JMSMessageDTOBuilder.create();
        if (properties != null) {
            jmsMessageBuilder.properties(properties);
        }
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ID, messageId);
        jmsMessageBuilder.property(MessageConstants.NOTIFICATION_TYPE, notificationType.name());

        return jmsMessageBuilder.build();
    }

    public String getMessageId() {
        return messageId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
