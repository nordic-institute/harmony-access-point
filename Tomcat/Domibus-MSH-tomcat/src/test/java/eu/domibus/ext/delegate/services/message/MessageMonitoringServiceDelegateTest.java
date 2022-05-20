package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.message.UserMessageSecurityService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.usermessage.UserMessageRestoreService;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.ext.delegate.mapper.MessageExtMapper;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageMonitoringServiceDelegateTest {

    public static final String ORIGINAL_USER = "originalUser";
    @Tested
    MessageMonitoringServiceDelegate messageMonitoringServiceDelegate;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    MessageExtMapper messageExtMapper;

    @Injectable
    MessageAttemptService messageAttemptService;

    @Injectable
    UserMessageSecurityService userMessageSecurityService;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    UserMessageRestoreService restoreService;

    @Test
    public void testGetFailedMessages() {
        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = originalUserFromSecurityContext;
        }};

        messageMonitoringServiceDelegate.getFailedMessages();

        new Verifications() {{
            userMessageService.getFailedMessages(null, originalUserFromSecurityContext);
        }};
    }

    @Test
    public void testGetFailedMessagesForFinalRecipient() {
        final String finalRecipient = "C4";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = ORIGINAL_USER;
        }};

        messageMonitoringServiceDelegate.getFailedMessages(finalRecipient);

        new Verifications() {{
            userMessageService.getFailedMessages(finalRecipient, ORIGINAL_USER);
        }};
    }

    @Test
    public void testGetFailedMessageInterval() {
        final String messageId = "1";

        messageMonitoringServiceDelegate.getFailedMessageInterval(messageId);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorization(messageId);
            userMessageService.getFailedMessageElapsedTime(messageId);
        }};
    }

    @Test
    public void testRestoreFailedMessagesDuringPeriod() {

        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = originalUserFromSecurityContext;
        }};

        messageMonitoringServiceDelegate.restoreFailedMessagesDuringPeriod(1L, 2L);

        new Verifications() {{
            restoreService.restoreFailedMessagesDuringPeriod(1L, 2L, null, originalUserFromSecurityContext);
        }};
    }

    @Test
    public void testRestoreFailedMessage() {
        final String messageId = "1";

        messageMonitoringServiceDelegate.restoreFailedMessage(messageId);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorization(messageId);
            restoreService.restoreFailedMessage(messageId);
        }};
    }

    @Test
    public void testDeleteFailedMessage() {
        final String messageId = "1";

        messageMonitoringServiceDelegate.deleteFailedMessage(messageId);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorization(messageId);
            userMessageService.deleteFailedMessage(messageId);
        }};
    }

    @Test
    public void testGetAttemptsHistory(@Injectable final List<MessageAttempt> attemptsHistory) {
        final String messageId = "1";

        new Expectations() {{
            messageAttemptService.getAttemptsHistory(messageId);
            result = attemptsHistory;
        }};

        messageMonitoringServiceDelegate.getAttemptsHistory(messageId);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorization(messageId);
            messageExtMapper.messageAttemptToMessageAttemptDTO(attemptsHistory);
        }};
    }

    @Test
    public void deleteMessagesDuringPeriod() {

        final String originalUserFromSecurityContext = "C4";

        new Expectations() {{
            authUtils.getOriginalUser();
            result = originalUserFromSecurityContext;
        }};

        messageMonitoringServiceDelegate.deleteMessagesDuringPeriod(1L, 2L);

        new Verifications() {{
            userMessageService.deleteMessagesDuringPeriod(1L, 2L, originalUserFromSecurityContext);
        }};
    }

    @Test
    public void deleteMessageNotInFinalStatus() {
        final String messageId = "1";

        messageMonitoringServiceDelegate.deleteMessageNotInFinalStatus(messageId);

        new Verifications() {{
            userMessageSecurityService.checkMessageAuthorization(messageId);
            userMessageService.deleteMessageNotInFinalStatus(messageId);
        }};
    }
}
