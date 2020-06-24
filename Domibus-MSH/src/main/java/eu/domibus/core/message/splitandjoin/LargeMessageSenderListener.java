package eu.domibus.core.message.splitandjoin;

import eu.domibus.core.ebms3.sender.AbstractMessageSenderListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service(value = "largeMessageSenderListener")
public class LargeMessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LargeMessageSenderListener.class);

    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Override
    public void onMessage(final Message message) {
        LOG.debug("Processing large message [{}]", message);
        super.onMessage(message);
    }

    @Override
    public void scheduleSending(String messageId, Long delay) {
        super.userMessageService.scheduleSending(messageId, delay, true);
    }

    @Override
    public void sendUserMessage(String messageId, int retryCount) {
        super.messageSenderService.sendUserMessage(messageId, retryCount, true);
    }
}
