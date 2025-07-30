package eu.domibus.core.ebms3.sender;

import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.persistence.EntityExistsException;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */
@Service
public class MessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderListener.class);

    @Autowired
    @Qualifier("messageSenderErrorHandler")
    protected MessageSenderErrorHandler messageSenderErrorHandler;

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
        try {
            super.onMessage(message);
        } catch (EntityExistsException e) {
            LOG.warn("Message already sent", e);
        } catch (Throwable t) {
            messageSenderErrorHandler.handleError(t);
        }
    }

    @Override
    public void sendUserMessage(String messageId, Long messageEntityId, int retryCount) {
        super.messageSenderService.sendUserMessage(messageId, messageEntityId, retryCount);
    }

    public DomibusLogger getLogger() {
        return LOG;
    }
}
