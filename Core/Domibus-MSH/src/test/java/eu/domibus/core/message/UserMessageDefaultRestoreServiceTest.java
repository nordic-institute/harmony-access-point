package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.resend.MessageResendEntity;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.scheduler.DomibusQuartzStarter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    protected UserMessageDefaultService userMessageDefaultService;

    @Injectable
    private AuditService auditService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserMessageRestoreDao userMessageRestoreDao;

    @Injectable
    private DomibusQuartzStarter domibusQuartzStarter;


    @Test
    public void testMaxAttemptsConfigurationWhenNoLegIsFound() {
        final Long messageEntityId = 1L;

        new Expectations(restoreService) {{
            pModeService.getLegConfiguration(messageEntityId);
            result = null;

        }};

        final Integer maxAttemptsConfiguration = restoreService.getMaxAttemptsConfiguration(messageEntityId);
        assertEquals(1, (int) maxAttemptsConfiguration);

    }

    @Test
    public void testMaxAttemptsConfiguration(@Injectable final LegConfiguration legConfiguration) {
        final Long messageEntityId = 1L;
        final Integer pModeMaxAttempts = 5;

        new Expectations(restoreService) {{
            pModeService.getLegConfiguration(messageEntityId);
            result = legConfiguration;

            pModeServiceHelper.getMaxAttempts(legConfiguration);
            result = pModeMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = restoreService.getMaxAttemptsConfiguration(messageEntityId);
        Assert.assertSame(maxAttemptsConfiguration, pModeMaxAttempts);
    }

    @Test
    public void testComputeMaxAttempts(@Injectable final UserMessageLog userMessageLog) {
        final Long messageEntityId = 1L;
        final Integer pModeMaxAttempts = 5;

        new Expectations(restoreService) {{
            restoreService.getMaxAttemptsConfiguration(userMessageLog.getEntityId());
            result = pModeMaxAttempts;

            userMessageLog.getSendAttemptsMax();
            result = pModeMaxAttempts;

        }};

        final Integer maxAttemptsConfiguration = restoreService.computeNewMaxAttempts(userMessageLog);
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
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.SEND_ENQUEUED);

        new Expectations(restoreService) {{
            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            messageExchangeService.retrieveMessageRestoreStatus(messageId, userMessage.getMshRole().getRole());
            result = messageStatusEntity;

            restoreService.computeNewMaxAttempts(userMessageLog);
            result = newMaxAttempts;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;

            userMessageDao.findByEntityId(userMessageLog.getEntityId());
            result = userMessage;
        }};

        restoreService.restoreFailedMessage(messageId);

        new FullVerifications(restoreService) {{
            backendNotificationService.notifyOfMessageStatusChange(userMessage, withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            userMessageLog.setMessageStatus(messageStatusEntity);
            userMessageLog.setRestored(withAny(new Date()));
            userMessageLog.setFailed(null);
            userMessageLog.setNextAttempt(withAny(new Date()));
            userMessageLog.setSendAttemptsMax(newMaxAttempts);

            userMessageLogDao.update(userMessageLog);
            userMessageDefaultService.scheduleSending(userMessage, userMessageLog);

        }};
    }

    @Test
    public void testRestorePUlledMessage(@Injectable final UserMessageLog userMessageLog,
                                         @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final Integer newMaxAttempts = 5;

        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.READY_TO_PULL);

        new Expectations(restoreService) {{

            userMessageDefaultService.getFailedMessage(messageId);
            result = userMessageLog;

            messageExchangeService.retrieveMessageRestoreStatus(messageId, userMessage.getMshRole().getRole());
            result = messageStatusEntity;

            restoreService.computeNewMaxAttempts(userMessageLog);
            result = newMaxAttempts;

            userMessageDao.findByEntityId(userMessageLog.getEntityId());
            result = userMessage;

        }};

        restoreService.restoreFailedMessage(messageId);

        new Verifications() {{
            userMessageLog.setMessageStatus(messageStatusEntity);
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

            userMessageDao.findByEntityId(userMessageLog.getEntityId());
            times = 1;

            pullMessageService.addPullMessageLock(userMessage, userMessageLog);
            times = 1;
        }};
    }

    @Test
    public void test_ResendFailedOrSendEnqueuedMessage_MessageNotFound() {
        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = null;
        }};

        try {
            //tested method
            restoreService.resendFailedOrSendEnqueuedMessage(messageId);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertEquals(MessageNotFoundException.class, e.getClass());
        }

        new FullVerifications() {
        };
    }

    @Test
    public void test_ResendFailedOrSendEnqueuedMessage_SendEnqueuedAndAudit(final @Injectable UserMessageLog userMessageLog) {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(userMessageDefaultService) {{
            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getMessageStatus();
            result = MessageStatus.SEND_ENQUEUED;
        }};

        //tested method
        restoreService.resendFailedOrSendEnqueuedMessage(messageId);

        new FullVerifications(userMessageDefaultService) {{
            String messageIdActual;
            userMessageDefaultService.sendEnqueuedMessage(messageIdActual = withCapture()); //method tested in UserMessageDefaultServiceTest.test_sendEnqueued
            Assert.assertEquals(messageId, messageIdActual);

            auditService.addMessageResentAudit(messageIdActual = withCapture());
            Assert.assertEquals(messageId, messageIdActual);
        }};
    }

    @Test
    public void restoreSelectedFailedMessages() throws SchedulerException {
        final String messageId = UUID.randomUUID().toString();
        final List<String> messageIds = new ArrayList<>();
        messageIds.add(messageId);

        new Expectations(restoreService) {{
            restoreService.MAX_RESEND_MESSAGE_COUNT = 5;
            restoreService.restoreFailedMessage(messageId);
        }};

        restoreService.restoreAllOrSelectedFailedMessages(messageIds);

        new FullVerifications() {{
            restoreService.restoreMessages((List<String>) any, messageIds);
        }};
    }

    @Test
    public void restoreAllFailedMessages() throws SchedulerException {
        final String messageId = UUID.randomUUID().toString();
        final String messageId1 = UUID.randomUUID().toString();
        final List<String> messageIds = new ArrayList<>();
        messageIds.add(messageId);
        messageIds.add(messageId1);

        new Expectations(restoreService) {{
            restoreService.MAX_RESEND_MESSAGE_COUNT = 1;
        }};

        restoreService.restoreAllOrSelectedFailedMessages(messageIds);

        new FullVerifications() {{
            userMessageRestoreDao.create((MessageResendEntity) any);
            domibusQuartzStarter.triggerMessageResendJob();
        }};
    }

}
