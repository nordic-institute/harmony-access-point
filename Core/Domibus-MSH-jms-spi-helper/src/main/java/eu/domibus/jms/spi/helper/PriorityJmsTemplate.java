package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

/**
 * Custom JMS Template implementation that uses priority set on the JMS Message
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class PriorityJmsTemplate extends JmsTemplate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PriorityJmsTemplate.class);

    @Override
    protected void doSend(MessageProducer producer, Message message) throws JMSException {
        //default priority
        int priority = getPriority();
        String messagePriorityUsed = message.getStringProperty(InternalJmsMessage.MESSAGE_PRIORITY_USED);

        if (BooleanUtils.toBoolean(messagePriorityUsed)) {
            int messagePriority = message.getJMSPriority();
            LOG.trace("Using message priority [{}]", messagePriority);
            priority = messagePriority;
        }
        LOG.debug("Sending message with priority [{}]", priority);
        producer.send(message, getDeliveryMode(), priority, getTimeToLive());
    }
}