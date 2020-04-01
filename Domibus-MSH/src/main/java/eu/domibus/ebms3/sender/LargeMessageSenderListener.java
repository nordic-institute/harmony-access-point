package eu.domibus.ebms3.sender;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Override
    public void onMessage(final Message message) {
        LOG.debug("Processing large message [{}]", message);
        long start = System.currentTimeMillis();
        try {
            super.onMessage(message);
        } finally {
            LOG.info("LargeMessageSenderListener took [{}] milliseconds", System.currentTimeMillis() - start);
        }
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
