package eu.domibus.core.ebms3.sender;

import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.jms.Message;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */
@Service
public class MessageSenderListener extends AbstractMessageSenderListener {
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderListener.class);

    /**
     * Method called when dealing with outgoing messages
     * There is a timeout configured on the dispatch queue by the domibus property domibus.dispatcher.timeout=300
     * @param message the message to send
     */
    @Override
    @Timer(clazz = MessageSenderListener.class,value="onMessage")
    @Counter(clazz = MessageSenderListener.class,value="onMessage")
    public void onMessage(final Message message) {
        LOG.debug("Processing message [{}]", message);
        super.onMessage(message);
    }

    @Override
    public void sendUserMessage(String messageId, Long messageEntityId, int retryCount) {
        super.messageSenderService.sendUserMessage(messageId, messageEntityId, retryCount);
    }

    public IDomibusLogger getLogger() {
        return LOG;
    }
}
