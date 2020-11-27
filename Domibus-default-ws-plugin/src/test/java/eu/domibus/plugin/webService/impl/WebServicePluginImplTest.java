package eu.domibus.plugin.webService.impl;

import eu.domibus.common.DeliverMessageEvent;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.MessageSendFailedEvent;
import eu.domibus.common.MessageSendSuccessEvent;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ext.services.*;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.dispatch.WSPluginBackendService;
import eu.domibus.plugin.webService.dao.WSMessageLogDao;
import eu.domibus.plugin.webService.entity.WSMessageLogEntity;
import eu.domibus.plugin.webService.generated.*;
import eu.domibus.plugin.webService.property.WSPluginPropertyManager;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.plugin.webService.backend.WSBackendMessageType.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.0.2
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WebServicePluginImplTest {

    public static final String RECIPIENT = "Recipient";

    public static final String MESSAGE_ID = "messageId";

    public static final String TO_RECIPIENT = "to_recipient";

    @Tested
    private WebServicePluginImpl webServicePlugin;

    @Injectable
    private StubDtoTransformer defaultTransformer;

    @Injectable
    private MessageAcknowledgeExtService messageAcknowledgeExtService;

    @Injectable
    private MessageRetriever messageRetriever;

    @Injectable
    private MessageSubmitter messageSubmitter;

    @Injectable
    private MessagePuller messagePuller;

    @Injectable
    private MessageExtService messageExtService;

    @Injectable
    private String name;

    @Injectable
    protected WebServicePluginExceptionFactory webServicePluginExceptionFactory;

    @Injectable
    protected WSMessageLogDao wsMessageLogDao;

    @Injectable
    protected DomainExtService domainExtService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Injectable
    protected WSPluginPropertyManager wsPluginPropertyManager;

    @Injectable
    private AuthenticationExtService authenticationExtService;

    @Injectable
    private WSPluginBackendService wsPluginBackendService;

    @Injectable
    private UserMessageExtService userMessageExtService;


    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadsAndBodyLoad(@Injectable SubmitRequest submitRequest,
                                                             @Injectable Messaging ebMSHeaderInfo,
                                                             @Injectable LargePayloadType payload1,
                                                             @Injectable LargePayloadType payload2,
                                                             @Injectable LargePayloadType bodyLoad) throws SubmitMessageFault {
        List<LargePayloadType> payloadList = new ArrayList<>();
        payloadList.add(payload1);
        payloadList.add(payload2);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloadList;

            payload1.getPayloadId();
            result = "cid:message1";

            payload2.getPayloadId();
            result = "cid:message2";

            submitRequest.getBodyload();
            result = bodyLoad;

            bodyLoad.getPayloadId();
            result = "null";
        }};

        webServicePlugin.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithMissingPayloadIdForPayload(@Injectable SubmitRequest submitRequest,
                                                                    @Injectable Messaging ebMSHeaderInfo,
                                                                    @Injectable LargePayloadType payload1) throws SubmitMessageFault {
        List<LargePayloadType> payloadList = new ArrayList<>();
        payloadList.add(payload1);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloadList;

            payload1.getPayloadId();
            result = null;
        }};

        webServicePlugin.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadIdAddedForBodyLoad(@Injectable SubmitRequest submitRequest,
                                                                   @Injectable Messaging ebMSHeaderInfo,
                                                                   @Injectable LargePayloadType bodyLoad) throws SubmitMessageFault {

        new Expectations() {{
            submitRequest.getBodyload();
            result = bodyLoad;

            bodyLoad.getPayloadId();
            result = "cid:message";
        }};

        webServicePlugin.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }

    @Test
    public void test_SubmitMessage_MessageIdHavingEmptySpaces(@Injectable SubmitRequest submitRequest,
                                                              @Injectable RetrieveMessageResponse retrieveMessageResponse,
                                                              @Injectable Messaging ebMSHeaderInfo) throws SubmitMessageFault {
        new Expectations() {{
            ebMSHeaderInfo.getUserMessage().getMessageInfo().getMessageId();
            result = "-Dom137-- ";

        }};

        // backendWebService.retrieveMessage(retrieveMessageRequest, new Holder<RetrieveMessageResponse>(retrieveMessageResponse), new Holder<>(ebMSHeaderInfo));

        webServicePlugin.submitMessage(submitRequest, ebMSHeaderInfo);

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom137-- ", messageId);
        }};
    }

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheMessageByItsIdentifier(@Injectable RetrieveMessageRequest retrieveMessageRequest,
                                                                                    @Injectable RetrieveMessageResponse retrieveMessageResponse,
                                                                                    @Injectable Messaging ebMSHeaderInfo,
                                                                                    @Injectable MessageLister lister) throws RetrieveMessageFault, MessageNotFoundException {
        new Expectations(webServicePlugin) {{
            retrieveMessageRequest.getMessageID();
            result = "-Dom137--";
            lister.removeFromPending(anyString);
            result = null;
        }};

        webServicePlugin.setLister(lister);
        webServicePlugin.retrieveMessage(retrieveMessageRequest, new Holder<>(retrieveMessageResponse), new Holder<>(ebMSHeaderInfo));

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom137--", messageId);
        }};
    }

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheStatusOfAMessageByItsIdentifier(@Injectable StatusRequest statusRequest) throws StatusFault {
        new Expectations() {{
            statusRequest.getMessageID();
            result = "-Dom138--";
        }};

        webServicePlugin.getStatus(statusRequest);

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom138--", messageId);
        }};
    }
    @Test
    public void deliverMessage(@Mocked DeliverMessageEvent deliverMessageEvent,
                               @Mocked WSMessageLogEntity wsMessageLogEntity) {
        new Expectations(webServicePlugin) {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = RECIPIENT;

            deliverMessageEvent.getMessageId();
            result = MESSAGE_ID;
        }};

        webServicePlugin.deliverMessage(deliverMessageEvent);

        new Verifications() {{
            wsMessageLogDao.create(withAny(wsMessageLogEntity));
            times = 1;

            wsPluginBackendService.sendNotification(RECEIVE_SUCCESS, MESSAGE_ID, RECIPIENT);
            times = 1;
        }};
    }

    @Test
    public void sendSuccess(@Mocked MessageSendSuccessEvent event) {
        new Expectations(webServicePlugin) {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = RECIPIENT;

            event.getMessageId();
            result = MESSAGE_ID;

            wsPluginBackendService.sendNotification(WSBackendMessageType.SEND_SUCCESS, MESSAGE_ID, RECIPIENT);
            times = 1;
        }};
        webServicePlugin.messageSendSuccess(event);

        new FullVerifications() {
        };
    }

    @Test
    public void messageReceiveFailed(@Mocked MessageReceiveFailureEvent event,
                               @Mocked WSMessageLogEntity wsMessageLogEntity) {
        new Expectations(webServicePlugin) {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = RECIPIENT;

            event.getMessageId();
            result = MESSAGE_ID;
        }};

        webServicePlugin.messageReceiveFailed(event);

        new Verifications() {{
            wsPluginBackendService.sendNotification(RECEIVE_FAIL, MESSAGE_ID, RECIPIENT);
            times = 1;
        }};
    }

    @Test
    public void messageSendFailed(@Mocked MessageSendFailedEvent event,
                               @Mocked WSMessageLogEntity wsMessageLogEntity) {
        new Expectations(webServicePlugin) {{
            userMessageExtService.getFinalRecipient(MESSAGE_ID);
            times = 1;
            result = RECIPIENT;

            event.getMessageId();
            result = MESSAGE_ID;
        }};

        webServicePlugin.messageSendFailed(event);

        new Verifications() {{
            wsPluginBackendService.sendNotification(SEND_FAILURE, MESSAGE_ID, RECIPIENT);
            times = 1;
        }};
    }
}