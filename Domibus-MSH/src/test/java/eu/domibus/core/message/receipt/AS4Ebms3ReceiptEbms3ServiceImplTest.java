package eu.domibus.core.message.receipt;

import eu.domibus.api.ebms3.model.*;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.api.model.SignalMessageLog;
import eu.domibus.api.model.SignalMessageResult;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.usermessage.domain.MessageInfo;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.MessageStatusDao;
import eu.domibus.core.message.ReceiptDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.splitandjoin.MessageGroupDao;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "rawtypes"})
@RunWith(JMockit.class)
public class AS4Ebms3ReceiptEbms3ServiceImplTest {

    public static final String MESSAGE_ID = "messageId";
    @Tested
    AS4ReceiptServiceImpl as4ReceiptService;

    @Injectable
    protected  UserMessageHandlerService userMessageHandlerService;
    @Injectable
    private  TimestampDateFormatter timestampDateFormatter;
    @Injectable
    protected  UserMessageService userMessageService;
    @Injectable
    private  MessageIdGenerator messageIdGenerator;
    @Injectable
    protected  UserMessageRawEnvelopeDao rawEnvelopeLogDao;
    @Injectable
    private  SignalMessageDao signalMessageDao;
    @Injectable
    protected  MessageGroupDao messageGroupDao;
    @Injectable
    private  UserMessageDao userMessageDao;
    @Injectable
    protected  MessageUtil messageUtil;
    @Injectable
    protected  SoapUtil soapUtil;
    @Injectable
    protected XMLUtil xmlUtil;
    @Injectable
    protected Ebms3Converter ebms3Converter;
    @Injectable
    protected MshRoleDao mshRoleDao;
    @Injectable
    protected MessageStatusDao messageStatusDao;
    @Injectable
    protected ReceiptDao receiptDao;

    @Injectable
    private LegConfiguration legConfiguration;

    @Injectable
    private SignalMessageResult signalMessageResult;

    @Injectable
    private MessageInfo messageInfo;

    @Injectable
    private UserMessage userMessage;

    @Injectable
    private SignalMessageLog messageLog;

    @Injectable
    private SOAPMessage soapResponseMessage;

    @Injectable
    private SOAPMessage soapRequestMessage;

    @Test
    public void testGenerateReceipt_WithReliabilityAndResponseRequired(@Injectable final Source messageToReceiptTransform,
                                                                       @Injectable final Transformer transformer,
                                                                       @Injectable final DOMResult domResult,
                                                                       @Injectable MessageFactory messageFactory,
                                                                       @Injectable SOAPPart soapPart,
                                                                       @Injectable Templates templates) throws Exception {
        new Expectations(as4ReceiptService) {{
            messageFactory.createMessage();
            result = soapResponseMessage;

            soapRequestMessage.getSOAPPart();
            result = soapPart;

            xmlUtil.getMessageFactorySoap12();
            result = messageFactory;

            messageIdGenerator.generateMessageId();
            result = "1234";

            timestampDateFormatter.generateTimestamp();
            result = "mydate";

            soapPart.getContent();
            result = messageToReceiptTransform;

            templates.newTransformer();
            result = transformer;

            soapResponseMessage.getSOAPPart();
            result = soapPart;
        }};

        as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.RESPONSE, false, false, false);

