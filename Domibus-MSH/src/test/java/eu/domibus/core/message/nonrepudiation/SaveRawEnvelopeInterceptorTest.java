package eu.domibus.core.message.nonrepudiation;

import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.ebms3.common.model.MessageType;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@RunWith(JMockit.class)
public class SaveRawEnvelopeInterceptorTest {

    @Tested
    SaveRawEnvelopeInterceptor saveRawEnvelopeInterceptor;

    @Injectable
    NonRepudiationService nonRepudiationService;

    @Test
    public void testHandleMessage(@Mocked SoapMessage message, @Mocked SOAPMessage jaxwsMessage) {
        String userMessageId = "mess123";

        new Expectations() {{
            message.getContent(SOAPMessage.class);
            result = jaxwsMessage;

            message.getExchange().get(DispatchClientDefaultProvider.EBMS_MESSAGE_ID);
            result = userMessageId;
        }};

        saveRawEnvelopeInterceptor.handleMessage(message);

        new Verifications() {{
            nonRepudiationService.saveResponse(jaxwsMessage, userMessageId);
        }};
    }
}