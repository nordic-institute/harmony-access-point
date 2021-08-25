package eu.domibus.core.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.Messaging;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageLogDao;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.soap.SOAPMessage;
import java.util.UUID;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class AbstractEbms3UserMessageSenderTest {

    @Tested
    AbstractUserMessageSender abstractUserMessageSender;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected MSHDispatcher mshDispatcher;

    @Injectable
    protected EbMS3MessageBuilder messageBuilder;

    @Injectable
    protected ReliabilityChecker reliabilityChecker;

    @Injectable
    protected ResponseHandler responseHandler;

    @Injectable
    protected MessageAttemptService messageAttemptService;

    @Injectable
    protected MessageExchangeService messageExchangeService;

    @Injectable
    protected PolicyService policyService;

    @Injectable
    protected ReliabilityService reliabilityService;

    @Injectable
    protected UserMessageLogDao userMessageLogDao;

    @Injectable
    protected ErrorLogService errorLogService;

    @Injectable
    protected MshRoleDao mshRoleDao;

    @Injectable
    protected PartInfoDao partInfoDao;

    @Injectable
    NonRepudiationService nonRepudiationService;

    private final String messageId = UUID.randomUUID().toString();

    private final String senderName = "domibus-blue";
    private final String receiverName = "domibus-red";
    private final String legConfigurationName = "pushTestcase1tc1Action";
    private final String pModeKey = "toto";
    private final String configPolicy = "tototiti";
    static final String POLICIES = "policies/";


    @Test
    public void testSendMessage(@Injectable Messaging messaging,
                                @Injectable UserMessage userMessage,
                                @Injectable UserMessageLog userMessageLog,
                                @Injectable LegConfiguration legConfiguration,
                                @Injectable Policy policy,
                                @Injectable Party senderParty,
                                @Injectable Party receiverParty,
                                @Injectable SOAPMessage soapMessage,
                                @Injectable SOAPMessage response,
                                @Injectable ResponseResult responseResult) throws Exception {

        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;
        String messageId = "123";

        new Expectations(abstractUserMessageSender) {{
            userMessage.getMessageId();
            result = messageId;

            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractEbms3UserMessageSenderTest.class);

            userMessage.getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = response;

            responseHandler.verifyResponse(response, messageId);
            result = responseResult;

            reliabilityChecker.check(soapMessage, response, responseResult, legConfiguration);
            result = reliabilityCheckSuccessful;

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage, userMessageLog);

        new FullVerifications(abstractUserMessageSender) {{
            LegConfiguration legConfigurationActual;
            String receiverPartyNameActual;
            messageExchangeService.verifyReceiverCertificate(legConfigurationActual = withCapture(), receiverPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(receiverName, receiverPartyNameActual);


            String senderPartyNameActual;
            messageExchangeService.verifySenderCertificate(legConfigurationActual = withCapture(), senderPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(senderName, senderPartyNameActual);

            ReliabilityChecker.CheckResult checkResultActual;

            reliabilityService.handleReliability(userMessage, userMessageLog, checkResultActual = withCapture(), response, responseResult, legConfiguration, null);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

        }};
    }

    @Test
    public void testSendMessage_WrongPolicyConfig_Exception(@Injectable final Messaging messaging,
                                                            @Injectable final UserMessage userMessage,
                                                            @Injectable final UserMessageLog userMessageLog,
                                                            @Injectable final LegConfiguration legConfiguration) throws EbMS3Exception {

        final ConfigurationException configurationException = new ConfigurationException("policy file not found");

        new Expectations(abstractUserMessageSender) {{

            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractEbms3UserMessageSenderTest.class);

            userMessage.getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = configurationException;

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage, userMessageLog);

        new FullVerifications(abstractUserMessageSender) {{
            EbMS3Exception ebMS3ExceptionActual;
            reliabilityChecker.handleEbms3Exception(ebMS3ExceptionActual = withCapture(), userMessage);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ebMS3ExceptionActual.getErrorCode());
            Assert.assertEquals("Policy configuration invalid", ebMS3ExceptionActual.getErrorDetail());
            Assert.assertEquals(MSHRole.SENDING, ebMS3ExceptionActual.getMshRole());
        }};
    }

    @Test
    public void testSendMessage_ChainCertificateInvalid_Exception(@Injectable final Messaging messaging,
                                                                  @Injectable final UserMessage userMessage,
                                                                  @Injectable final UserMessageLog userMessageLog,
                                                                  @Injectable final LegConfiguration legConfiguration,
                                                                  @Injectable final Party senderParty,
                                                                  @Injectable final Party receiverParty,
                                                                  @Injectable SOAPMessage response) throws Exception {
        final String chainExceptionMessage = "certificate invalid";
        final ChainCertificateInvalidException chainCertificateInvalidException = new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;


        new Expectations(abstractUserMessageSender) {{

            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractEbms3UserMessageSenderTest.class);

            userMessage.getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
            result = chainCertificateInvalidException;
        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage, userMessageLog);

        new FullVerifications(abstractUserMessageSender) {{
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(userMessage, userMessageLog, checkResultActual = withCapture(), null, null, legConfiguration, null);
            errorLogService.createErrorLogSending(messageId, ErrorCode.EBMS_0004, chainCertificateInvalidException.getMessage(), userMessage);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);

        }};
    }

    @Test
    public void testSendMessage_UnmarshallingError_Exception(@Injectable final Messaging messaging,
                                                             @Injectable final UserMessage userMessage,
                                                             @Injectable final UserMessageLog userMessageLog,
                                                             @Injectable final LegConfiguration legConfiguration,
                                                             @Injectable final Policy policy,
                                                             @Injectable final Party senderParty,
                                                             @Injectable final Party receiverParty,
                                                             @Injectable final SOAPMessage soapMessage,
                                                             @Injectable final SOAPMessage response,
                                                             @Injectable ResponseResult responseResult) throws Exception {

        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;

        new Expectations(abstractUserMessageSender) {{
            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractEbms3UserMessageSenderTest.class);

            userMessage.getMessageId();
            result = messageId;

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = response;

            nonRepudiationService.saveRequest(soapMessage, userMessage);

            responseHandler.verifyResponse(response, messageId);
            result = EbMS3ExceptionBuilder
                    .getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .message("Problem occurred during marshalling")
                    .refToMessageId(messageId)
                    .mshRole(MSHRole.SENDING)
                    .build();
        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage, userMessageLog);

        new FullVerifications() {{
            LegConfiguration legConfigurationActual;
            String receiverPartyNameActual;
            messageExchangeService.verifyReceiverCertificate(legConfigurationActual = withCapture(), receiverPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(receiverName, receiverPartyNameActual);


            String senderPartyNameActual;
            messageExchangeService.verifySenderCertificate(legConfigurationActual = withCapture(), senderPartyNameActual = withCapture());
            Assert.assertEquals(legConfiguration.getName(), legConfigurationActual.getName());
            Assert.assertEquals(senderName, senderPartyNameActual);

            nonRepudiationService.saveRequest(soapMessage, userMessage);

            EbMS3Exception ebMS3ExceptionActual;
            reliabilityChecker.handleEbms3Exception(ebMS3ExceptionActual = withCapture(), userMessage);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0004, ebMS3ExceptionActual.getErrorCode());
            Assert.assertEquals("Problem occurred during marshalling", ebMS3ExceptionActual.getErrorDetail());
            Assert.assertEquals(MSHRole.SENDING, ebMS3ExceptionActual.getMshRole());

            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(
                    userMessage,
                    userMessageLog,
                    checkResultActual = withCapture(),
                    response,
                    null,
                    legConfiguration,
                    null);

            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);
        }};
    }

    @Test
    public void testSendMessage_DispatchError_Exception(final @Injectable Messaging messaging,
                                                        @Injectable final UserMessage userMessage,
                                                        @Injectable final UserMessageLog userMessageLog,
                                                        @Injectable final LegConfiguration legConfiguration,
                                                        @Injectable final Policy policy,
                                                        @Injectable final Party senderParty,
                                                        @Injectable final Party receiverParty,
                                                        @Injectable final SOAPMessage soapMessage,
                                                        @Injectable SOAPMessage response,
                                                        @Injectable ResponseResult responseResult) throws Exception {

        final ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;

        String attemptError = "OutOfMemory occurred while dispatching messages";

        new Expectations(abstractUserMessageSender) {{

            userMessage.getMessageId();
            result = messageId;

            abstractUserMessageSender.getLog();
            result = DomibusLoggerFactory.getLogger(AbstractEbms3UserMessageSenderTest.class);

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            result = pModeKey;

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            legConfiguration.getName();
            result = legConfigurationName;

            legConfiguration.getSecurity().getPolicy();
            result = configPolicy;

            policyService.parsePolicy(POLICIES + legConfiguration.getSecurity().getPolicy());
            result = policy;

            pModeProvider.getSenderParty(pModeKey);
            result = senderParty;

            pModeProvider.getReceiverParty(pModeKey);
            result = receiverParty;

            receiverParty.getName();
            result = receiverName;

            senderParty.getName();
            result = senderName;

            abstractUserMessageSender.createSOAPMessage(userMessage, legConfiguration);
            result = soapMessage;

            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            result = new OutOfMemoryError(attemptError);

        }};

        //tested method
        abstractUserMessageSender.sendMessage(userMessage, userMessageLog);

        new Verifications() {{
            ReliabilityChecker.CheckResult checkResultActual;
            reliabilityService.handleReliability(userMessage, userMessageLog, checkResultActual = withCapture(), null, null, legConfiguration, null);
            Assert.assertEquals(reliabilityCheckSuccessful, checkResultActual);
        }};
    }
}