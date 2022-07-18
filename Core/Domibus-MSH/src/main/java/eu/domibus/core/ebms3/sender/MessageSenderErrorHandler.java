package eu.domibus.core.ebms3.sender;


import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.apache.commons.lang3.math.NumberUtils;
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

    private UserMessageDao userMessageDao;

    private PModeProvider pModeProvider;

    private UpdateRetryLoggingService updateRetryLoggingService;

    public MessageSenderErrorHandler(UserMessageDao userMessageDao,
                                     PModeProvider pModeProvider,
                                     UpdateRetryLoggingService updateRetryLoggingService) {
        this.userMessageDao = userMessageDao;
        this.pModeProvider = pModeProvider;
        this.updateRetryLoggingService = updateRetryLoggingService;
    }

    @Override
    @MDCKey({DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID})
    public void handleError(Throwable t) {
        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);
        String messageEntityId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ENTITY_ID);
        LOG.warn("Handling dispatch error for message [{}]", messageId, t);

        // what mshRole? sending probably/ Or use entityId?
//        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, MSHRole.SENDING);
        final UserMessage userMessage = userMessageDao.findByEntityId(NumberUtils.createLong(messageEntityId));

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        attempt.setStatus(MessageAttemptStatus.ERROR);
        attempt.setError(t.getMessage());

        final String pModeKey;
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
        } catch (final EbMS3Exception e) {
            LOG.error("Could not get the pMode key for message " + messageId, e);
            return;
        }

        LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        LOG.warn("Updating the retry logging for message " + messageId);
        updateRetryLoggingService.updatePushedMessageRetryLogging(userMessage, legConfiguration, attempt);
    }
}
