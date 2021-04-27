package eu.domibus.core.ebms3.sender;


import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import java.sql.Timestamp;

/**
 * @author idragusa
 * @since 4.2.2
 */

@Service("messageSenderErrorHandler")
public class MessageSenderErrorHandler implements ErrorHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderErrorHandler.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Override
    public void handleError(Throwable t) {
        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);
        LOG.warn("Handling dispatch error for message " + messageId, t);

        final Messaging messaging = messagingDao.findMessageByMessageId(messageId);
        final UserMessage userMessage = messaging.getUserMessage();

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        attempt.setStatus(MessageAttemptStatus.ERROR);
        attempt.setError(t.getMessage());

        final String pModeKey;
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (final EbMS3Exception e) {
            LOG.error("Impossible to handle error for message " + messageId, e);
            return ;
        }

        LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        LOG.warn("Updating the retry logging for message " + messageId);
        updateRetryLoggingService.updatePushedMessageRetryLogging(messageId, legConfiguration, attempt);
    }
}