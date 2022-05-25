
package eu.domibus.plugin.jms;

import eu.domibus.common.NotificationType;
import eu.domibus.ext.domain.JMSMessageDTOBuilder;
import eu.domibus.ext.domain.JmsMessageDTO;

import static eu.domibus.plugin.jms.JMSMessageConstants.*;

/**
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public class SignalMessageCreator  {
    private NotificationType notificationType;
    private String messageId;
    private long messageEntityId;

    public SignalMessageCreator(long messageEntityId, String messageId, NotificationType notificationType) {
        this.messageId = messageId;
        this.notificationType = notificationType;
        this.messageEntityId = messageEntityId;
    }


    public JmsMessageDTO createMessage() {
        final JMSMessageDTOBuilder jmsMessageBuilder = JMSMessageDTOBuilder.create();
        String messageType = null;
        if (this.notificationType == NotificationType.MESSAGE_SEND_SUCCESS) {
            messageType = JMSMessageConstants.MESSAGE_TYPE_SEND_SUCCESS;
        }
        jmsMessageBuilder.property(JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, messageType);
        jmsMessageBuilder.property(MESSAGE_ID, messageId);
        jmsMessageBuilder.property(MESSAGE_ENTITY_ID, messageEntityId);
        return jmsMessageBuilder.build();
    }
}
