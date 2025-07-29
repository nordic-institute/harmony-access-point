package eu.domibus.core.ebms3.sender;

import eu.domibus.api.model.MessageType;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.nonrepudiation.SaveRawEnvelopeInterceptor;
import eu.domibus.core.message.nonrepudiation.SignalMessageRawEnvelopeDao;
import eu.domibus.test.AbstractIT;
import eu.domibus.test.common.SoapSampleUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class SaveRawEnvelopeInterceptorTestIT extends AbstractIT {

    @Autowired
    SaveRawEnvelopeInterceptor saveRawEnvelopeInterceptor;

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    MessageDaoTestUtil messageDaoTestUtil;

    @Autowired
    SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;

    //an exception is thrown and caught but the UserMessage exchange is marked as successfully
    @Test
    public void handleMessageWhenTheSignalMessageIsNotSavedInTheDatabase() throws IOException, SOAPException, ParserConfigurationException, SAXException {
        String filename = "MSHAS4Response.xml";
        final String messageId = UUID.randomUUID() + "@domibus.eu";
        Long userMessageEntityId = 123L;

        SoapMessage cxfSoapMessage = soapSampleUtil.createSoapMessage(filename, messageId);
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        cxfSoapMessage.setContent(SOAPMessage.class, soapMessage);

        cxfSoapMessage.getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.SIGNAL_MESSAGE);
        cxfSoapMessage.getExchange().put(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY, messageId);
        cxfSoapMessage.getExchange().put(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY, userMessageEntityId + "");

        saveRawEnvelopeInterceptor.handleMessage(cxfSoapMessage);

        final RawEnvelopeDto rawEnvelopeDto = signalMessageRawEnvelopeDao.findBySignalMessageEntityId(userMessageEntityId);
        Assert.assertNull(rawEnvelopeDto);
    }

    @Test
    public void handleMessageWithExistingSignalMessage() throws IOException, SOAPException, ParserConfigurationException, SAXException {
        String filename = "MSHAS4Response.xml";
        final String messageId = UUID.randomUUID() + "@domibus.eu";

        final SignalMessage signalMessage = messageDaoTestUtil.createSignalMessageLog(messageId, new Date());
        Long signalMessageEntityId = signalMessage.getEntityId();

        SoapMessage cxfSoapMessage = soapSampleUtil.createSoapMessage(filename, messageId);
        SOAPMessage soapMessage = soapSampleUtil.createSOAPMessage(filename, messageId);
        cxfSoapMessage.setContent(SOAPMessage.class, soapMessage);

        cxfSoapMessage.getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.SIGNAL_MESSAGE);
        cxfSoapMessage.getExchange().put(UserMessage.MESSAGE_ID_CONTEXT_PROPERTY, messageId);
        cxfSoapMessage.getExchange().put(UserMessage.USER_MESSAGE_ID_KEY_CONTEXT_PROPERTY, signalMessageEntityId + "");

        saveRawEnvelopeInterceptor.handleMessage(cxfSoapMessage);

        final RawEnvelopeDto rawEnvelopeDto = signalMessageRawEnvelopeDao.findBySignalMessageEntityId(signalMessageEntityId);
        Assert.assertNotNull(rawEnvelopeDto);
    }
}
