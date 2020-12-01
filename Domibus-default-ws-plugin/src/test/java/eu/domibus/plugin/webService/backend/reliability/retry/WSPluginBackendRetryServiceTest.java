package eu.domibus.plugin.webService.backend.reliability.retry;

import eu.domibus.ext.domain.JmsMessageDTO;
import eu.domibus.ext.services.JMSExtService;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogDao;
import eu.domibus.plugin.webService.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.queue.WSSendMessageListener;
import eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRule;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
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
    public static final String FINAL_RECIPIENT = "finalRecipient";
    public static final String ORIGINAL_SENDER = "originalSender";
    public static final String MESSAGE_ID = "messageId";
    public static final int RETRY_MAX = 10;
    public static final String RULE_NAME = "ruleName";
    public static final long BACKEND_MESSAGE_ID = 1L;
    public static final long BACKEND_MESSAGE_ID2 = 2L;
    public static final String MESSAGE_ID_2 = "messageId2";

    @Tested
    private WSPluginBackendRetryService retryService;
    @Injectable
    private WSBackendMessageLogDao wsBackendMessageLogDao;

    @Injectable
    protected JMSExtService jmsExtService;

    @Injectable
    protected Queue wsPluginSendQueue;


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
        //backendMessage cannot be mocked because of the constructor in the code.
        WSBackendMessageLogEntity backendMessage = new WSBackendMessageLogEntity();
        backendMessage.setMessageId(MESSAGE_ID);
        backendMessage.setEntityId(BACKEND_MESSAGE_ID);
        backendMessage.setType(WSBackendMessageType.SEND_SUCCESS);
        //Since we are making a capture of WSBackendMessageLogEntity, we can not use a Mock object here
        new Expectations() {{
            wsBackendMessageLogDao.createEntity(withCapture(backendMessages));
            result = backendMessage;
            times = 1;

            rule.getRuleName();
            result = RULE_NAME;

            rule.getRetryCount();
            result = RETRY_MAX;
            times = 1;
        }};

        retryService.send(MESSAGE_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, rule, WSBackendMessageType.SEND_SUCCESS);

        new Verifications() {{
            JmsMessageDTO jmsMessageDTO;
            jmsExtService.sendMessageToQueue(jmsMessageDTO = withCapture(), wsPluginSendQueue);
            assertEquals(MESSAGE_ID, jmsMessageDTO.getProperties().get(MessageConstants.MESSAGE_ID));
            assertEquals(BACKEND_MESSAGE_ID, jmsMessageDTO.getProperties().get(WSSendMessageListener.ID));
            assertEquals(WSBackendMessageType.SEND_SUCCESS.name(), jmsMessageDTO.getProperties().get(WSSendMessageListener.TYPE));
        }};

        assertEquals(1, backendMessages.size());

        WSBackendMessageLogEntity wsBackendMessageLogEntity = backendMessages.get(0);
        Assert.assertEquals(MESSAGE_ID, wsBackendMessageLogEntity.getMessageId());
        Assert.assertEquals(RULE_NAME, wsBackendMessageLogEntity.getRuleName());
        Assert.assertEquals(FINAL_RECIPIENT, wsBackendMessageLogEntity.getFinalRecipient());
        Assert.assertEquals(ORIGINAL_SENDER, wsBackendMessageLogEntity.getOriginalSender());
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

            entity1.getMessageId();
            result = MESSAGE_ID;

            entity1.getEntityId();
            result = BACKEND_MESSAGE_ID;

            entity1.getType();
            result = WSBackendMessageType.SEND_SUCCESS;

            entity2.getMessageId();
            result = MESSAGE_ID_2;

            entity2.getEntityId();
            result = BACKEND_MESSAGE_ID2;

            entity2.getType();
            result = WSBackendMessageType.SEND_FAILURE;
        }};
        retryService.sendWaitingForRetry();

        new FullVerifications() {{

            List<JmsMessageDTO> jmsMessageDTO = new ArrayList<>();
            jmsExtService.sendMessageToQueue(withCapture(jmsMessageDTO), wsPluginSendQueue);

            assertEquals(MESSAGE_ID, jmsMessageDTO.get(0).getProperties().get(MessageConstants.MESSAGE_ID));
            assertEquals(BACKEND_MESSAGE_ID, jmsMessageDTO.get(0).getProperties().get(WSSendMessageListener.ID));
            assertEquals(WSBackendMessageType.SEND_SUCCESS.name(), jmsMessageDTO.get(0).getProperties().get(WSSendMessageListener.TYPE));

            assertEquals(MESSAGE_ID_2, jmsMessageDTO.get(1).getProperties().get(MessageConstants.MESSAGE_ID));
            assertEquals(BACKEND_MESSAGE_ID2, jmsMessageDTO.get(1).getProperties().get(WSSendMessageListener.ID));
            assertEquals(WSBackendMessageType.SEND_FAILURE.name(), jmsMessageDTO.get(1).getProperties().get(WSSendMessageListener.TYPE));

        }};
    }
}