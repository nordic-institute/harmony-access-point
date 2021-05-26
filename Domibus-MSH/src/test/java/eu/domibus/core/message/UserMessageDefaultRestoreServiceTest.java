package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Soumya
 * @since 4.2.2
 */
@RunWith(JMockit.class)
public class UserMessageDefaultRestoreServiceTest {

    @Tested
    UserMessageDefaultRestoreService restoreService;

    @Injectable
    PModeService pModeService;

    @Injectable
    PModeServiceHelper pModeServiceHelper;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    protected UserMessageDefaultService userMessageDefaultService;

    @Test
    public void testMaxAttemptsConfigurationWhenNoLegIsFound() {
        final String messageId = "1";

        new Expectations(restoreService) {{
            pModeService.getLegConfiguration(messageId);
            result = null;

        }};

        final Integer maxAttemptsConfiguration = restoreService.getMaxAttemptsConfiguration(messageId);
        assertEquals(1, (int) maxAttemptsConfiguration);

    }

    @Test
    public void testMaxAttemptsConfiguration(@Injectable final LegConfiguration legConfiguration) {
        final String messageId = "1";
        final Integer pModeMaxAttempts = 5;

        new Expectations(restoreService) {{
            pModeService.getLegConfiguration(messageId);
            result = legConfiguration;

            pModeServiceHelper.getMaxAttempts(legConfiguration);
            result = pModeMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = restoreService.getMaxAttemptsConfiguration(messageId);
        Assert.assertSame(maxAttemptsConfiguration, pModeMaxAttempts);
    }

    @Test
    public void testComputeMaxAttempts(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";
        final Integer pModeMaxAttempts = 5;

        new Expectations(restoreService) {{
            restoreService.getMaxAttemptsConfiguration(messageId);
            result = pModeMaxAttempts;

            userMessageLog.getSendAttemptsMax();
            result = pModeMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = restoreService.computeNewMaxAttempts(userMessageLog, messageId);
        assertEquals(11, (int) maxAttemptsConfiguration);
    }

    @Test(expected = UserMessageException.class)
    public void testRestoreMessageWhenMessageIsDeleted(@Injectable final UserMessageLog userMessageLog) {
        final String messageId = "1";

        new Expectations(restoreService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.DELETED;

        }};

        restoreService.restoreFailedMessage(messageId);
    }

    @Test
    public void testRestorePushedMessage(@Injectable final UserMessageLog userMessageLog,
                                         @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final Integer newMaxAttempts = 5;

        new Expectations(restoreService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            messageExchangeService.retrieveMessageRestoreStatus(messageId);
            result = MessageStatus.SEND_ENQUEUED;

            restoreService.computeNewMaxAttempts(userMessageLog, messageId);
            result = newMaxAttempts;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;
        }};

        restoreService.restoreFailedMessage(messageId);

        new FullVerifications(restoreService) {{
            backendNotificationService.notifyOfMessageStatusChange(withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            userMessageLog.setMessageStatus(MessageStatus.SEND_ENQUEUED);
            userMessageLog.setRestored(withAny(new Date()));
            userMessageLog.setFailed(null);
            userMessageLog.setNextAttempt(withAny(new Date()));
            userMessageLog.setSendAttemptsMax(newMaxAttempts);

            userMessageLogDao.update(userMessageLog);
            uiReplicationSignalService.messageChange(anyString);
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);

        }};
    }

    @Test
    public void testRestorePUlledMessage(@Injectable final UserMessageLog userMessageLog,
                                         @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final Integer newMaxAttempts = 5;

        new Expectations(restoreService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            messageExchangeService.retrieveMessageRestoreStatus(messageId);
            result = MessageStatus.READY_TO_PULL;

            restoreService.computeNewMaxAttempts(userMessageLog, messageId);
            result = newMaxAttempts;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

        }};

        restoreService.restoreFailedMessage(messageId);

        new Verifications() {{
            userMessageLog.setMessageStatus(MessageStatus.READY_TO_PULL);
            times = 1;
            userMessageLog.setRestored(withAny(new Date()));
            times = 1;
            userMessageLog.setFailed(null);
            times = 1;
            userMessageLog.setNextAttempt(withAny(new Date()));
            times = 1;
            userMessageLog.setSendAttemptsMax(newMaxAttempts);
            times = 1;

            userMessageLogDao.update(userMessageLog);
            times = 1;

            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);
            times = 0;

            messagingDao.findUserMessageByMessageId(messageId);
            times = 1;

            pullMessageService.addPullMessageLock(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void restoreSendEnqueuedMessage(@Injectable final UserMessageLog userMessageLog,
                                           @Injectable final UserMessage userMessage) {
        final String messageId = "1";

        new Expectations(restoreService) {{
            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

        }};

        restoreService.restoreSendEnqueuedMessage(messageId);

        new Verifications() {{
            restoreService.restoreMessage(messageId, userMessageLog);
        }};
    }

    @Test
    public void restoreMessage(@Injectable final UserMessageLog userMessageLog,
                               @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final Integer newMaxAttempts = 5;

        new Expectations(restoreService) {{

            messageExchangeService.retrieveMessageRestoreStatus(messageId);
            result = MessageStatus.READY_TO_PULL;

            restoreService.computeNewMaxAttempts(userMessageLog, messageId);
            result = newMaxAttempts;

            messagingDao.findUserMessageByMessageId(messageId);
            result = userMessage;

        }};

        restoreService.restoreMessage(messageId, userMessageLog);

        new Verifications() {{
            userMessageLog.setMessageStatus(MessageStatus.READY_TO_PULL);
            times = 1;
            userMessageLog.setRestored(withAny(new Date()));
            times = 1;
            userMessageLog.setFailed(null);
            times = 1;
            userMessageLog.setNextAttempt(withAny(new Date()));
            times = 1;
            userMessageLog.setSendAttemptsMax(newMaxAttempts);
            times = 1;

            userMessageLogDao.update(userMessageLog);
            times = 1;

            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);
            times = 0;

            messagingDao.findUserMessageByMessageId(messageId);
            times = 1;

            pullMessageService.addPullMessageLock(userMessage, userMessageLog);
            times = 1;
        }};
    }

}
