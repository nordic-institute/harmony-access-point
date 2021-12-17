package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.*;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.dictionary.NotificationStatusDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class UserMessageLogDefaultServiceParameterizedTest {

    @Tested
    private UserMessageLogDefaultService userMessageLogDefaultService;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private BackendNotificationService backendNotificationService;

    @Injectable
    private UIReplicationSignalService uiReplicationSignalService;

    @Injectable
    private MessageStatusDao messageStatusDao;

    @Injectable
    private MshRoleDao mshRoleDao;
    @Injectable
    private NotificationStatusDao notificationStatusDao;

    @Parameterized.Parameter(0)
    public String service;

    @Parameterized.Parameter(1)
    public String action;

    @Parameterized.Parameters(name = "{index}: service=\"{0}\" action=\"{1}\"")
    public static Collection<Object[]> values() {
        return Arrays.asList(new Object[][]{
                {"service", "action"},
                {Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION}
        });
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

        UserMessage userMessage = new UserMessage();
        userMessage.setMessageId(messageId);
        userMessage.setTestMessage(true);
        MessageStatusEntity messageStatusEntity = new MessageStatusEntity();
        messageStatusEntity.setMessageStatus(MessageStatus.SEND_ENQUEUED);
        MSHRoleEntity mshRoleEntity = new MSHRoleEntity();
        NotificationStatusEntity notifStatus = new NotificationStatusEntity();
        new Expectations() {{
            messageStatusDao.findOrCreate(MessageStatus.valueOf(messageStatus));
            result = messageStatusEntity;
            mshRoleDao.findOrCreate(MSHRole.valueOf(mshRole));
            result = mshRoleEntity;
            notificationStatusDao.findOrCreate(NotificationStatus.valueOf(notificationStatus));
            result = notifStatus;
            messageStatusDao.findMessageStatus(MessageStatus.valueOf(messageStatus));
            result = messageStatusEntity;
        }};
        userMessageLogDefaultService.save(userMessage, messageStatus, notificationStatus, mshRole, maxAttempts, backendName);

        new Verifications() {{

            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            assertEquals(messageId, userMessage.getMessageId());
            assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            assertEquals(notifStatus, userMessageLog.getNotificationStatus());
            assertEquals(mshRoleEntity, userMessageLog.getMshRole());
            assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
            assertEquals(backendName, userMessageLog.getBackend());
        }};
    }
}
