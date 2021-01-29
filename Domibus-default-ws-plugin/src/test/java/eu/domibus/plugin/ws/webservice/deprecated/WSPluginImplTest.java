package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.common.*;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.ws.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.ws.webservice.WSMessageLogEntity;
import eu.domibus.plugin.ws.webservice.WSMessageLogDao;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

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
    public void deliverMessage(@Injectable DeliverMessageEvent deliverMessageEvent,
                               @Injectable WSMessageLogEntity wsMessageLogEntity) {
        new Expectations() {{
            deliverMessageEvent.getMessageId();
            result = MESSAGE_ID;
            deliverMessageEvent.toString();
            result = "MESSAGE_ID";
        }};

        wsPlugin.deliverMessage(deliverMessageEvent);

        new Verifications() {{
            wsMessageLogDao.create(withAny(wsMessageLogEntity));
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

        new FullVerifications() {
        };
    }

    @Test
    public void messageReceiveFailed(@Injectable MessageReceiveFailureEvent event) {

        wsPlugin.messageReceiveFailed(event);

        new FullVerifications() {
        };
    }

    @Test
    public void messageSendFailed(@Injectable MessageSendFailedEvent event) {

        wsPlugin.messageSendFailed(event);

        new Verifications() {
        };
    }

    @Test
    public void messageStatusChanged(@Injectable MessageStatusChangeEvent event) {
        wsPlugin.messageStatusChanged(event);

        new Verifications() {
        };
    }

    @Test
    public void messageDeletedBatchEvent(@Injectable MessageDeletedBatchEvent event) {
        List<String> messageIds = new ArrayList<>();

        wsPlugin.messageDeletedBatchEvent(event);

        new Verifications() {{
            wsMessageLogDao.deleteByMessageIds(messageIds);
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
            wsMessageLogDao.deleteByMessageId(MESSAGE_ID);
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