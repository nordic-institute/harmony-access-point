package eu.domibus.core.ebms3.sender;

import eu.domibus.api.message.attempt.MessageAttempt;
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
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author idragusa
 * @since 4.2.2
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class MessageSenderErrorHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderErrorHandlerTest.class);

    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String PMODE_KEY = "PMODE_KEY";

    @Tested
    private MessageSenderErrorHandler messageSenderErrorHandler;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Mocked
    private Messaging messaging;

    @Mocked
    LegConfiguration legConfiguration;

    @Mocked
    UserMessage userMessage;

    @Test
    public void verifyHandler() throws EbMS3Exception {

        new Expectations(messageSenderErrorHandler) {{
            messagingDao.findMessageByMessageId(anyString);
            result = messaging;

            pModeProvider.findUserMessageExchangeContext((UserMessage)any, MSHRole.SENDING).getPmodeKey();
            result = PMODE_KEY;

            pModeProvider.getLegConfiguration(PMODE_KEY);
            result = legConfiguration;

            messaging.getUserMessage();
            result = userMessage;

            updateRetryLoggingService.updatePushedMessageRetryLogging(anyString, legConfiguration, (MessageAttempt)any);
        }};

        messageSenderErrorHandler.handleError((new Throwable("OutOfMemory")));

        new FullVerifications() {
        };
    }
}