package eu.domibus.core.message.pull;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.api.model.ProcessingType.PULL;
import static eu.domibus.api.model.ProcessingType.PUSH;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JMockit.class)
public class PullMessageStateServiceImplTest {

    @Tested
    PullMessageStateServiceImpl pullMessageStateService;

    @Injectable
    protected UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Injectable
    protected UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    protected BackendNotificationService backendNotificationService;

    @Injectable
    protected UserMessageDao userMessageDao;

    @Injectable
    protected MessageStatusDao messageStatusDao;

    @Test
    public void expirePullMessageTest(@Injectable UserMessageLog userMessageLog) {

        final String messageId = "messageId";

        new Expectations(pullMessageStateService) {{
            userMessageLogDefaultService.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
            pullMessageStateService.sendFailed(userMessageLog, messageId);
            times = 1;
        }};
        pullMessageStateService.expirePullMessage(messageId);
        Assert.assertNotNull(userMessageLog);

        new Verifications() {{
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(anyLong);
            times = 1;
        }};

    }

    @Test
    public void sendFailedTest(@Injectable UserMessageLog userMessageLog) {
        final String messageId = "messageId";

        UserMessage userMessage = new UserMessage();
        new Expectations() {{
            userMessageDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessage;
        }};
        pullMessageStateService.sendFailed(userMessageLog, messageId);
        Assert.assertNotNull(userMessage);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog, PULL);
            times = 1;
        }};
    }

    @Test
    public void sendFailedWithNullUserMessageTest(@Injectable UserMessageLog userMessageLog,
                                                  @Injectable UserMessage userMessage) {

        final String messageId = "messageId";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId, MSHRole.SENDING);
            result = null;
        }};

        pullMessageStateService.sendFailed(userMessageLog, messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog, PUSH);
            times = 0;
        }};
    }

    @Test
    public void sendFailedWithNullUserMessageLogTest() {

        final String messageId = "messageId";

        pullMessageStateService.sendFailed(null, messageId);

        new FullVerifications() {
        };
    }

}
