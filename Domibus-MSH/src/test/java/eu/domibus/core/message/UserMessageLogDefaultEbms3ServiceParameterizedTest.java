package eu.domibus.core.message;

import eu.domibus.api.ebms3.Ebms3Constants;
import eu.domibus.api.model.*;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.replication.UIReplicationSignalService;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
@RunWith(Parameterized.class)
public class UserMessageLogDefaultEbms3ServiceParameterizedTest {

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
                {"service","action"},
                {Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION}
        });
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
        userMessageLogDefaultService.save(userMessage, messageStatus, notificationStatus, mshRole, maxAttempts, mpc, backendName, endpoint, service, action, null, null);

        new Verifications() {{
            backendNotificationService.notifyOfMessageStatusChange(userMessage, withAny(new UserMessageLog()), MessageStatus.SEND_ENQUEUED, withAny(new Timestamp(System.currentTimeMillis())));
//            times = userMessageLogDefaultService.checkTestMessage(service,action)?0:1;

            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            Assert.assertEquals(messageId, userMessage.getMessageId());
            Assert.assertEquals(MessageStatus.SEND_ENQUEUED, userMessageLog.getMessageStatus());
            Assert.assertEquals(NotificationStatus.NOTIFIED, userMessageLog.getNotificationStatus());
            Assert.assertEquals(MSHRole.SENDING, userMessageLog.getMshRole());
            Assert.assertEquals(maxAttempts.intValue(), userMessageLog.getSendAttemptsMax());
            Assert.assertEquals(backendName, userMessageLog.getBackend());
        }};
    }
}
