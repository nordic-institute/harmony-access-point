package eu.domibus.plugin.webService.connector;

import eu.domibus.common.*;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import eu.domibus.plugin.webService.impl.StubDtoTransformer;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.plugin.webService.backend.WSBackendMessageType.*;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginImplTest {

    public static final String MESSAGE_ID = "messageId";

    @Tested
    private WSPluginImpl wsPlugin;

    @Injectable
    private StubDtoTransformer defaultTransformer;

    @Injectable
    private WSMessageLogDao wsMessageLogDao;

    @Injectable
    private WSPluginBackendService wsPluginBackendService;

    /**
     * {@link eu.domibus.plugin.AbstractBackendConnector} dependencies
     **/
    @Injectable
    protected MessageRetriever messageRetriever;

    @Injectable
    protected MessageSubmitter messageSubmitter;

    @Injectable
    protected MessagePuller messagePuller;

    @Injectable
    protected MessageExtService messageExtService;

    @Test
    public void deliverMessage(@Mocked DeliverMessageEvent deliverMessageEvent,
                               @Mocked WSMessageLogEntity wsMessageLogEntity) {
        new Expectations() {{
            deliverMessageEvent.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.deliverMessage(deliverMessageEvent);

        new Verifications() {{
            wsMessageLogDao.create(withAny(wsMessageLogEntity));
            times = 1;

            wsPluginBackendService.send(MESSAGE_ID, SUBMIT_MESSAGE, RECEIVE_SUCCESS);
            times = 1;
        }};
    }

    @Test
    public void sendSuccess(@Mocked MessageSendSuccessEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;

            wsPluginBackendService.send(MESSAGE_ID, WSBackendMessageType.SEND_SUCCESS);
            times = 1;
        }};
        wsPlugin.messageSendSuccess(event);

        new FullVerifications() {
        };
    }

    @Test
    public void messageReceiveFailed(@Mocked MessageReceiveFailureEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.messageReceiveFailed(event);

        new Verifications() {{
            wsPluginBackendService.send(MESSAGE_ID, RECEIVE_FAIL);
            times = 1;
        }};
    }

    @Test
    public void messageSendFailed(@Mocked MessageSendFailedEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.messageSendFailed(event);

        new Verifications() {{
            wsPluginBackendService.send(MESSAGE_ID, SEND_FAILURE);
            times = 1;
        }};
    }

    @Test
    public void messageStatusChanged(@Mocked MessageStatusChangeEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.messageStatusChanged(event);

        new Verifications() {{
            wsPluginBackendService.send(MESSAGE_ID, MESSAGE_STATUS_CHANGE);
            times = 1;
        }};
    }

    @Test
    public void messageDeletedBatchEvent(@Mocked MessageDeletedBatchEvent event) {
        List<String> messageIds = new ArrayList<>();
        new Expectations() {{
            event.getMessageIds();
            result = messageIds;
        }};

        wsPlugin.messageDeletedBatchEvent(event);

        new Verifications() {{
            wsMessageLogDao.deleteByMessageIds(messageIds);
            times = 1;

            wsPluginBackendService.send(messageIds, DELETED_BATCH);
            times = 1;
        }};
    }

    @Test
    public void messageDeletedEvent(@Mocked MessageDeletedEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.messageDeletedEvent(event);

        new Verifications() {{
            wsPluginBackendService.send(MESSAGE_ID, DELETED);
            times = 1;
        }};
    }

    @Test
    public void getMessageSubmissionTransformer() {
        assertEquals(defaultTransformer, wsPlugin.getMessageSubmissionTransformer());
    }

    @Test
    public void getMessageRetrievalTransformer() {
        assertEquals(defaultTransformer, wsPlugin.getMessageRetrievalTransformer());
    }
}