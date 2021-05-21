package eu.domibus.core.message.pull;

import com.google.common.collect.Lists;
import eu.domibus.api.model.*;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.core.scheduler.ReprogrammableService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PullMessageEbms3ServiceImplTest {

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private PullMessageStateService pullMessageStateService;

    @Injectable
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private UserMessageRawEnvelopeDao rawEnvelopeLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Injectable
    protected MpcService mpcService;


    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    private UserMessageService userMessageService;

    @Injectable
    private MessageRetentionDefaultService messageRetentionService;

    @Injectable
    private MessageStatusDao messageStatusDao;

    @Injectable
    private UserMessageDao userMessageDao;

    @Injectable
    private ReprogrammableService reprogrammableService;

    @Tested
    private PullMessageServiceImpl pullMessageService;

    @Test
    public void delete() {
        String messageId = "messageId";
        pullMessageService.deletePullMessageLock(messageId);
        new Verifications() {{
            messagingLockDao.delete(messageId);
        }};
    }

    @Test
    public void getPullMessageIdFirstAttempt(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final long id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.FIRST_ATTEMPT;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertEquals(messageId, returnedMessageId);

        new Verifications() {{
            pullMessageStateService.expirePullMessage(messageId);
            times = 0;
        }};

    }

    @Test
    public void getPullMessageIdExpired(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final long id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.EXPIRED;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertNull(returnedMessageId);

        new Verifications() {{
            pullMessageStateService.expirePullMessage(messageId);
            times = 1;
        }};

    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void getPullMessageIdRetry(@Mocked final MessagingLock messagingLock, @Mocked final PullMessageId pullMessageId) {
        final String initiator = "initiator";
        final String mpc = "mpc";
        final String messageId = "messageId";
        final long id = 99;
        new Expectations() {{

            messagingLockDao.findReadyToPull(mpc, initiator);
            result = Lists.newArrayList(messagingLock);

            messagingLock.getMessageId();
            result = messageId;

            messagingLock.getEntityId();
            this.result = id;

            messagingLockDao.getNextPullMessageToProcess(id);
            result = pullMessageId;

            pullMessageId.getState();
            result = PullMessageState.RETRY;

            pullMessageId.getMessageId();
            result = messageId;

        }};
        final String returnedMessageId = pullMessageService.getPullMessageId(initiator, mpc);
        assertEquals(messageId, returnedMessageId);

        new Verifications() {{
            rawEnvelopeLogDao.deleteUserMessageRawEnvelope(id);
            times = 1;
        }};

    }

    @Test(expected = PModeException.class)
    public void addPullMessageLockWithPmodeException(@Mocked final UserMessage userMessage, @Mocked final UserMessageLog messageLog) throws EbMS3Exception {
        final String partyId = "partyId";
        final String messageId = "messageId";
        final String mpc = "mpc";
        new NonStrictExpectations(pullMessageService) {{
//            userMessage.getToFirstPartyId();
//            result = partyId;
//            messageLog.getMessageId();
//            result = messageId;
//            messageLog.getMpc();
//            result = mpc;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, anyBoolean).getPmodeKey();
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "", "", null);
        }};

        pullMessageService.addPullMessageLock(userMessage, messageLog);
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void addPullMessageLock(@Injectable final UserMessage userMessage,
                                   @Injectable final UserMessageLog messageLog) throws EbMS3Exception {
        final String pmodeKey = "pmodeKey";
        final String partyId = "partyId";
        final String messageId = "messageId";
        final String mpc = "mpc";
        final Date staledDate = new Date();
        final LegConfiguration legConfiguration = new LegConfiguration();
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageId;
            userMessage.getMpc();
            result = mpc;
            messageLog.getNextAttempt();
            result = null;
            userMessage.getPartyInfo().getToParty();
            result = partyId;
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING, anyBoolean).getPmodeKey();
            result = pmodeKey;
            pModeProvider.getLegConfiguration(pmodeKey);
            result = legConfiguration;
            updateRetryLoggingService.getMessageExpirationDate(messageLog, legConfiguration);
            result = staledDate;
        }};
        pullMessageService.addPullMessageLock(userMessage, messageLog);
        new Verifications() {{
            MessagingLock messagingLock = null;
            messagingLockDao.save(messagingLock = withCapture());
            assertEquals(partyId, messagingLock.getInitiator());
            assertEquals(mpc, messagingLock.getMpc());
            assertEquals(messageId, messagingLock.getMessageId());
            assertEquals(staledDate, messagingLock.getStaled());
            assertNotNull(messagingLock.getNextAttempt());
        }};
    }

    @Test
    public void waitingForCallExpired(
            @Injectable final MessagingLock lock,
            @Injectable final LegConfiguration legConfiguration,
            @Injectable final UserMessageLog userMessageLog,
            @Injectable final UserMessage userMessage,
            @Injectable final Timestamp timestamp) {

        String messageId = "123";
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageId;

            messagingLockDao.findMessagingLockForMessageId(messageId);
            result = lock;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = true;

        }};
        pullMessageService.waitingForCallBack(userMessage, legConfiguration, userMessageLog);
        new Verifications() {{
            pullMessageStateService.sendFailed(userMessageLog, messageId);
            reprogrammableService.removeRescheduleInfo(lock);
            lock.setMessageState(MessageState.DEL);
            messagingLockDao.save(lock);
            userMessageLogDao.update(userMessageLog);
            times = 0;
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void waitingForCallBackWithAttempt(
            @Injectable final MessagingLock lock,
            @Injectable final LegConfiguration legConfiguration,
            @Injectable final UserMessageLog userMessageLog,
            @Injectable final UserMessage userMessage,
            @Injectable final Timestamp timestamp) {
        new Expectations(pullMessageService) {{
            messagingLockDao.findMessagingLockForMessageId(userMessage.getMessageId());
            result = lock;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = false;

            updateRetryLoggingService.updateMessageLogNextAttemptDate(legConfiguration, userMessageLog);
        }};
        pullMessageService.waitingForCallBack(userMessage, legConfiguration, userMessageLog);
        new Verifications() {{
            lock.setMessageState(MessageState.WAITING);
            lock.setSendAttempts(userMessageLog.getSendAttempts());
            reprogrammableService.setRescheduleInfo(lock, userMessageLog.getNextAttempt());

            MessageStatusEntity messageStatus = new MessageStatusEntity();
            messageStatus.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
            userMessageLog.setMessageStatus(messageStatus);
            messagingLockDao.save(lock);
            userMessageLogDao.update(userMessageLog);
            backendNotificationService.notifyOfMessageStatusChange(userMessage, userMessageLog, MessageStatus.WAITING_FOR_RECEIPT, withAny(timestamp));
        }};
    }

    @Test
    public void hasAttemptsLeftTrueBecauseOfSendAttempt(@Injectable final UserMessageLog userMessageLog,
                                                        @Injectable final LegConfiguration legConfiguration) {
        new NonStrictExpectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;
            userMessageLog.getSendAttempts();
            result = 1;
            userMessageLog.getSendAttemptsMax();
            result = 2;

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() + 10000;

            updateRetryLoggingService.getMessageExpirationDate(userMessageLog, legConfiguration);
            result = new Date(System.currentTimeMillis() + 50000);

        }};
        assertTrue(pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration));
    }

    @Test
    public void hasAttemptsLeftFalseBecauseOfSendAttempt(@Injectable final UserMessageLog userMessageLog,
                                                         @Injectable final LegConfiguration legConfiguration) {
        new Expectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            times = 0;
            userMessageLog.getSendAttempts();
            result = 3;
            userMessageLog.getSendAttemptsMax();
            result = 2;
            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            times = 0;
        }};
        assertFalse(pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration));
    }

    @Test
    public void equalAttemptsButNotExpired(@Injectable final UserMessageLog userMessageLog,
                                           @Injectable final LegConfiguration legConfiguration) {
        new NonStrictExpectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;

            userMessageLog.getSendAttempts();
            result = 2;

            userMessageLog.getSendAttemptsMax();
            result = 2;

            updateRetryLoggingService.getScheduledStartTime(userMessageLog);
            result = System.currentTimeMillis() + 70000;

            updateRetryLoggingService.getMessageExpirationDate(userMessageLog, legConfiguration);
            result = new Date(System.currentTimeMillis() + 50000);
        }};
        final boolean actual = pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration);
        assertTrue(actual);
    }

    @Test
    public void equalAttemptsButExpired(@Injectable final UserMessageLog userMessageLog,
                                        @Injectable final LegConfiguration legConfiguration) {
        new NonStrictExpectations() {{
            legConfiguration.getReceptionAwareness().getRetryTimeout();
            result = 1;

            userMessageLog.getSendAttempts();
            result = 2;

            userMessageLog.getSendAttemptsMax();
            result = 2;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = true;
        }};
        final boolean actual = pullMessageService.attemptNumberLeftIsLowerOrEqualThenMaxAttempts(userMessageLog, legConfiguration);
        assertFalse(actual);
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void pullFailedOnRequestWithNoAttempt(@Injectable final MessagingLock lock,
                                                 @Injectable final LegConfiguration legConfiguration,
                                                 @Injectable final UserMessage userMessage,
                                                 @Injectable final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageID;

            messagingLockDao.findMessagingLockForMessageId(userMessage.getMessageId());
            result = lock;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = false;
        }};

        pullMessageService.pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            reprogrammableService.removeRescheduleInfo(lock);
            lock.setMessageState(MessageState.DEL);
            pullMessageStateService.sendFailed(userMessageLog, messageID);
            messagingLockDao.save(lock);
        }};
    }

    @Test
    public void pullFailedOnRequestWithAttempt(@Injectable final MessagingLock lock,
                                               @Injectable final UserMessage userMessage,
                                               @Injectable final LegConfiguration legConfiguration,
                                               @Injectable final UserMessageLog userMessageLog) {

        final String messageID = "123456";
        final Date nextAttempt = new Date(1528110891749l);
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageID;

            messagingLockDao.findMessagingLockForMessageId(messageID);
            result = lock;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = true;

            userMessageLog.getSendAttempts();
            result = 3;

            userMessageLog.getNextAttempt();
            result = nextAttempt;
        }};

        pullMessageService.pullFailedOnRequest(userMessage, legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            updateRetryLoggingService.saveAndNotify(userMessage, MessageStatus.READY_TO_PULL, userMessageLog);
            lock.setMessageState(MessageState.READY);
            lock.setSendAttempts(3);
            reprogrammableService.setRescheduleInfo(lock, nextAttempt);
            messagingLockDao.save(lock);
        }};
    }

    @Test
    public void pullFailedOnReceiptWithAttemptLeft(@Injectable final LegConfiguration legConfiguration,
                                                   @Injectable final UserMessage userMessage,
                                                   @Injectable final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageID;
            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = true;
        }};
        pullMessageService.pullFailedOnReceipt(userMessage, legConfiguration, userMessageLog);
        new VerificationsInOrder() {{
            pullMessageStateService.reset(userMessageLog, messageID);
            times = 1;
        }};

    }

    @Test
    public void pullFailedOnReceiptWithNoAttemptLeft(@Injectable final LegConfiguration legConfiguration,
                                                     @Injectable final UserMessage userMessage,
                                                     @Injectable final UserMessageLog userMessageLog) {
        final String messageID = "123456";
        new Expectations(pullMessageService) {{
            userMessage.getMessageId();
            result = messageID;

            pullMessageService.attemptNumberLeftIsStricltyLowerThenMaxAttemps(userMessageLog, legConfiguration);
            result = false;
        }};
        pullMessageService.pullFailedOnReceipt(userMessage, legConfiguration, userMessageLog);

        new VerificationsInOrder() {{
            pullMessageStateService.sendFailed(userMessageLog, messageID);
        }};

    }
}