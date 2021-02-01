package eu.domibus.sti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MapMessage;


public class JmsListenerReplyQueue {

    private static final Logger LOG = LoggerFactory.getLogger(JmsListenerReplyQueue.class);

    public JmsListenerReplyQueue() {
    }

    @org.springframework.jms.annotation.JmsListener(containerFactory = "${jms.connectionFactory}", destination = "${jms.reply.queue}", concurrency = "${jms.listener.concurrency}")
    public void receiveReplyMessage(MapMessage msj) {
        LOG.info("Consume reply message:[{}]", msj);
    }
}
