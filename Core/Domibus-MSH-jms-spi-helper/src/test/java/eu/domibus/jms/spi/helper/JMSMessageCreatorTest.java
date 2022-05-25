package eu.domibus.jms.spi.helper;

import eu.domibus.jms.spi.InternalJmsMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSMessageCreatorTest {

    @Tested
    JmsMessageCreator jmsMessageCreator;

    @Test
    public void testCreateMessage(@Injectable final Session session,
                                  @Injectable Message message,
                                  @Injectable InternalJmsMessage internalJmsMessage) throws JMSException {
        int priority = 5;

        new Expectations(jmsMessageCreator) {{
            jmsMessageCreator.doCreateMessage(session);
            result = message;

            internalJmsMessage.getPriority();
            this.result = priority;
        }};

        jmsMessageCreator.createMessage(session);

        new Verifications() {{
            message.setJMSPriority(priority);
            message.setStringProperty(InternalJmsMessage.MESSAGE_PRIORITY_USED, "true");
        }};
    }

    @Test
    public void testCreateTextMessage(@Injectable final Session session,
                                      @Injectable final TextMessage result,
                                      @Injectable InternalJmsMessage internalJmsMessage) throws Exception {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("key1", "value1");

        new Expectations() {{
            internalJmsMessage.getContent();
            result = "mycontent";

            internalJmsMessage.getType();
            result = "mytype";

            internalJmsMessage.getCustomProperties();
            result = properties;
        }};

        jmsMessageCreator.createTextMessage(session);

        new Verifications() {{
            result.setText("mycontent");
            result.setJMSType("mytype");
            result.setStringProperty("key1", "value1");
        }};
    }
}
