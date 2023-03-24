package eu.domibus.test.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;

@Service
public class JMSMessageUtil {

    @Transactional
    public Message popQueueMessageWithTimeout(javax.jms.Connection connection, String queueName, long mSecs) throws Exception {
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);
        Message message = consumer.receive(mSecs);
        consumer.close();
        session.close();
        return message;
    }
}
