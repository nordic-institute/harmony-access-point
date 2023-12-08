package eu.domibus.core.message.receive.handler;

import eu.domibus.AbstractIT;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.core.ebms3.receiver.handler.*;
import eu.domibus.core.message.pull.IncomingPullReceiptHandler;
import eu.domibus.core.message.pull.IncomingPullRequestHandler;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.test.common.SoapSampleUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public class IncomingMessageHandlerFactoryTestIT extends AbstractIT {

    @Autowired
    SoapSampleUtil soapSampleUtil;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    protected IncomingPullRequestHandler incomingMessagePullRequestHandler;

    @Autowired
    protected IncomingSignalErrorHandler incomingSignalErrorHandler;

    @Autowired
    protected IncomingUserMessageHandler incomingUserMessageHandler;

    @Autowired
    IncomingMessageHandlerDefaultFactory incomingMessageHandlerDefaultFactory;


    @Autowired
    protected IncomingUserMessageReceiptHandler incomingUserMessageReceiptHandler;

    @Autowired
    protected IncomingPullReceiptHandler incomingMessagePullReceiptHandler;

    @Test
    public void testIncomingSplitAndJoinSourceMessageReceiptMessageHandler() throws Exception {
        String filename = "soapEnvelope-UserMessage-receipt-split-and-join.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        final SOAPMessage soapMessage = (SOAPMessage) soapSampleUtil.createSOAPMessage(filename, messageId);

        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapMessage);
        final IncomingMessageHandler messageHandler = incomingMessageHandlerDefaultFactory.getMessageHandler(soapMessage, secondEbms3Messaging);
        Assert.assertEquals(incomingUserMessageReceiptHandler, messageHandler);
    }

    @Test
    public void testIncomingPullReceiptMessageHandler() throws Exception {
        String filename = "soapEnvelope-SignalMessage-receipt-pull.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        final SOAPMessage soapMessage = (SOAPMessage) soapSampleUtil.createSOAPMessage(filename, messageId);

        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapMessage);
        final IncomingMessageHandler messageHandler = incomingMessageHandlerDefaultFactory.getMessageHandler(soapMessage, secondEbms3Messaging);
        Assert.assertEquals(incomingMessagePullReceiptHandler, messageHandler);
    }

    @Test
    public void testIncomingUserMessageHandler() throws Exception {
        String filename = "mshwebserviceit-soapenvelope.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        final SOAPMessage soapMessage = (SOAPMessage) soapSampleUtil.createSOAPMessage(filename, messageId);

        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapMessage);
        final IncomingMessageHandler messageHandler = incomingMessageHandlerDefaultFactory.getMessageHandler(soapMessage, secondEbms3Messaging);
        Assert.assertEquals(incomingUserMessageHandler, messageHandler);
    }

    @Test
    public void testIncomingSignalWithErrorMessageHandler() throws Exception {
        String filename = "soapEnvelope-SignalMessage-error.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        final SOAPMessage soapMessage = (SOAPMessage) soapSampleUtil.createSOAPMessage(filename, messageId);

        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapMessage);
        final IncomingMessageHandler messageHandler = incomingMessageHandlerDefaultFactory.getMessageHandler(soapMessage, secondEbms3Messaging);
        Assert.assertEquals(incomingSignalErrorHandler, messageHandler);
    }

    @Test
    public void testIncomingPullRequestMessageHandler() throws Exception {
        String filename = "soapEnvelope-PullRequest.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        final SOAPMessage soapMessage = (SOAPMessage) soapSampleUtil.createSOAPMessage(filename, messageId);

        final Ebms3Messaging secondEbms3Messaging = messageUtil.getMessagingWithDom(soapMessage);
        final IncomingMessageHandler messageHandler = incomingMessageHandlerDefaultFactory.getMessageHandler(soapMessage, secondEbms3Messaging);
        Assert.assertEquals(incomingMessagePullRequestHandler, messageHandler);
    }
}
