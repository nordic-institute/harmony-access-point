package eu.domibus.core.message.splitandjoin;

import eu.domibus.core.ebms3.sender.AbstractMessageSenderListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import javax.jms.Message;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class LargeMessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LargeMessageSenderListener.class);

    @Override
    public void onMessage(final Message message) {
        LOG.debug("Processing large message [{}]", message);
        super.onMessage(message);
    }


    @Override
    public void sendUserMessage(String messageId, Long messageEntityId, int retryCount) {
        super.messageSenderService.sendUserMessage(messageId, messageEntityId, retryCount);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
