package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.*;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.core.message.pull.PullRequestHandler;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.security.AuthorizationService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;
import javax.xml.ws.WebServiceException;

import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class IncomingUserMessageHandlerTest {

    @Tested
    IncomingUserMessageHandler incomingUserMessageHandler;

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
    AttachmentCleanupService attachmentCleanupService;

    @Injectable
    AuthorizationService authorizationService;


    /**
     * Happy flow unit testing with actual data
     *
     */
    @Test
    public void testInvoke_tc1Process_HappyFlow(@Injectable Messaging messaging,
                                                @Injectable LegConfiguration legConfiguration) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(incomingUserMessageHandler) {{
            soapRequestMessage.getProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            userMessageHandlerService.handleNewUserMessage(legConfiguration, withEqual(pmodeKey), withEqual(soapRequestMessage), withEqual(messaging), false);
            result = soapResponseMessage;
        }};

        incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
            times = 0;
        }};
    }


    /**
     * Unit testing with actual data.
     *
     */
    @Test
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final LegConfiguration legConfiguration,
                                                           @Injectable final Messaging messaging,
                                                           @Injectable final UserMessage userMessage) throws Exception {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(incomingUserMessageHandler) {{
            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;

            userMessageHandlerService.handleNewUserMessage(legConfiguration, withAny(pmodeKey), withAny(soapRequestMessage), withAny(messaging), false);
            result = new EbMS3Exception(null, null, null, null);

        }};

        try {
            incomingUserMessageHandler.processMessage(soapRequestMessage, messaging);
        } catch (Exception e) {
            assertTrue("Expecting Webservice exception!", e instanceof WebServiceException);
        }

        new Verifications() {{
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
        }};
    }
}
