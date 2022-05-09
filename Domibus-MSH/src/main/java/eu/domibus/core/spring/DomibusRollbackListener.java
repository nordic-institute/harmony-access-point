package eu.domibus.core.spring;

import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.plugin.DownloadEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author idragusa
 * @since 5.0
 */
@Component
public class DomibusRollbackListener {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusRollbackListener.class);

    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleEvent(DownloadEvent downloadEvent) {
        final String messageId = downloadEvent.getMessageId();
        if (StringUtils.isNotBlank(messageId)) {
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
        }
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_STATUS_ROLLBACK, "USER_MESSAGE", MessageStatus.RECEIVED);
    }
}
