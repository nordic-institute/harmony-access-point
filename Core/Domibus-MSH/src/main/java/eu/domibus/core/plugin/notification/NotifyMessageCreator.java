package eu.domibus.core.plugin.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.MessageEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class NotifyMessageCreator {

    private final  MSHRole mshRole;
    private NotificationType notificationType;
    private Map<String, String> properties;
    private final ObjectMapper objectMapper;


    public NotifyMessageCreator(MSHRole mshRole,
                                final NotificationType notificationType, final Map<String, String> properties,
                                final ObjectMapper objectMapper) {
        this.notificationType = notificationType;
        this.properties = properties;
        this.mshRole = mshRole;
        this.objectMapper = objectMapper;
    }


    public JmsMessage createMessage(MessageEvent messageEvent) {
        final JMSMessageBuilder jmsMessageBuilder = JMSMessageBuilder.create();
        if (properties != null) {
            jmsMessageBuilder.properties(new HashMap<>(properties));
        }

        jmsMessageBuilder.property(MessageConstants.MESSAGE_ENTITY_ID, String.valueOf(messageEvent.getMessageEntityId()));
        jmsMessageBuilder.property(MessageConstants.MESSAGE_ID, messageEvent.getMessageId());
        jmsMessageBuilder.property(MessageConstants.MSH_ROLE, mshRole!=null ? mshRole.name() : null);
        jmsMessageBuilder.property(MessageConstants.NOTIFICATION_TYPE, notificationType.name());
        String eventSerialized = null;
        try {
            eventSerialized = objectMapper.writeValueAsString(messageEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        jmsMessageBuilder.content(eventSerialized);
        jmsMessageBuilder.property(AsyncNotificationConfiguration.BODY, eventSerialized);
        jmsMessageBuilder.property(AsyncNotificationConfiguration.EVENT_CLASS, messageEvent.getClass().getName());

        return jmsMessageBuilder.build();
    }

    public MSHRole getMshRole() {
        return mshRole;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }
}
