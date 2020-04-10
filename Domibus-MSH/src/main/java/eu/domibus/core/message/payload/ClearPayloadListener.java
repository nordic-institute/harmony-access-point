package eu.domibus.core.message.payload;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

/**
 * @since 4.2
 * @author Catalin Enache
 */
@Component
public class ClearPayloadListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearPayloadListener.class);

    @Autowired
    ClearPayloadMessageService clearPayloadMessageService;

    @JmsListener(destination = "${domibus.jms.queue.clear.payload}", containerFactory = "clearPayloadJmsListenerContainerFactory")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID})
    public void onMessage(final Message message) throws JMSException {
        LOG.debug("clearPayload message received");

        final String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
        if (StringUtils.isBlank(messageId)) {
            LOG.debug("no messageId retrieved from [{}]", message);
            return;
        }
        //add messageId to MDC map
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

        clearPayloadMessageService.clearPayloadData(messageId);
        LOG.debug("clearPayload done");
    }

}
