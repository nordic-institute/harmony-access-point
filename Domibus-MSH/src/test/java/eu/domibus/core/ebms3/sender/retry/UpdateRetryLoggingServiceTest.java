
package eu.domibus.core.ebms3.sender.retry;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.model.*;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.scheduler.ReprogrammableService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
@Ignore
public class UpdateRetryLoggingServiceTest {

    private static final int RETRY_TIMEOUT_IN_MINUTES = 60;

    private static final int RETRY_COUNT = 4;

    private static final long SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 = 1451602800000L; //This is the reference time returned by System.correctTImeMillis() mock

    private static final long FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016 = SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 - (60 * 5 * 1000);

    private static final long ONE_HOUR_BEFORE_FIRST_OF_JANUARY_2016 = SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 - (60 * 60 * 1000);

    @Tested
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageLogDefaultService messageLogService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    MessageAttemptService messageAttemptService;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    MessageRetentionDefaultService messageRetentionService;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    MessageGroupDao messageGroupDao;

    @Injectable
    private ReprogrammableService reprogrammableService;

    @Injectable
    MessageStatusDao messageStatusDao;


    /**
     * Max retries limit reached
     * Timeout limit not reached
     * Notification is enabled
     * Expected result: MessageLogDao#setAsNotified() is called
     * MessageLogDao#setMessageAsSendFailure is called
     * MessagingDao#clearPayloadData() is called
     *
     * @throws Exception
     */
    @Test
    public void testUpdateRetryLogging_maxRetriesReachedNotificationEnabled_ExpectedMessageStatus(@Injectable UserMessage userMessage,
                                                                                                  @Injectable UserMessageLog userMessageLog,
                                                                                                  @Injectable LegConfiguration legConfiguration) throws Exception {
        final String messageId = UUID.randomUUID().toString();

        new Expectations(updateRetryLoggingService) {{
            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = false;

            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
            updateRetryLoggingService.getScheduledStartDate(userMessageLog);
        }};


        updateRetryLoggingService.updateRetryLogging(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, null);

        new Verifications() {{
            userMessageLogDao.update(userMessageLog);
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
        }};
    }


    /**
     * Message was restored
     * NextAttempt is set correctly
     */
    @Test
    public void testUpdateRetryLogging_Restored(@Injectable UserMessage userMessage,
                                                @Injectable LegConfiguration legConfiguration,
                                                @Injectable UserMessageLog userMessageLog) throws Exception {
        new SystemMockFirstOfJanuary2016(); //current timestamp

        final String messageId = UUID.randomUUID().toString();


        new Expectations() {{
            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            userMessageLog.getSendAttempts();
            result = 2;
        }};


        updateRetryLoggingService.updateRetryLogging(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, null);

        new Verifications() {{
            userMessageLog.setSendAttempts(3);
        }};
    }

    @Test
    public void testUpdateMessageLogNextAttemptDateForRestoredMessage(@Injectable LegConfiguration legConfiguration,
                                                                      @Injectable UserMessageLog userMessageLog) {

        new SystemMockFirstOfJanuary2016(); //current timestamp

        Date nextAttempt = new Date(FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016 + (RETRY_TIMEOUT_IN_MINUTES / RETRY_COUNT * 60 * 1000));


        new Expectations() {{
            userMessageLog.getNextAttempt();
            result = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016;

            legConfiguration.getReceptionAwareness().getStrategy().getAlgorithm();
            result = RetryStrategy.CONSTANT.getAlgorithm();

            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = RETRY_TIMEOUT_IN_MINUTES;

            legConfiguration.getReceptionAwareness().getRetryCount();
            result = RETRY_COUNT;
        }};

        updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);


