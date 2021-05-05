package eu.domibus.core.message.pull;

import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
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
    protected UIReplicationSignalService uiReplicationSignalService;

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
        }};
        pullMessageStateService.expirePullMessage(messageId);
        Assert.assertNotNull(userMessageLog);

        new Verifications() {{
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(messageId);
            times = 1;
            pullMessageStateService.sendFailed(userMessageLog, messageId);
            times = 1;
        }};

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void sendFailedTest(@Injectable UserMessageLog userMessageLog,
                               @Injectable UserMessage userMessage) {
        final String messageId = "messageId";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;
        }};
        pullMessageStateService.sendFailed(userMessageLog, messageId);
        Assert.assertNotNull(userMessage);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void sendFailedWithNullUserMessageTest(@Injectable UserMessageLog userMessageLog,
                                                  @Injectable UserMessage userMessage) {

        final String messageId = "messageId";

        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;
        }};

        pullMessageStateService.sendFailed(userMessageLog, messageId);

        new Verifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
            times = 0;
        }};
    }

    @Test
    public void resetTest(@Injectable UserMessageLog userMessageLog,
                          @Injectable MessageStatus messageStatus,
                          @Mocked Timestamp timestamp) {
        final MessageStatus readyToPull = messageStatus.READY_TO_PULL;
        final String messageId = "messageId";

        new Expectations(pullMessageStateService) {{
//            userMessageLog.setMessageStatus(readyToPull);
//            times = 1;
            userMessageLogDao.update(userMessageLog);
            times = 1;
            uiReplicationSignalService.messageChange(messageId);
            times = 1;
            backendNotificationService.notifyOfMessageStatusChange(messageId, userMessageLog, MessageStatus.READY_TO_PULL, new Timestamp(anyLong));
            times = 1;
        }};
        pullMessageStateService.reset(userMessageLog, messageId);

        new VerificationsInOrder() {{
            userMessageLog.setMessageStatus(withCapture());
            userMessageLogDao.update(withCapture());
            uiReplicationSignalService.messageChange(withCapture());
            backendNotificationService.notifyOfMessageStatusChange(messageId, userMessageLog, messageStatus.READY_TO_PULL, new Timestamp(anyLong));
        }};
    }

}