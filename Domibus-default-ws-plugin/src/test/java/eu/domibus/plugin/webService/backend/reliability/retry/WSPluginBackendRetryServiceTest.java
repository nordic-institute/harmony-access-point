package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginMessageSender;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginBackendRetryServiceTest {
    public static final String RECIPIENT = "recipient";
    public static final String MESSAGE_ID = "messageId";
    public static final int RETRY_MAX = 10;
    public static final String RULE_NAME = "ruleName";

    @Tested
    private WSPluginBackendRetryService retryService;
    @Injectable
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @Injectable
    private WSPluginMessageSender wsPluginMessageSender;

    @Test
    public void getMessagesNotAlreadyScheduled() {
        List<WSBackendMessageLogEntity> wsBackendMessageLogEntities = Collections.singletonList(new WSBackendMessageLogEntity());
        new Expectations() {{
            wsBackendMessageLogDao.findRetryMessages();
            result = wsBackendMessageLogEntities;
            times = 1;
        }};
        List<WSBackendMessageLogEntity> messagesNotAlreadyScheduled = retryService.getMessagesNotAlreadyScheduled();
        assertEquals(1, messagesNotAlreadyScheduled.size());
        assertNotNull(messagesNotAlreadyScheduled.get(0));
    }

    @Test
    public void getMessagesNotAlreadyScheduled_empty() {
        new Expectations() {{
            wsBackendMessageLogDao.findRetryMessages();
            result = new ArrayList<>();
            times = 1;
        }};
        assertEquals(0, retryService.getMessagesNotAlreadyScheduled().size());
    }

    @Test
    public void sendNotification(@Mocked WSPluginDispatchRule rule) {
        List<WSBackendMessageLogEntity> backendMessages = new ArrayList<>();
        //Since we are making a capture of WSBackendMessageLogEntity, we can not use a Mock object here
        WSBackendMessageLogEntity backendMessage = new WSBackendMessageLogEntity();
        new Expectations() {{
            wsBackendMessageLogDao.createEntity(withCapture(backendMessages));
            result = backendMessage;
            times = 1;

            rule.getRuleName();
            result = RULE_NAME;
            times = 1;

            rule.getRetryCount();
            result = RETRY_MAX;
            times = 1;
        }};

        retryService.sendNotification(MESSAGE_ID, RECIPIENT, rule);

        new Verifications() {{
            wsPluginMessageSender.sendNotification(backendMessage);
            times = 1;
        }};

        assertEquals(1, backendMessages.size());

        WSBackendMessageLogEntity wsBackendMessageLogEntity = backendMessages.get(0);
        Assert.assertEquals(MESSAGE_ID, wsBackendMessageLogEntity.getMessageId());
        Assert.assertEquals(RULE_NAME, wsBackendMessageLogEntity.getRuleName());
        Assert.assertEquals(RECIPIENT, wsBackendMessageLogEntity.getFinalRecipient());
        Assert.assertEquals(WSBackendMessageType.SEND_SUCCESS, wsBackendMessageLogEntity.getType());
        Assert.assertEquals(0, wsBackendMessageLogEntity.getSendAttempts());
        Assert.assertEquals(RETRY_MAX, wsBackendMessageLogEntity.getSendAttemptsMax());
    }

    @Test
    public void sendNotifications(@Mocked WSBackendMessageLogEntity entity1, @Mocked WSBackendMessageLogEntity entity2) {
        List<WSBackendMessageLogEntity> entities = Arrays.asList(entity1, entity2);
        new Expectations(retryService) {{
            retryService.getMessagesNotAlreadyScheduled();
            result = entities;
            times = 1;
        }};
        retryService.sendNotifications();

        new FullVerifications(){{
            wsPluginMessageSender.sendNotification(entity1);
            times = 1;

            wsPluginMessageSender.sendNotification(entity2);
            times = 1;
        }};
    }
}