        new Verifications() {{
            reprogrammableService.setRescheduleInfo(userMessageLog, nextAttempt);
        }};
    }

    /**
     * Max retries limit reached
     * Notification is disabled
     * Clear payload is default (false)
     * Expected result: MessagingDao#clearPayloadData is not called
     * MessageLogDao#setMessageAsSendFailure is called
     * MessageLogDao#setAsNotified() is not called
     *
     */
    @Test
    public void testUpdateRetryLogging_maxRetriesReachedNotificationDisabled_ExpectedMessageStatus_ClearPayloadDisabled(@Injectable UserMessage userMessage,
                                                                                                                        @Injectable UserMessageLog userMessageLog,
                                                                                                                        @Injectable LegConfiguration legConfiguration) throws Exception {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        final long receivedTime = FIVE_MINUTES_BEFORE_FIRST_OF_JANUARY_2016; //Received 5 min ago

        new Expectations() {{
            userMessageLog.getSendAttempts();
            result = 2;

            userMessageLog.getSendAttemptsMax();
            result = 3;

//            userMessageLog.getReceived();
//            result = new Date(receivedTime);

//            userMessageLog.getNotificationStatus();
//            result = NotificationStatus.NOT_REQUIRED;

            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};

        updateRetryLoggingService.updatePushedMessageRetryLogging(userMessage, legConfiguration, null);

        new Verifications() {{
            messageLogService.setMessageAsSendFailure(userMessage, userMessageLog);
            userMessageLogDao.setAsNotified(userMessageLog);
            times = 0;
        }};

    }

    /**
     * Max retries limit not reached
     * Timeout limit reached
     * Notification is enabled
     * Expected result: MessagingDao#clearPayloadData is called
     * MessageLogDao#setMessageAsSendFailure is called
     * MessageLogDao#setAsNotified() is called
     */
    @Test
    public void testUpdateRetryLogging_timeoutNotificationEnabled_ExpectedMessageStatus(@Injectable UserMessage userMessage,
                                                                                        @Injectable UserMessageLog userMessageLog,
                                                                                        @Injectable LegConfiguration legConfiguration,
                                                                                        @Injectable NotificationStatusEntity notificationStatus) {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();

        new Expectations() {{
            userMessageLog.getSendAttempts();
            result = 0;

            userMessageLog.getSendAttemptsMax();
            result = 3;

            userMessageLog.getNotificationStatus();
            result = notificationStatus;

            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;
        }};


        updateRetryLoggingService.updatePushedMessageRetryLogging(userMessage, legConfiguration, null);


        new Verifications() {{
            messageLogService.setMessageAsSendFailure(userMessage, userMessageLog);
            messageRetentionService.deletePayloadOnSendFailure(userMessage, userMessageLog);
        }};

    }


    /**
     * Max retries limit not reached
     * Timeout limit reached
     */
    @Test
    public void testUpdateRetryLogging_timeoutNotificationDisabled_ExpectedMessageStatus(@Injectable UserMessage userMessage,
                                                                                         @Injectable UserMessageLog userMessageLog,
                                                                                         @Injectable LegConfiguration legConfiguration) {
        new SystemMockFirstOfJanuary2016();

        final String messageId = UUID.randomUUID().toString();
        int retryTimeout = 1;

        new Expectations(updateRetryLoggingService) {{
            userMessageLog.getSendAttempts();
            result = 0;

            userMessageLog.getSendAttemptsMax();
            result = 3;

            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = retryTimeout;

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = ONE_HOUR_BEFORE_FIRST_OF_JANUARY_2016;
        }};


        Assert.assertFalse(updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration));
    }

    @Test
    public void testUpdateRetryLogging_success_ExpectedMessageStatus(@Injectable UserMessage userMessage,
                                                                     @Injectable UserMessageLog userMessageLog,
                                                                     @Injectable LegConfiguration legConfiguration,
                                                                     @Injectable MessageAttempt messageAttempt) throws Exception {

        final String messageId = UUID.randomUUID().toString();
        new Expectations(updateRetryLoggingService) {{
            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            updateRetryLoggingService.hasAttemptsLeft(userMessageLog, legConfiguration);
            result = true;

            userMessage.isTestMessage();
            result = false;

            updateRetryLoggingService.updateNextAttemptAndNotify(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, userMessageLog);
        }};

        updateRetryLoggingService.updateRetryLogging(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, messageAttempt);

        new Verifications() {{
            userMessageLogDao.update(userMessageLog);
            updateRetryLoggingService.updateNextAttemptAndNotify(userMessage, legConfiguration, MessageStatus.WAITING_FOR_RETRY, userMessageLog);
            messageAttemptService.createAndUpdateEndDate(messageAttempt);
        }};
    }

    @Test
    public void testMessageExpirationDate(@Injectable final UserMessageLog userMessageLog,
                                          @Injectable final LegConfiguration legConfiguration) throws InterruptedException {
        final int timeOutInMin = 10; // in minutes
        final long timeOutInMillis = 60000L * timeOutInMin;
        final long restoredTime = System.currentTimeMillis();
        final Date expectedDate = new Date(restoredTime + timeOutInMillis);


        new Expectations(updateRetryLoggingService) {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = timeOutInMin;

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = restoredTime;
        }};

        Date messageExpirationDate = updateRetryLoggingService.getMessageExpirationDate(userMessageLog, legConfiguration);

        assertEquals(expectedDate, messageExpirationDate);
    }

    @Test
    public void testMessageExpirationDateInTheFarFuture(@Injectable final UserMessageLog userMessageLog,
                              @Injectable final LegConfiguration legConfiguration) throws InterruptedException {
        final int timeOutInMin = 90 * 24 * 60; // 90 days in minutes
        final long timeOutInMillis = 60000L * timeOutInMin;
        final long restoredTime = System.currentTimeMillis();
        final Date expectedDate = new Date(restoredTime + timeOutInMillis);

        new Expectations(updateRetryLoggingService) {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = timeOutInMin;

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = restoredTime;
        }};
        Date messageExpirationDate = updateRetryLoggingService.getMessageExpirationDate(userMessageLog, legConfiguration);

        assertEquals(expectedDate, messageExpirationDate);
    }

    @Test
    public void testIsExpired(@Injectable final UserMessageLog userMessageLog,
                              @Injectable final LegConfiguration legConfiguration) throws InterruptedException {

        long delay = 10;

        new Expectations(updateRetryLoggingService) {{
            domibusPropertyProvider.getLongProperty(UpdateRetryLoggingService.MESSAGE_EXPIRATION_DELAY);
            result = delay;

            updateRetryLoggingService.getMessageExpirationDate(userMessageLog, legConfiguration);
            result = SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016 - delay - 100;

        }};

        boolean result = updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
        assertTrue(result);
    }

    private static class SystemMockFirstOfJanuary2016 extends MockUp<System> {
        @Mock
        public static long currentTimeMillis() {
            return SYSTEM_DATE_IN_MILLIS_FIRST_OF_JANUARY_2016;
        }
    }

    @Test
    public void test_failIfExpired_MessageExpired_NotSourceMessage(final @Mocked UserMessage userMessage) {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        userMessageLog.setMessageStatus(messageStatus);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations(updateRetryLoggingService) {{
            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = true;

            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
        }};

        //tested method
        boolean result = updateRetryLoggingService.failIfExpired(userMessage, legConfiguration);
        Assert.assertTrue(result);

        new FullVerifications(updateRetryLoggingService) {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);

            updateRetryLoggingService.setMessageFailed(userMessage, userMessageLog);
        }};
    }

    @Test
    public void test_failIfExpired_MessageNotExpired_NotSourceMessage(final @Mocked UserMessage userMessage) {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        MessageStatusEntity messageStatus = new MessageStatusEntity();
        messageStatus.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);
        userMessageLog.setMessageStatus(messageStatus);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations(updateRetryLoggingService) {{
            userMessage.getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = false;
        }};

        //tested method
        boolean result = updateRetryLoggingService.failIfExpired(userMessage, legConfiguration);
        Assert.assertFalse(result);

        new FullVerifications() {{
        }};
    }
}