package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.InternalJmsMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

@RunWith(JMockit.class)
public class PriorityJmsTemplateTest {

    @Tested
    PriorityJmsTemplate priorityJmsTemplate;

    @Test
    public void doSendWithDefaultPriority(@Injectable MessageProducer producer,
                                          @Injectable Message message) throws JMSException {
        Integer defaultPriority = 4;
        Long timeToLive = 23L;

        new Expectations(priorityJmsTemplate) {{
            priorityJmsTemplate.getPriority();
            result = defaultPriority;

            priorityJmsTemplate.getDeliveryMode();
            result = DeliveryMode.PERSISTENT;

            priorityJmsTemplate.getTimeToLive();
            result = timeToLive;

        }};

        priorityJmsTemplate.doSend(producer, message);

        new Verifications() {{
            producer.send(message, DeliveryMode.PERSISTENT, defaultPriority, timeToLive);
        }};
    }

    @Test
    public void doSendWithMessagePriority(@Injectable MessageProducer producer,
                                          @Injectable Message message) throws JMSException {
        Integer defaultPriority = 4;
        Integer messagePriority = 5;
        Long timeToLive = 23L;

        new Expectations(priorityJmsTemplate) {{
            priorityJmsTemplate.getPriority();
            result = defaultPriority;

            message.getStringProperty(InternalJmsMessage.MESSAGE_PRIORITY_USED);
            result = "true";

            message.getJMSPriority();
            result = messagePriority;

            priorityJmsTemplate.getDeliveryMode();
            result = DeliveryMode.PERSISTENT;

            priorityJmsTemplate.getTimeToLive();
            result = timeToLive;

        }};

        priorityJmsTemplate.doSend(producer, message);

        new Verifications() {{
            producer.send(message, DeliveryMode.PERSISTENT, messagePriority, timeToLive);
        }};
    }
}