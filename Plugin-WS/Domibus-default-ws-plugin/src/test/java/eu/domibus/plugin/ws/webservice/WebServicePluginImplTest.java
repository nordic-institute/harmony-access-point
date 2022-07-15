package eu.domibus.plugin.ws.webservice;

import eu.domibus.common.MessageStatus;
import eu.domibus.ext.services.*;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogService;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.generated.StatusFault;
import eu.domibus.plugin.ws.generated.SubmitMessageFault;
import eu.domibus.plugin.ws.generated.body.*;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.plugin.ws.message.WSMessageLogService;
import eu.domibus.plugin.ws.property.WSPluginPropertyManager;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Cosmin Baciu
 * @since 4.0.2
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WebServicePluginImplTest {

    public static final String MESSAGE_ID = "messageId";

    @Tested
    private WebServiceImpl webServicePlugin;

    @Injectable
    private MessageAcknowledgeExtService messageAcknowledgeExtService;

    @Injectable
    private WSBackendMessageLogService wsBackendMessageLogService;

    @Injectable
    protected WebServiceExceptionFactory webServicePluginExceptionFactory;

    @Injectable
    protected WSMessageLogService wsMessageLogService;

    @Injectable
    private DomainContextExtService domainContextExtService;

    @Injectable
    protected WSPluginPropertyManager wsPluginPropertyManager;

    @Injectable
    private AuthenticationExtService authenticationExtService;

    @Injectable
    protected MessageExtService messageExtService;

    @Injectable
    private WSPluginImpl wsPlugin;


    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadsAndBodyLoad(@Injectable SubmitRequest submitRequest,
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

        webServicePlugin.validateSubmitRequest(submitRequest);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithMissingPayloadIdForPayload(@Injectable SubmitRequest submitRequest,
                                                                    @Injectable LargePayloadType payload1) throws SubmitMessageFault {
        List<LargePayloadType> payloadList = new ArrayList<>();
        payloadList.add(payload1);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloadList;

            payload1.getPayloadId();
            result = null;
        }};

        webServicePlugin.validateSubmitRequest(submitRequest);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadIdAddedForBodyLoad(@Injectable SubmitRequest submitRequest,
                                                                   @Injectable LargePayloadType bodyLoad) throws SubmitMessageFault {

        new Expectations() {{
            submitRequest.getBodyload();
            result = bodyLoad;

            bodyLoad.getPayloadId();
            result = "cid:message";
        }};

        webServicePlugin.validateSubmitRequest(submitRequest);
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
    public void test_SubmitMessage_PayloadAsFilepathReference(@Injectable SubmitRequest submitRequest,
                                                              @Injectable RetrieveMessageResponse retrieveMessageResponse,

                                                              @Mocked PayloadInfo payloadInfo,
                                                              @Mocked DataHandler dataHandler) throws SubmitMessageFault, MalformedURLException, MessagingProcessingException {
        final PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");
        PartProperties partProperties = new PartProperties();
        partInfo.setPartProperties(partProperties);
        Property mimeTypeProperty = new Property();
        mimeTypeProperty.setName("MimeType");
        mimeTypeProperty.setValue("text/xml");
        Property filepathProperty = new Property();
        filepathProperty.setName("filepath");
        String filepathValue = "file:/some/path/someFile.txt";
        filepathProperty.setValue(filepathValue);
        List<Property> properties = new ArrayList<>();
        properties.add(mimeTypeProperty);
        properties.add(filepathProperty);
        ReflectionTestUtils.setField(partProperties, "property", properties);
        partInfo.setHref("cid:message");

        Messaging ebMSHeaderInfo = new Messaging();
        UserMessage userMessage = new UserMessage();
        ebMSHeaderInfo.setUserMessage(userMessage);
        MessageInfo messageInfo = new MessageInfo();
        userMessage.setMessageInfo(messageInfo);
        messageInfo.setMessageId("-Dom137-- ");
        userMessage.setPayloadInfo(payloadInfo);

        new Expectations() {{
//            ebMSHeaderInfo.getUserMessage().getMessageInfo().getMessageId();
//            result = "-Dom137-- ";
//            ebMSHeaderInfo.getUserMessage().getPayloadInfo();
//            result = payloadInfo;
            payloadInfo.getPartInfo();
            ArrayList<Object> partInfoList = new ArrayList<>();
            partInfoList.add(partInfo);
            result = partInfoList;

//            new DataHandler(new URLDataSource(new URL(filepathValue)));
//            result = dataHandler;

//            wsPlugin.submit(ebMSHeaderInfo);
//            result = "someMessageId";
        }};

        webServicePlugin.submitMessage(submitRequest, ebMSHeaderInfo);

        new Verifications() {{
            Messaging ebMSHeaderInfo;
            wsPlugin.submit(ebMSHeaderInfo = withCapture());
            assertNotNull(ebMSHeaderInfo.getUserMessage().getPayloadInfo().getPartInfo());
        }};
    }

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheMessageByItsIdentifier(@Injectable RetrieveMessageRequest retrieveMessageRequest,
                                                                                    @Injectable RetrieveMessageResponse retrieveMessageResponse,
                                                                                    @Injectable Messaging ebMSHeaderInfo) throws RetrieveMessageFault {
        new Expectations(webServicePlugin) {{
            retrieveMessageRequest.getMessageID();
            result = "-Dom137--";
        }};

//        webServicePlugin.setLister(lister);
        webServicePlugin.retrieveMessage(retrieveMessageRequest, new Holder<>(retrieveMessageResponse), new Holder<>(ebMSHeaderInfo));

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom137--", messageId);
        }};
    }

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheStatusOfAMessageByItsIdentifier(
            @Injectable StatusRequest statusRequest,
            @Injectable MessageRetrieverExtService messageRetriever) throws StatusFault {
        new Expectations() {{
            statusRequest.getMessageID();
            result = MESSAGE_ID;
            times = 2;

            messageExtService.cleanMessageIdentifier(MESSAGE_ID);
            result = MESSAGE_ID;
            times = 1;

            wsPlugin.getMessageRetriever();
            result = messageRetriever;
            times = 1;

            messageRetriever.getStatus(MESSAGE_ID);
            result = MessageStatus.ACKNOWLEDGED;
            times = 1;
        }};

        webServicePlugin.getStatus(statusRequest);

        new FullVerifications() {};
    }

}