        new FullVerifications(as4ReceiptService) {{
            transformer.setParameter(anyString, any);
            times = 3;

            transformer.transform(withAny(messageToReceiptTransform), withAny(domResult));
            times = 1;

            soapPart.setContent((Source) any);
            times = 1;

            as4ReceiptService.setMessagingId(soapResponseMessage, userMessage);
            times = 1;
        }};
    }

    @Test
    public void testGenerateReceipt_NoResponse() throws EbMS3Exception {

        as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.CALLBACK, false, false, false);

        new FullVerifications() {
        };
    }

    @Test
    public void testGenerateReceipt_TransformException(@Injectable Source messageToReceiptTransform,
                                                       @Injectable Transformer transformer,
                                                       @Injectable DOMResult domResult,
                                                       @Injectable SOAPPart soapPart,
                                                       @Injectable MessageFactory messageFactory,
                                                       @Injectable Templates templates) throws Exception {
        new Expectations(as4ReceiptService) {{
            xmlUtil.getMessageFactorySoap12();
            result = messageFactory;

            messageFactory.createMessage();
            result = soapResponseMessage;

            soapRequestMessage.getSOAPPart();
            result = soapPart;

            soapPart.getContent();
            result = messageToReceiptTransform;

            as4ReceiptService.getTemplates();
            result = templates;

            templates.newTransformer();
            result = transformer;

            messageIdGenerator.generateMessageId();
            result = "1234";

            timestampDateFormatter.generateTimestamp();
            result = "mydate";

            transformer.transform(withAny(messageToReceiptTransform), withAny(domResult));
            result = new TransformerException("TEST Transformer Exception");

            userMessage.getMessageId();
            result = MESSAGE_ID;

        }};

        try {
            as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.RESPONSE, false, false, false);
            fail("Expected Transformer exception to be raised !!!");
        } catch (EbMS3Exception e) {
            assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0201, e.getErrorCode());
        }

        new FullVerifications() {{
            transformer.setParameter(anyString, any);
            times = 3;
        }};
    }


    @Test
    public void testSaveResponse(@Injectable Ebms3Messaging ebms3Messaging, @Injectable SignalMessage signalMessage) throws SOAPException, EbMS3Exception {
        new Expectations() {{

            messageUtil.getMessagingWithDom(soapResponseMessage);
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(ebms3Messaging);
            result = signalMessageResult;

            signalMessageResult.getSignalMessage();
            result = signalMessage;
        }};

        as4ReceiptService.generateResponse(soapResponseMessage, false);

        new FullVerifications() {
        };
    }

    @Test
    public void testSaveResponse_selfSending(@Injectable Ebms3Messaging ebms3Messaging,
                                             @Injectable Ebms3SignalMessage ebms3SignalMessage,
                                             @Injectable Ebms3MessageInfo ebms3MessageInfo,
                                             @Injectable Ebms3UserMessage ebms3UserMessage,
                                             @Injectable SignalMessage signalMessage
    ) throws SOAPException, EbMS3Exception {
        new Expectations() {{
            messageUtil.getMessagingWithDom(soapResponseMessage);
            result = ebms3Messaging;

            ebms3Converter.convertFromEbms3(ebms3Messaging);
            result = signalMessageResult;

            signalMessageResult.getSignalMessage();
            result = signalMessage;

            signalMessage.getRefToMessageId() ;
            result = "refTomessageId";

            signalMessage.getSignalMessageId() ;
            result = "signalMessageId";
        }};

        as4ReceiptService.generateResponse(soapResponseMessage, true);

        new FullVerifications() {{
            signalMessage.setRefToMessageId("refTomessageId" + UserMessageHandlerService.SELF_SENDING_SUFFIX);
            signalMessage.setSignalMessageId("signalMessageId" + UserMessageHandlerService.SELF_SENDING_SUFFIX);

        }};
    }

    @Test
    public void testGenerateReceipt_NoReliability() throws EbMS3Exception {

        as4ReceiptService.generateReceipt(soapRequestMessage, userMessage, ReplyPattern.CALLBACK, false, false, false);

        new FullVerifications() {
        };
    }

    @Test
    public void testSetMessagingId(@Injectable SOAPMessage responseMessage,
                                   @Injectable Iterator childElements,
                                   @Injectable SOAPElement messagingElement) throws Exception {
        String messageId = "123";
        new Expectations() {{
            responseMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME);
            result = childElements;

            childElements.hasNext();
            result = true;

            childElements.next();
            result = messagingElement;

            userMessage.getMessageId();
            result = messageId;
        }};

        as4ReceiptService.setMessagingId(responseMessage, userMessage);

        new FullVerifications() {{
            QName idQname;
            String value;
            messagingElement.addAttribute(idQname = withCapture(), value = withCapture());
            assertNotNull(idQname);
            assertEquals("Id", idQname.getLocalPart());
            assertEquals("wsu", idQname.getPrefix());
            assertNotNull(value);
            assertEquals(value, "_1" + DigestUtils.sha256Hex(messageId));
        }};
    }

}