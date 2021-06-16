package eu.domibus.plugin.ws.backend.dispatch;

import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import eu.domibus.plugin.ws.generated.header.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.UserMessage;
import eu.domibus.ext.services.XMLUtilExtService;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.ws.backend.WSBackendMessageLogEntity;
import eu.domibus.plugin.ws.backend.WSBackendMessageType;
import eu.domibus.plugin.ws.connector.WSPluginImpl;
import eu.domibus.plugin.ws.exception.WSPluginException;
import eu.domibus.plugin.ws.webservice.ExtendedPartInfo;
import eu.domibus.webservice.backend.generated.*;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.plugin.ws.backend.dispatch.WSPluginMessageBuilder.PAYLOAD_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class WSPluginMessageBuilderTest {

    public static final String MESSAGE_ID = "messageId";
    public static final String ORIGINAL_SENDER = "sender";
    public static final String FINAL_RECIPIENT = "recipient";
    public static final String HREF = "HREF";
    public static final String VALUE_FOUND = "VALUE_FOUND";
    public static final String MIME_TYPE = "MIME_TYPE";
    @Tested
    private WSPluginMessageBuilder wsPluginMessageBuilder;

    @Injectable
    private XMLUtilExtService xmlUtilExtService;

    @Injectable
    private JAXBContext jaxbContextWebserviceBackend;

    @Injectable
    private WSPluginImpl wsPlugin;

    @Test
    public void getJaxbElement_MessageStatusChange(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.MESSAGE_STATUS_CHANGE;

            wsPluginMessageBuilder.getChangeStatus(messageLogEntity);
            result = new MessageStatusChange();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(MessageStatusChange.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_sendSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.SEND_SUCCESS;

            wsPluginMessageBuilder.getSendSuccess(messageLogEntity);
            result = new SendSuccess();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(SendSuccess.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_deleteBatch(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.DELETED_BATCH;

            wsPluginMessageBuilder.getDeleteBatch(messageLogEntity);
            result = new DeleteBatch();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(DeleteBatch.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    public void getJaxbElement_delete(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.DELETED;

            wsPluginMessageBuilder.getDelete(messageLogEntity);
            result = new Delete();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(Delete.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_receiveSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.RECEIVE_SUCCESS;

            wsPluginMessageBuilder.getReceiveSuccess(messageLogEntity);
            result = new ReceiveSuccess();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(ReceiveSuccess.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_receiveFail(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.RECEIVE_FAIL;

            wsPluginMessageBuilder.getReceiveFailure(messageLogEntity);
            result = new ReceiveFailure();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(ReceiveFailure.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_submitMessage(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.SUBMIT_MESSAGE;

            wsPluginMessageBuilder.getSubmitMessage(messageLogEntity);
            result = new SubmitMessage();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(SubmitMessage.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getJaxbElement_sendFailure(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getType();
            result = WSBackendMessageType.SEND_FAILURE;

            wsPluginMessageBuilder.getSendFailure(messageLogEntity);
            result = new SendFailure();
        }};

        Object jaxbElement = wsPluginMessageBuilder.getJaxbElement(messageLogEntity);

        assertEquals(SendFailure.class, jaxbElement.getClass());

        new FullVerifications() {
        };
    }

    @Test
    public void getSendSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        SendSuccess sendSuccess = wsPluginMessageBuilder.getSendSuccess(messageLogEntity);
        assertEquals(MESSAGE_ID, sendSuccess.getMessageID());
        new FullVerifications() {
        };
    }

    @Test
    public void getSDelete(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        Delete delete = wsPluginMessageBuilder.getDelete(messageLogEntity);
        assertEquals(MESSAGE_ID, delete.getMessageID());
        new FullVerifications() {
        };
    }

    @Test
    public void getDeleteBatch(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        DeleteBatch sendSuccess = wsPluginMessageBuilder.getDeleteBatch(messageLogEntity);
        assertEquals(MESSAGE_ID, sendSuccess.getMessageIds().get(0));
        new FullVerifications() {
        };
    }
    @Test(expected = WSPluginException.class)
    public void getDeleteBatch_empty(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = "";
        }};
        wsPluginMessageBuilder.getDeleteBatch(messageLogEntity);
    }

    @Test
    public void getSendFailure(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        SendFailure sendFailure = wsPluginMessageBuilder.getSendFailure(messageLogEntity);
        assertEquals(MESSAGE_ID, sendFailure.getMessageID());
        new FullVerifications() {
        };
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getSubmitMessage(@Mocked WSBackendMessageLogEntity messageLogEntity) throws MessageNotFoundException {

        List<SubmitMessage> capturedSubmitMessages = new ArrayList<>();
        List<UserMessage> capturedUserMessages = new ArrayList<>();

        new Expectations(wsPluginMessageBuilder) {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
            messageLogEntity.getFinalRecipient();
            result = FINAL_RECIPIENT;
            messageLogEntity.getOriginalSender();
            result = ORIGINAL_SENDER;

            wsPlugin.browseMessage(MESSAGE_ID, (UserMessage) any);
            result = new UserMessage();

            wsPluginMessageBuilder.fillInfoPartsForLargeFiles(
                    withCapture(capturedSubmitMessages),
                    withCapture(capturedUserMessages));
        }};

        SubmitMessage sendFailure = wsPluginMessageBuilder.getSubmitMessage(messageLogEntity);
        assertEquals(MESSAGE_ID, sendFailure.getMessageID());
        assertEquals(1, capturedSubmitMessages.size());
        assertEquals(MESSAGE_ID, capturedSubmitMessages.get(0).getMessageID());
        assertEquals(FINAL_RECIPIENT, capturedSubmitMessages.get(0).getFinalRecipient());
        assertEquals(ORIGINAL_SENDER, capturedSubmitMessages.get(0).getOriginalSender());

        assertEquals(1, capturedUserMessages.size());
        Assert.assertNotNull(capturedUserMessages.get(0));

        new FullVerifications() {
        };
    }

    @Test(expected = WSPluginException.class)
    public void getSubmitMessage_MessageNotFoundException(@Mocked WSBackendMessageLogEntity messageLogEntity) throws MessageNotFoundException {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;

            wsPlugin.browseMessage(MESSAGE_ID, (UserMessage) any);
            result = new MessageNotFoundException();
        }};

        wsPluginMessageBuilder.getSubmitMessage(messageLogEntity);

        new FullVerifications() {
        };
    }

    @Test
    public void getReceiveFailure(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        ReceiveFailure sendFailure = wsPluginMessageBuilder.getReceiveFailure(messageLogEntity);
        assertEquals(MESSAGE_ID, sendFailure.getMessageID());
        new FullVerifications() {
        };
    }

    @Test
    public void getReceiveSuccess(@Mocked WSBackendMessageLogEntity messageLogEntity) {
        new Expectations() {{
            messageLogEntity.getMessageId();
            result = MESSAGE_ID;
        }};
        ReceiveSuccess sendFailure = wsPluginMessageBuilder.getReceiveSuccess(messageLogEntity);
        assertEquals(MESSAGE_ID, sendFailure.getMessageID());
        new FullVerifications() {
        };
    }

    @Test
    public void fillInfoPartsForLargeFiles_noPayloadInfo(@Mocked SubmitMessage submitMessage, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.getPayloadInfo();
            result = null;

            userMessage.getMessageInfo();
            result = null;
        }};
        wsPluginMessageBuilder.fillInfoPartsForLargeFiles(submitMessage, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void fillInfoPartsForLargeFiles_noPartInfo_empty(@Mocked SubmitMessage submitMessage, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = new ArrayList<>();

            userMessage.getMessageInfo();
            result = null;
        }};
        wsPluginMessageBuilder.fillInfoPartsForLargeFiles(submitMessage, userMessage);

        new FullVerifications() {};
    }

    @Test
    public void fillInfoPartsForLargeFiles_noPartInfo_empty2(@Mocked SubmitMessage submitMessage, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = new ArrayList<>();

            userMessage.getMessageInfo();
            result = new MessageInfo();
        }};
        wsPluginMessageBuilder.fillInfoPartsForLargeFiles(submitMessage, userMessage);

        new FullVerifications(wsPluginMessageBuilder) {};
    }

    @Test
    public void fillInfoPartsForLargeFiles_noPartInfo_null(@Mocked SubmitMessage submitMessage, @Mocked UserMessage userMessage) {
        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = null;

            userMessage.getMessageInfo();
            result = null;
        }};
        wsPluginMessageBuilder.fillInfoPartsForLargeFiles(submitMessage, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void fillInfoPartsForLargeFiles(@Mocked SubmitMessage submitMessage,
                                           @Mocked UserMessage userMessage,
                                           @Mocked ExtendedPartInfo partInfo1,
                                           @Mocked ExtendedPartInfo partInfo2) {
        new Expectations(wsPluginMessageBuilder) {{
            userMessage.getPayloadInfo().getPartInfo();
            result = Arrays.asList(partInfo1, partInfo2);

            wsPluginMessageBuilder.fillInfoPart(submitMessage, partInfo1);
            times = 1;

            wsPluginMessageBuilder.fillInfoPart(submitMessage, partInfo2);
            times = 1;
        }};

        wsPluginMessageBuilder.fillInfoPartsForLargeFiles(submitMessage, userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void fillInPart_inBody(@Mocked SubmitMessage submitMessage,
                                  @Mocked ExtendedPartInfo partInfo) {

        new Expectations(wsPluginMessageBuilder) {{
            partInfo.getPayloadDatahandler();
            result = null;

            wsPluginMessageBuilder.getAnyPropertyValue(partInfo, WSPluginMessageBuilder.MIME_TYPE);
            result = WSPluginMessageBuilder.MIME_TYPE;
            wsPluginMessageBuilder.getAnyPropertyValue(partInfo, PAYLOAD_NAME);
            result = PAYLOAD_NAME;

            partInfo.isInBody();
            result = true;

        }};

        wsPluginMessageBuilder.fillInfoPart(submitMessage, partInfo);

        new FullVerifications() {{
            LargePayloadType largePayloadType;
            submitMessage.setBodyload(largePayloadType = withCapture());

            assertEquals(WSPluginMessageBuilder.MIME_TYPE, largePayloadType.getMimeType());
            assertEquals(PAYLOAD_NAME, largePayloadType.getPayloadName());
            assertNull(largePayloadType.getPayloadId());

        }};
    }

    @Test
    public void fillInPart_notInBody(@Mocked SubmitMessage submitMessage,
                                     @Mocked ExtendedPartInfo partInfo,
                                     @Mocked DataHandler dataHandler) {

        List<LargePayloadType> largePayloadTypes = new ArrayList<>();

        new Expectations(wsPluginMessageBuilder) {{
            partInfo.getPayloadDatahandler();
            result = dataHandler;

            dataHandler.getContentType();
            result = "contentType";

            wsPluginMessageBuilder.getAnyPropertyValue(partInfo, WSPluginMessageBuilder.MIME_TYPE);
            result = WSPluginMessageBuilder.MIME_TYPE;

            wsPluginMessageBuilder.getAnyPropertyValue(partInfo, PAYLOAD_NAME);
            result = PAYLOAD_NAME;

            partInfo.isInBody();
            result = false;

            submitMessage.getPayload();
            result = largePayloadTypes;

            partInfo.getHref();
            result = HREF;

        }};
        wsPluginMessageBuilder.fillInfoPart(submitMessage, partInfo);

        new FullVerifications() {
        };

        assertEquals(1, largePayloadTypes.size());
        assertEquals(dataHandler, largePayloadTypes.get(0).getValue());
        assertEquals(HREF, largePayloadTypes.get(0).getPayloadId());
        assertEquals(PAYLOAD_NAME, largePayloadTypes.get(0).getPayloadName());
    }

    @Test
    public void getAnyPropertyValue_empty(@Mocked ExtendedPartInfo extPartInfo, @Mocked String mimeType) {

        new Expectations() {{
            extPartInfo
                    .getPartProperties()
                    .getProperty();
            result = new ArrayList<>();
        }};
        String anyPropertyValue = wsPluginMessageBuilder.getAnyPropertyValue(extPartInfo, mimeType);
        assertNull(anyPropertyValue);
        new FullVerifications() {
        };
    }

    @Test
    public void getAnyPropertyValue_ok(@Mocked ExtendedPartInfo extPartInfo,
                                       @Mocked Property property1,
                                       @Mocked Property property2) {

        new Expectations() {{
            extPartInfo
                    .getPartProperties()
                    .getProperty();
            result = Arrays.asList(property1, property2);

            property1.getName();
            result = "NOPE";

            property2.getName();
            result = MIME_TYPE;

            property2.getValue();
            result = VALUE_FOUND;
        }};
        String anyPropertyValue = wsPluginMessageBuilder.getAnyPropertyValue(extPartInfo, MIME_TYPE);
        assertEquals(VALUE_FOUND, anyPropertyValue);
        new FullVerifications() {
        };
    }

    @Test
    public void buildSOAPMessage(@Mocked WSBackendMessageLogEntity messageLogEntity,
                                 @Mocked SendSuccess jaxbElement,
                                 @Mocked SOAPMessage soapMessage) {
        new Expectations(wsPluginMessageBuilder) {{
            wsPluginMessageBuilder.getJaxbElement(messageLogEntity);
            result = jaxbElement;
            wsPluginMessageBuilder.createSOAPMessage(jaxbElement);
            result = soapMessage;
        }};
        SOAPMessage result = wsPluginMessageBuilder.buildSOAPMessage(messageLogEntity);
        assertEquals(soapMessage, result);
    }

    @Test
    public void createSOAPMessage(@Mocked SendSuccess sendSuccess,
                                  @Mocked SOAPMessage soapMessage,
                                  @Mocked SOAPBody soapBody) throws SOAPException, JAXBException {
        new Expectations() {{
            xmlUtilExtService.getMessageFactorySoap12().createMessage();
            result = soapMessage;

            soapMessage.getSOAPBody();
            result = soapBody;
        }};

        wsPluginMessageBuilder.createSOAPMessage(sendSuccess);

        new FullVerifications() {{
            jaxbContextWebserviceBackend.createMarshaller().marshal(sendSuccess, soapBody);
            times = 1;

            soapMessage.saveChanges();
            times = 1;
        }};

    }

    @Test(expected = WSPluginException.class)
    public void createSOAPMessage_exception(@Mocked SendSuccess sendSuccess,
                                            @Mocked SOAPMessage soapMessage,
                                            @Mocked SOAPBody soapBody) throws SOAPException {
        new Expectations() {{
            xmlUtilExtService.getMessageFactorySoap12().createMessage();
            result = soapMessage;

            soapMessage.getSOAPBody();
            result = new SOAPException();
        }};

        wsPluginMessageBuilder.createSOAPMessage(sendSuccess);

        new FullVerifications() {
        };

    }
}