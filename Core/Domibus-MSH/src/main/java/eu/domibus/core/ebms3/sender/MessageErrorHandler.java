package eu.domibus.core.ebms3.sender;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * @author idragusa
 * @since 4.2.2
 */

@Service("messageErrorHandler")
public class MessageErrorHandler implements ErrorHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageErrorHandler.class);



    @Override
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void handleError(Throwable t) {
        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);
        LOG.warn("Error dispatching message [{}] from application server", messageId, t);
    }
}
