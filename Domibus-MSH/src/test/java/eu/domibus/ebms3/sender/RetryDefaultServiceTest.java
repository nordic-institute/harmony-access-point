
package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pull.MessagingLockDao;
import eu.domibus.core.pull.PullMessageService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Ioana Dragusanu,
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class RetryDefaultServiceTest {
    private static List<String> QUEUED_MESSAGEIDS = Arrays.asList("queued123@domibus.eu", "queued456@domibus.eu", "queued789@domibus.eu");

    @Tested
    private RetryDefaultService retryService;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private Queue sendLargeMessageQueue;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private PullMessageService pullMessageService;

    @Injectable
    private JMSManager jmsManager;

    @Injectable
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    private MessagingLockDao messagingLockDao;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UpdateRetryLoggingService updateRetryLoggingService;

    private List<JmsMessage> getQueuedMessages() {
        List<JmsMessage> jmsMessages = new ArrayList<>();
        for (String messageId : QUEUED_MESSAGEIDS) {
            JmsMessage jmsMessage = new JmsMessage();
            jmsMessage.setProperty(MessageConstants.MESSAGE_ID, messageId);
            jmsMessages.add(jmsMessage);
        }
        return jmsMessages;
    }

    @Test
    public void getMessagesNotAlreadyQueuedWithNoAlreadyQueuedMessagesTest() {
        List<String> retryMessageIds = Arrays.asList("retry123@domibus.eu", "retry456@domibus.eu", "expired123@domibus.eu");

        new NonStrictExpectations(retryService) {{
            userMessageLogDao.findRetryMessages();
            result = new ArrayList<>(retryMessageIds);
        }};

        List<String> result = retryService.getMessagesNotAlreadyScheduled();
        assertEquals(3, result.size());

        assertEquals(result, retryMessageIds);
    }

    @Test
    public void test_failIfExpired_MessageExpired_NotSourceMessage(final @Mocked UserMessage userMessage) throws Exception {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = true;

            userMessage.isUserMessageFragment();
            result = false;
        }};

        //tested method
        boolean result = retryService.failIfExpired(userMessage);
        Assert.assertTrue(result);

        new FullVerifications() {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);
        }};
    }

    @Test
    public void test_failIfExpired_ExceptionThrown(final @Mocked UserMessage userMessage) throws Exception {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, null, UUID.randomUUID().toString(), null);
        }};

        //tested method
        boolean result = retryService.failIfExpired(userMessage);
        Assert.assertFalse(result);

        new FullVerifications() {{
        }};
    }

    @Test
    public void test_failIfExpired_MessageExpired_SourceMessage(final @Mocked UserMessage userMessage) throws Exception {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations(retryService) {{
            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = true;

            userMessage.isUserMessageFragment();
            result = true;
        }};

        //tested method
        boolean result = retryService.failIfExpired(userMessage);
        Assert.assertTrue(result);

        new FullVerifications(retryService) {{
            updateRetryLoggingService.messageFailed(userMessage, userMessageLog);

            userMessageService.scheduleSplitAndJoinSendFailed(anyString, anyString);
        }};
    }

    @Test
    public void test_failIfExpired_MessageNotExpired_NotSourceMessage(final @Mocked UserMessage userMessage) throws Exception {
        final String messageId = "expired123@domibus.eu";
        final String pModeKey = "pModeKey";

        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setSendAttempts(2);
        userMessageLog.setSendAttemptsMax(3);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RETRY);

        final LegConfiguration legConfiguration = new LegConfiguration();
        legConfiguration.setName("myLegConfiguration");

        new Expectations() {{
            userMessage.getMessageInfo().getMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId, MSHRole.SENDING);
            result = userMessageLog;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            updateRetryLoggingService.isExpired(legConfiguration, userMessageLog);
            result = false;
        }};

        //tested method
        boolean result = retryService.failIfExpired(userMessage);
        Assert.assertFalse(result);

        new FullVerifications() {{
        }};
    }

}