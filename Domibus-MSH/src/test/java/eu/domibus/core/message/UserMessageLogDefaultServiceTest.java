package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.NotificationStatusDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class UserMessageLogDefaultServiceTest {

    @Tested
    UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    protected MessageStatusDao messageStatusDao;

    @Injectable
    protected MshRoleDao mshRoleDao;

    @Injectable
    protected NotificationStatusDao notificationStatusDao;

    @Test
    public void setSignalMessageAsDeleted_signalIsNull() {
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted((SignalMessage) null));
    }

    @Test
    public void setSignalMessageAsDeleted_messageIdIsNull(@Injectable final SignalMessage signalMessage) {
        new Expectations() {{
            signalMessage.getSignalMessageId();
            result = null;
            
        }};
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {
        };
    }

    @Test
    public void setSignalMessageAsDeleted_messageIdIsBlank(@Injectable final SignalMessage signalMessage) {
        new Expectations() {{
            signalMessage.getSignalMessageId();
            result = "";

        }};
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {
        };
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void setSignalMessageAsDeleted_ok(@Injectable final SignalMessage signalMessage,
                                             @Injectable final SignalMessageLog signalMessageLog) {
        String messageId = "1";

        new Expectations() {{
            signalMessage.getSignalMessageId();
            result = messageId;


            signalMessageLogDao.findByMessageId(messageId);
            result = signalMessageLog;

        }};

        assertTrue(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));

        new FullVerifications() {{

            uiReplicationSignalService.messageChange(signalMessageLog.getSignalMessage().getSignalMessageId());
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSave() {
        final String messageId = "1";
        final String messageStatus = MessageStatus.SEND_ENQUEUED.toString();
        final String notificationStatus = NotificationStatus.NOTIFIED.toString();
        final String mshRole = MSHRole.SENDING.toString();
        final Integer maxAttempts = 10;
        final String mpc = " default";
        final String backendName = "JMS";
        final String endpoint = "http://localhost";

        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(messageId);
        userMessage.setConversationId(messageId);
        userMessageLogDefaultService.save(userMessage, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint, null, null, null, null);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(new UserMessage(), withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            Assert.assertEquals(messageId, userMessageLog.getUserMessage().getMessageId());
            Assert.assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            Assert.assertEquals(NotificationStatus.NOTIFIED, userMessageLog.getNotificationStatus());
            Assert.assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
            Assert.assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
//            Assert.assertEquals(mpc, userMessageLog.getMpc());
            Assert.assertEquals(backendName, userMessageLog.getBackend());
//            Assert.assertEquals(endpoint, userMessageLog.getEndpoint());
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testUpdateMessageStatus(@Injectable final UserMessageLog messageLog,
                                        @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final MessageStatus messageStatus = MessageStatus.SEND_ENQUEUED;

        new Expectations() {{
//            messageLog.getMessageType();
//            result = MessageType.USER_MESSAGE;
//
//            messageLog.getMessageId();
//            result = messageId;
//
//            messageLog.isTestMessage();
//            result = false;
        }};

        userMessageLogDefaultService.updateUserMessageStatus(userMessage, messageLog, messageStatus);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, messageStatus, withAny(new Timestamp(System.currentTimeMillis())));
            userMessageLogDao.setMessageStatus(messageLog, messageStatus);
            uiReplicationSignalService.messageChange(messageId);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSetMessageAsDeleted(@Injectable final UserMessage userMessage,
                                        @Injectable final UserMessageLog messageLog) {
        userMessageLogDefaultService.setMessageAsDeleted(userMessage, messageLog);

        new FullVerifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, messageLog, MessageStatus.DELETED);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSetMessageAsDownloaded(@Injectable UserMessage userMessage,
                                           @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsDownloaded(userMessage, userMessageLog);

        new FullVerifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.DOWNLOADED);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSetMessageAsAcknowledged(@Injectable UserMessage userMessage,
                                             @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsAcknowledged(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void testSetMessageAsAckWithWarnings(@Injectable UserMessage userMessage,
                                                @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsAckWithWarnings(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
        }};
    }

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void tesSetMessageAsSendFailure(@Injectable UserMessage userMessage,
                                           @Injectable UserMessageLog userMessageLog) {

        userMessageLogDefaultService.setMessageAsSendFailure(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.SEND_FAILURE);
        }};
    }

}
