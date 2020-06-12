package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Reliability;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.ResponseResult;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.pull.MessagingLock;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.pull.PullRequestHandler;
import eu.domibus.core.message.pull.PullRequestResult;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.PropertyProfileValidator;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.*;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;

/**
 * @author François Gautier
 * @since 4.2
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "TestMethodWithIncorrectSignature"})
public class IncomingUserMessageReceiptHandlerTest {

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    IncomingMessageHandlerFactory incomingMessageHandlerFactory;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    MessagingService messagingService;

    @Injectable
    SignalMessageDao signalMessageDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageFactory messageFactory;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    JAXBContext jaxbContext;

    @Injectable
    TransformerFactory transformerFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    TimestampDateFormatter timestampDateFormatter;

    @Injectable
    CompressionService compressionService;

    @Injectable
    MessageIdGenerator messageIdGenerator;

    @Injectable
    PayloadProfileValidator payloadProfileValidator;

    @Injectable
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    CertificateService certificateService;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    EbMS3MessageBuilder messageBuilder;

    @Injectable
    UserMessageHandlerService userMessageHandlerService;

    @Injectable
    ResponseHandler responseHandler;

    @Injectable
    ReliabilityChecker reliabilityChecker;

    @Tested
    IncomingUserMessageReceiptHandler incomingUserMessageReceiptHandler;

    @Injectable
    ReliabilityMatcher pullReceiptMatcher;

    @Injectable
    ReliabilityMatcher pullRequestMatcher;

    @Injectable
    PullRequestHandler pullRequestHandler;

    @Injectable
    ReliabilityService reliabilityService;

    @Injectable
    PullMessageService pullMessageService;

    @Injectable
    MessageUtil messageUtil;

    @Injectable
    SoapUtil soapUtil;

    @Test
    public void testHandleUserMessageReceipt_HappyFlow(@Mocked final SOAPMessage request,
                                                      @Mocked final Messaging messaging,
                                                      @Mocked final UserMessage userMessage,
                                                      @Mocked final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final PullRequestResult pullRequestResult,
                                                      @Injectable final MessagingLock messagingLock,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final Reliability reliability,
                                                      @Injectable final LegConfiguration legConfiguration,
                                                      @Injectable ResponseResult responseResult) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        new Expectations(incomingUserMessageReceiptHandler) {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            messagingDao.findMessageByMessageId(messageId);
            result = messaging;

            messaging.getUserMessage();
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageConfiguration;

            messageConfiguration.getPmodeKey();
            result = pModeKey;

            responseHandler.verifyResponse(request, messageId);
            result = responseResult;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = "NAME";

            incomingUserMessageReceiptHandler.getSourceMessageReliability();
            result = reliability;

            incomingUserMessageReceiptHandler.getSoapMessage(legConfiguration, userMessage);
            result = soapMessage;

            reliabilityChecker.check(soapMessage, request, responseResult, reliability);
            result = ReliabilityChecker.CheckResult.OK;

        }};

        incomingUserMessageReceiptHandler.handleUserMessageReceipt(request, messaging);

        new FullVerifications(incomingUserMessageReceiptHandler) {};

    }

    @Test
    public void testHandleUserMessageReceipt_Exception(@Mocked final SOAPMessage request,
                                                      @Mocked final Messaging messaging,
                                                      @Mocked final UserMessage userMessage,
                                                      @Mocked final MessageExchangeConfiguration messageConfiguration,
                                                      @Injectable final PullRequestResult pullRequestResult,
                                                      @Injectable final MessagingLock messagingLock,
                                                      @Injectable final SOAPMessage soapMessage,
                                                      @Injectable final Reliability reliability,
                                                      @Injectable final LegConfiguration legConfiguration,
                                                      @Injectable ResponseResult responseResult) throws EbMS3Exception {
        final String messageId = "12345";
        final String pModeKey = "pmodeKey";
        final UserMessageLog userMessageLog = new UserMessageLog();
        userMessageLog.setMessageId(messageId);
        userMessageLog.setMessageStatus(MessageStatus.WAITING_FOR_RECEIPT);
        new Expectations(incomingUserMessageReceiptHandler) {{
            messaging.getSignalMessage().getMessageInfo().getRefToMessageId();
            result = messageId;

            userMessageLogDao.findByMessageId(messageId);
            result = userMessageLog;

            messagingDao.findMessageByMessageId(messageId);
            result = messaging;

            messaging.getUserMessage();
            result = userMessage;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = messageConfiguration;

            messageConfiguration.getPmodeKey();
            result = pModeKey;

            responseHandler.verifyResponse(request, messageId);
            result = EbMS3ExceptionBuilder
                    .getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0001)
                    .refToMessageId(messageId)
                    .build();

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = "NAME";

            incomingUserMessageReceiptHandler.getSoapMessage(legConfiguration, userMessage);
            result = soapMessage;

        }};

        incomingUserMessageReceiptHandler.handleUserMessageReceipt(request, messaging);

        new FullVerifications(incomingUserMessageReceiptHandler) {{
            reliabilityChecker.handleEbms3Exception((EbMS3Exception) any, anyString);
            reliabilityService.handleReliability(messageId, messaging, userMessageLog, ReliabilityChecker.CheckResult.ABORT, request, null, legConfiguration, null);
        }};

    }
}