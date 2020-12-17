package eu.domibus.core.message.pull;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.message.compression.CompressionService;
import eu.domibus.core.generator.id.MessageIdGenerator;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.payload.PayloadProfileValidator;
import eu.domibus.core.pmode.validation.validators.PropertyProfileValidator;
import eu.domibus.core.message.signal.SignalMessageDao;
import eu.domibus.core.message.signal.SignalMessageLogDao;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.security.AuthorizationService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.TimestampDateFormatter;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.ebms3.receiver.MessageTestUtility;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandlerFactory;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.ebms3.sender.ResponseHandler;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@RunWith(JMockit.class)
public class IncomingPullRequestHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullRequestHandlerTest.class);
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";

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
    IncomingPullRequestHandler incomingPullRequestHandler;

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
    AuthorizationService authorizationService;

    @Test
    public void testHandlePullRequest(
            @Mocked final PhaseInterceptorChain pi,
            @Mocked final Process process,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {
        final String mpcQualifiedName = "defaultMPC";

        Messaging messaging = new Messaging();
        SignalMessage signalMessage = new SignalMessage();
        final PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc(mpcQualifiedName);
        signalMessage.setPullRequest(pullRequest);
        messaging.setSignalMessage(signalMessage);

        final UserMessage userMessage = new MessageTestUtility().createSampleUserMessage();
        final String messageId = userMessage.getMessageInfo().getMessageId();


        new Expectations() {{
            messageExchangeService.extractProcessOnMpc(pullRequest.getMpc());
            result = pullContext;


            messageExchangeService.retrieveReadyToPullUserMessageId(pullRequest.getMpc(), pullContext.getInitiator());
            result = messageId;

        }};
        SOAPMessage soapMessage = incomingPullRequestHandler.handlePullRequest(messaging);
        new Verifications() {{
            messageExchangeService.extractProcessOnMpc(mpcQualifiedName);
            times = 1;

            messageExchangeService.retrieveReadyToPullUserMessageId(pullRequest.getMpc(), pullContext.getInitiator());
            times = 1;

            pullRequestHandler.handlePullRequest(messageId, pullContext, null);
            times = 1;
        }};
    }


}
