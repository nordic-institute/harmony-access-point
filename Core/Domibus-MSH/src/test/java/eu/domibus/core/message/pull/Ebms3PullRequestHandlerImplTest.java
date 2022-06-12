package eu.domibus.core.message.pull;

import eu.domibus.api.ebms3.model.Ebms3Error;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.model.*;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.sender.retry.RetryService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.test.common.MessageTestUtility;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.util.List;

import static org.junit.Assert.assertNull;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked", "ConstantConditions", "unused"})
@RunWith(JMockit.class)
public class Ebms3PullRequestHandlerImplTest {

    @Injectable
    MessageExchangeService messageExchangeService;

    @Injectable
    EbMS3MessageBuilder messageBuilder;

    @Injectable
    ReliabilityChecker reliabilityChecker;

    @Injectable
    ReliabilityMatcher pullRequestMatcher;

    @Injectable
    MessageAttemptService messageAttemptService;

    @Injectable
    RetryService retryService;

    @Injectable
    ReliabilityService reliabilityService;

    @Injectable
    PullMessageService pullMessageService;

    @Injectable
    UserMessageDao userMessageDao;

    @Injectable
    PartInfoDao partInfoDao;

    @Tested
    PullRequestHandler pullRequestHandler;

    @Test
    public void testHandlePullRequestMessageFoundWithError(
            @Mocked final PhaseInterceptorChain pi,
            @Injectable final LegConfiguration legConfiguration,
            @Injectable final PullContext pullContext) throws EbMS3Exception {
        final UserMessage userMessage = new MessageTestUtility().createSampleUserMessage();
        final String messageId = userMessage.getMessageId();
        final EbMS3Exception ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0001)
                .message("Payload in body must be valid XML")
                .refToMessageId(messageId)
                .build();

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = userMessage;

            pullContext.filterLegOnMpc();
            result = legConfiguration;

            messageBuilder.buildSOAPMessage(userMessage, (List<PartInfo>) any, legConfiguration);
            result = ebMS3Exception;

            messageBuilder.buildSOAPFaultMessage((Ebms3Error) any);
            result = null;
        }};

        SOAPMessage soapMessage = pullRequestHandler.handleRequest(messageId, pullContext);
        assertNull(soapMessage);
        new Verifications() {{

            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, legConfiguration, ReliabilityChecker.CheckResult.PULL_FAILED);
            times = 1;
        }};
    }

    @Test
    public void testHandlePullRequestMessageFound(
            @Mocked final PhaseInterceptorChain pi,
            @Injectable final LegConfiguration legConfiguration,
            @Injectable final UserMessage userMessage,
            @Injectable final PullContext pullContext) throws EbMS3Exception {
        final String messageId = "messageId";

        new Expectations() {{
            userMessageDao.findByMessageId(messageId);
            result = userMessage;

            legConfiguration.getReliability().isNonRepudiation();
            result = true;

            pullRequestMatcher.matchReliableCallBack(withAny(legConfiguration.getReliability()));
            result = true;

            pullContext.filterLegOnMpc();
            result = legConfiguration;

        }};

        pullRequestHandler.handleRequest(messageId, pullContext);

        new Verifications() {{

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
            times = 1;

            PhaseInterceptorChain.getCurrentMessage().getExchange().put(DispatchClientDefaultProvider.MESSAGE_ID, messageId);
            times = 1;

            messageBuilder.buildSOAPMessage(userMessage, (List<PartInfo>) any, legConfiguration);
            times = 1;

            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, legConfiguration, ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK);
            times = 1;
        }};
    }

    @Test
    public void testHandlePullRequestNoMessageFound(@Injectable ReliabilityMatcher pullReceiptMatcher,
                                                    @Injectable ReliabilityMatcher pullRequestMatcher,
                                                    @Injectable PullContext pullContext) {

        pullRequestHandler.notifyNoMessage(pullContext, null);
        new Verifications() {{

            EbMS3Exception exception;
            messageBuilder.getSoapMessage(exception = withCapture());
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0006, exception.getErrorCode());
        }};
    }

    @Test
    public void testHandlePullRequestWithInvalidSenderCertificate(
            @Injectable final UserMessage userMessage,
            @Injectable final LegConfiguration legConfiguration,
            @Injectable final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverId";

        new Expectations() {{

            messageExchangeService.verifySenderCertificate(legConfiguration, pullContext.getResponder().getName());
            result = new DomibusCertificateException("test");
        }};

        pullRequestHandler.handleRequest(messageId, pullContext);

        new Verifications() {{
            EbMS3Exception e;
            reliabilityChecker.handleEbms3Exception(e = withCapture(), userMessage);
            times = 1;
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0101, e.getErrorCode());
            Ebms3Error faultInfo;
            messageBuilder.buildSOAPFaultMessage(faultInfo = withCapture());
            times = 1;
            Assert.assertEquals("EBMS:0101", faultInfo.getErrorCode());

            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, legConfiguration, ReliabilityChecker.CheckResult.PULL_FAILED);
            times = 1;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 1;
        }};
    }

    @Test
    public void testHandlePullRequestConfigurationException(
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final UserMessage userMessage,
            @Mocked final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverId";

        new Expectations() {{

            messageExchangeService.verifySenderCertificate(legConfiguration, pullContext.getResponder().getName());
            result = new ConfigurationException();

        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            EbMS3Exception e;
            reliabilityChecker.handleEbms3Exception(e = withCapture(), userMessage);
            times = 1;
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, e.getErrorCode());
            Ebms3Error faultInfo;
            messageBuilder.buildSOAPFaultMessage(faultInfo = withCapture());
            times = 1;
            Assert.assertEquals("EBMS:0010", faultInfo.getErrorCode());

            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, legConfiguration, ReliabilityChecker.CheckResult.PULL_FAILED);
            times = 1;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 1;

        }};
    }

    @Test
    public void testHandlePullRequestWithInvalidReceiverCertificate(
            @Mocked final UserMessage userMessage,
            @Mocked final LegConfiguration legConfiguration,
            @Mocked final PullContext pullContext) throws EbMS3Exception {

        final String messageId = "whatEverID";
        new Expectations() {{

            messageExchangeService.verifyReceiverCertificate(legConfiguration, pullContext.getInitiator().getName());
            result = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "invalid certificate");


        }};
        pullRequestHandler.handleRequest(messageId, pullContext);
        new Verifications() {{
            messageBuilder.buildSOAPFaultMessage(withAny(new Ebms3Error()));
            times = 0;
            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, legConfiguration, ReliabilityChecker.CheckResult.ABORT);
            times = 1;
            MessageAttempt attempt = null;
            messageAttemptService.create(withAny(attempt));
            times = 0;
        }};
    }
}