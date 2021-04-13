package eu.domibus.core.message.pull;

import mockit.integration.junit4.JMockit;
import org.junit.runner.RunWith;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */

@RunWith(JMockit.class)
public class IncomingEbms3PullRequestHandlerTest {

    /*private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingEbms3PullRequestHandlerTest.class);
    private static final String VALID_PMODE_CONFIG_URI = "samplePModes/domibus-configuration-valid.xml";

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    IncomingMessageHandlerFactory incomingMessageHandlerFactory;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    UserMessageRawEnvelopeDao rawEnvelopeLogDao;

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
    }*/


}
