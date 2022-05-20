package eu.domibus.core.message;

import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.api.usermessage.UserMessageDownloadEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to events related to transactions
 *
 * @author idragusa
 * @since 5.0
 */
@Component
public class UserMessageRollbackListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageRollbackListener.class);

    /**
     * On transaction rollback, the message status is reverted from DOWNLOADED to RECEIVED, add a business log for it.
     */
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleEvent(UserMessageDownloadEvent downloadEvent) {
        final String messageId = downloadEvent.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_ROLLBACK, "USER_MESSAGE", MessageStatus.RECEIVED);
    }
}
