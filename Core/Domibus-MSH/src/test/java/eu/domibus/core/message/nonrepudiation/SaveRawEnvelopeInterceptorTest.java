package eu.domibus.core.message.nonrepudiation;

import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.spring.SpringContextProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.sender.interceptor.SoapInterceptorTest;
import eu.domibus.core.message.UserMessageContextKeyProvider;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Assert;
import org.junit.Test;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class SaveRawEnvelopeInterceptorTest extends SoapInterceptorTest {

    @Tested
    SaveRawEnvelopeInterceptor saveRawEnvelopeInterceptor;

    @Injectable
    NonRepudiationService nonRepudiationService;

    @Injectable
    protected UserMessageContextKeyProvider userMessageContextKeyProvider;

    @Test
    public void testHandleUserMessage(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        soapMessage.getExchange().put(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY, "messageId");
        soapMessage.getExchange().put(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY, "123");
        Assert.assertEquals(soapMessage.get(MSHDispatcher.MESSAGE_TYPE_OUT), MessageType.USER_MESSAGE);

        saveRawEnvelopeInterceptor.handleMessage(soapMessage);

        new Verifications() {{
            nonRepudiationService.saveUserMessageRawEnvelope(anyString, anyLong);
        }};
    }

    @Test
    public void testHandleSignalMessage() throws Exception {
        Long userMessageEntityId = 123L;
        String userMessageId = "456";

        SoapMessage message = new SoapSampleUtil().createSoapMessage("SOAPMessage2.xml", "id");

        message.getExchange().put(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY, userMessageId);
        message.getExchange().put(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY, userMessageEntityId+"");
        message.getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.SIGNAL_MESSAGE);

        new Expectations() {{
            userMessageContextKeyProvider.getKeyFromTheCurrentMessage(UserMessage.USER_MESSAGE_DUPLICATE_KEY);
            result = "false";
        }};

        saveRawEnvelopeInterceptor.handleMessage(message);

        new Verifications() {{
            nonRepudiationService.saveSignalMessageRawEnvelope(anyString, userMessageEntityId);
        }};
    }
}
