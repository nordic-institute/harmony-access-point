package eu.domibus.core.message;

import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.signal.SignalMessageLog;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.plugin.notification.NotificationStatus;
import eu.domibus.core.replication.UIReplicationSignalService;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
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

    @Test
    public void setSignalMessageAsDeleted_signalIsNull() {
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted((SignalMessage) null));
    }

    @Test
    public void setSignalMessageAsDeleted_infoIsNull(@Injectable final SignalMessage signalMessage) {
        new Expectations() {{
            signalMessage.getMessageInfo();
            result = null;
        }};
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {
        };
    }

    @Test
    public void setSignalMessageAsDeleted_messageIdIsNull(@Injectable final SignalMessage signalMessage,
                                                          @Injectable final MessageInfo messageInfo) {
        new Expectations() {{
            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = null;
        }};
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {
        };
    }

    @Test
    public void setSignalMessageAsDeleted_messageIdIsBlank(@Injectable final SignalMessage signalMessage,
                                                           @Injectable final MessageInfo messageInfo) {
        new Expectations() {{
            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = "";
        }};
        assertFalse(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {
        };
    }

    @Test
    public void setSignalMessageAsDeleted_ok(@Injectable final SignalMessage signalMessage,
                                             @Injectable final MessageInfo messageInfo,
                                             @Injectable final SignalMessageLog signalMessageLog) {
        String messageId = "1";

        new Expectations() {{
            signalMessage.getMessageInfo();
            result = messageInfo;

            messageInfo.getMessageId();
            result = messageId;

            signalMessageLogDao.findByMessageId(messageId);
            result = signalMessageLog;

        }};

        assertTrue(userMessageLogDefaultService.setSignalMessageAsDeleted(signalMessage));
        new FullVerifications() {{
            signalMessageLogDao.setMessageStatus(signalMessageLog, MessageStatus.DELETED);

            uiReplicationSignalService.messageChange(signalMessageLog.getMessageId());
        }};
    }

    @Test
    public void testSave() {
        final String messageId = "1";
        final String messageStatus = MessageStatus.SEND_ENQUEUED.toString();
        final String notificationStatus = NotificationStatus.NOTIFIED.toString();
        final String mshRole = MSHRole.SENDING.toString();
        final Integer maxAttempts = 10;
        final String mpc = " default";
        final String backendName = "JMS";
        final String endpoint = "http://localhost";

        userMessageLogDefaultService.save(messageId, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint, null, null, null, null);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));

            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            Assert.assertEquals(messageId, userMessageLog.getMessageId());
            Assert.assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            Assert.assertEquals(NotificationStatus.NOTIFIED, userMessageLog.getNotificationStatus());
            Assert.assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
            Assert.assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
            Assert.assertEquals(mpc, userMessageLog.getMpc());
            Assert.assertEquals(backendName, userMessageLog.getBackend());
            Assert.assertEquals(endpoint, userMessageLog.getEndpoint());
        }};
    }

    @Test
    public void testUpdateMessageStatus(@Injectable final UserMessageLog messageLog,
                                        @Injectable final UserMessage userMessage) {
        final String messageId = "1";
        final MessageStatus messageStatus = MessageStatus.SEND_ENQUEUED;

        new Expectations() {{
            messageLog.getMessageType();
            result = MessageType.USER_MESSAGE;

            messageLog.getMessageId();
            result = messageId;

            messageLog.isTestMessage();
            result = false;
        }};

        userMessageLogDefaultService.updateUserMessageStatus(userMessage, messageLog, messageStatus);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(userMessage, messageLog, messageStatus, withAny(new Timestamp(System.currentTimeMillis())));
            userMessageLogDao.setMessageStatus(messageLog, messageStatus);
            uiReplicationSignalService.messageChange(messageId);
        }};
    }

    @Test
    public void testSetMessageAsDeleted(@Injectable final UserMessage userMessage,
                                        @Injectable final UserMessageLog messageLog) {
        userMessageLogDefaultService.setMessageAsDeleted(userMessage, messageLog);

        new FullVerifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, messageLog, MessageStatus.DELETED);
        }};
    }

    @Test
    public void testSetMessageAsDownloaded(@Injectable UserMessage userMessage,
                                           @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsDownloaded(userMessage, userMessageLog);

        new FullVerifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.DOWNLOADED);
        }};
    }

    @Test
    public void testSetMessageAsAcknowledged(@Injectable UserMessage userMessage,
                                             @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsAcknowledged(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED);
        }};
    }

    @Test
    public void testSetMessageAsAckWithWarnings(@Injectable UserMessage userMessage,
                                                @Injectable UserMessageLog userMessageLog) {
        userMessageLogDefaultService.setMessageAsAckWithWarnings(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.ACKNOWLEDGED_WITH_WARNING);
        }};
    }

    @Test
    public void tesSetMessageAsSendFailure(@Injectable UserMessage userMessage,
                                           @Injectable UserMessageLog userMessageLog) {

        userMessageLogDefaultService.setMessageAsSendFailure(userMessage, userMessageLog);

        new Verifications() {{
            userMessageLogDefaultService.updateUserMessageStatus(userMessage, userMessageLog, MessageStatus.SEND_FAILURE);
        }};
    }

}
