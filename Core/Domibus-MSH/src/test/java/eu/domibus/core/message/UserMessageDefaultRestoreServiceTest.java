package eu.domibus.core.message;

import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.audit.AuditService;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_RESEND_ALL_BATCH_COUNT_LIMIT;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_MESSAGE_RESEND_ALL_MAX_COUNT;

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
    private DomainTaskExecutor domainTaskExecutor;

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
    public void restoreSelectedFailedMessages(@Injectable Runnable task) {
        final String messageId = UUID.randomUUID().toString();
        final List<String> messageIds = new ArrayList<>();
        messageIds.add(messageId);
        final List<String> restoredMessages = new ArrayList<>();
        List<String> result;
        new Expectations() {{
            restoreService.restoreBatchMessages(restoredMessages, messageIds);
        }};

        result = restoreService.restoreAllOrSelectedFailedMessages(messageIds, "selected");

        new FullVerifications(restoreService) {{
            restoreService.validationForRestoreSelected(messageIds);
            domainTaskExecutor.submit((Runnable) any);
            Assert.assertEquals(messageIds.size(), result.size());
        }};
    }

    @Test
    public void restoreAllFailedMessages(@Injectable Runnable task) {
        final String messageId = UUID.randomUUID().toString();
        final List<String> messageIds = new ArrayList<>();
        messageIds.add(messageId);
        List<String> result;
        new Expectations(restoreService) {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_RESEND_ALL_MAX_COUNT);
            result = 10;
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_MESSAGE_RESEND_ALL_BATCH_COUNT_LIMIT);
            result = 5;
        }};

        result = restoreService.restoreAllOrSelectedFailedMessages(messageIds, "all");

        new FullVerifications(restoreService) {{
            restoreService.validationForRestoreAll((messageIds));
            domainTaskExecutor.submit((Runnable) any);
            Assert.assertEquals(result.size(), messageIds.size());
        }};
    }

}
