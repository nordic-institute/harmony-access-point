package eu.domibus.plugin.ws.connector;

import eu.domibus.common.*;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.ws.webservice.WSMessageLogService;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.ws.webservice.WSMessageLogEntity;
import eu.domibus.plugin.ws.webservice.StubDtoTransformer;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.plugin.ws.backend.WSBackendMessageType.*;
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
    private WSMessageLogService wsMessageLogService;

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
    public void deliverMessage(@Injectable DeliverMessageEvent deliverMessageEvent,
                               @Injectable WSMessageLogEntity wsMessageLogEntity) {
        new Expectations() {{
            deliverMessageEvent.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.deliverMessage(deliverMessageEvent);

        new Verifications() {{
            wsMessageLogService.create(withAny(wsMessageLogEntity));
            times = 1;

            //wsPluginBackendService.send(MESSAGE_ID, wsPluginBackendService.userMessageExtService.getFinalRecipient(MESSAGE_ID), wsPluginBackendService.userMessageExtService.getOriginalSender(MESSAGE_ID), SUBMIT_MESSAGE, RECEIVE_SUCCESS);
            times = 1;
        }};
    }

    @Test
    public void sendSuccess(@Injectable MessageSendSuccessEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};
        wsPlugin.messageSendSuccess(event);

        new FullVerifications() {{
            wsPluginBackendService.send(event, SEND_SUCCESS);
            times = 1;
        }};
    }

    @Test
    public void messageReceiveFailed(@Injectable MessageReceiveFailureEvent event) {

        wsPlugin.messageReceiveFailed(event);

        new Verifications() {{
            wsPluginBackendService.send(event, RECEIVE_FAIL);
            times = 1;
        }};
    }

    @Test
    public void messageSendFailed(@Injectable MessageSendFailedEvent event) {

        wsPlugin.messageSendFailed(event);

        new Verifications() {{
            wsPluginBackendService.send(event, SEND_FAILURE);
            times = 1;
        }};
    }

    @Test
    public void messageStatusChanged(@Injectable MessageStatusChangeEvent event) {

        wsPlugin.messageStatusChanged(event);

        new Verifications() {{
            wsPluginBackendService.send(event, MESSAGE_STATUS_CHANGE);
            times = 1;
        }};
    }

    @Test
    public void messageDeletedBatchEvent(@Injectable MessageDeletedBatchEvent event) {
        List<String> messageIds = new ArrayList<>();

        wsPlugin.messageDeletedBatchEvent(event);

        new Verifications() {{
            wsMessageLogService.deleteByMessageIds(messageIds);
            times = 1;

            wsPluginBackendService.send(event, DELETED_BATCH);
            times = 1;
        }};
    }

    @Test
    public void messageDeletedEvent(@Injectable MessageDeletedEvent event) {
        new Expectations() {{
            event.getMessageId();
            result = MESSAGE_ID;
        }};

        wsPlugin.messageDeletedEvent(event);

        new FullVerifications() {{
            wsMessageLogService.deleteByMessageId(MESSAGE_ID);
            times = 1;

            wsPluginBackendService.send(event, DELETED);
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