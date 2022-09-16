package eu.domibus.core.plugin.notification;

import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;

import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator {

    private final Long messageEntityId;
    private final String messageId;
    private final  MSHRole mshRole;
    private NotificationType notificationType;
    private Map<String, String> properties;

    public NotifyMessageCreator(final long messageEntityId, final String messageId, MSHRole mshRole,
                                final NotificationType notificationType, final Map<String, String> properties) {
        this.messageEntityId = messageEntityId;
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.properties = properties;
        this.mshRole = mshRole;
    }

    public JmsMessage createMessage() {
        final JMSMessageBuilder jmsMessageBuilder = JMSMessageBuilder.create();
        if (properties != null) {
            jmsMessageBuilder.properties(properties);
        }
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ENTITY_ID, String.valueOf(messageEntityId));
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ID, messageId);
        jmsMessageBuilder.property(MessageConstants.MSH_ROLE, mshRole.name());
        jmsMessageBuilder.property(MessageConstants.NOTIFICATION_TYPE, notificationType.name());

        return jmsMessageBuilder.build();
    }

    public Long getMessageEntityId() {
        return messageEntityId;
    }

    public String getMessageId() {
        return messageId;
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
