package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.MessageStatusEntity;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

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
    protected UserMessageLogDao userMessageLogDao;

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
            userMessageLogDao.findByMessageId(messageId);
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
    public void sendFailedTest(@Injectable UserMessageLog userMessageLog,
                               @Injectable UserMessage userMessage) {
        final String messageId = "messageId";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = userMessage;
        }};
        pullMessageStateService.sendFailed(userMessageLog, messageId);
        Assert.assertNotNull(userMessage);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void sendFailedWithNullUserMessageTest(@Injectable UserMessageLog userMessageLog,
                                                  @Injectable UserMessage userMessage) {

        final String messageId = "messageId";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = null;
        }};

        pullMessageStateService.sendFailed(userMessageLog, messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
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

    @Test
    public void resetTest(@Injectable UserMessageLog userMessageLog,
                          @Injectable MessageStatusEntity readyToPull) {
        final String messageId = "messageId";

        new Expectations() {{
            messageStatusDao.findOrCreate(MessageStatus.READY_TO_PULL);
            result = readyToPull;
        }};

        pullMessageStateService.reset(userMessageLog, messageId);

        new Verifications() {{
            userMessageLog.setMessageStatus(readyToPull);
            userMessageLogDao.update(userMessageLog);
            times = 1;
            times = 1;
            backendNotificationService.notifyOfMessageStatusChange(messageId, userMessageLog, MessageStatus.READY_TO_PULL, (Timestamp) any);
            times = 1;
        }};
    }

}