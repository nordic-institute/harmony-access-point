package eu.domibus.ebms3.sender;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */
@Service(value = "messageSenderListener")
public class MessageSenderListener extends AbstractMessageSenderListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderListener.class);

    @Autowired
    private MetricRegistry metricRegistry;

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Override
    public void onMessage(final Message message) {
        com.codahale.metrics.Counter methodCounter = metricRegistry.counter(MetricRegistry.name(MessageSenderListener.class, "message_sender_consumer_counter"));
        com.codahale.metrics.Timer.Context on_message = metricRegistry.timer(MetricRegistry.name(MessageSenderListener.class, "on_message")).time();
        try {
            methodCounter.inc();
            com.codahale.metrics.Timer.Context before_on_message = metricRegistry.timer(MetricRegistry.name(MessageSenderListener.class, "before_on_message")).time();
            LOG.debug("Processing message [{}]", message);
            before_on_message.stop();

            super.onMessage(message);
        } finally {
            methodCounter.dec();
            on_message.stop();
        }
    }

    @Override
    public void scheduleSending(String messageId, Long delay) {
        super.userMessageService.scheduleSending(messageId, delay, false);
    }

    @Override
    public void sendUserMessage(String messageId, int retryCount) {
        super.messageSenderService.sendUserMessage(messageId, retryCount, false);
    }

}
