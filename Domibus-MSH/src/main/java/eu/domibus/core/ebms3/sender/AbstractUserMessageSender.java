package eu.domibus.core.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.UserMessageLog;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.interceptor.Fault;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;

import static eu.domibus.core.metrics.MetricNames.OUTGOING_USER_MESSAGE;

/**
 * Common logic for sending AS4 messages to C3
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class AbstractUserMessageSender implements MessageSender {

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Autowired
    protected EbMS3MessageBuilder messageBuilder;

    @Autowired
    protected ReliabilityChecker reliabilityChecker;

    @Autowired
    protected ResponseHandler responseHandler;

    @Autowired
    protected MessageExchangeService messageExchangeService;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected ReliabilityService reliabilityService;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Override
    @Timer(OUTGOING_USER_MESSAGE)
    @Counter(OUTGOING_USER_MESSAGE)
    public void sendMessage(final Messaging messaging, final UserMessageLog userMessageLog) {
        final UserMessage userMessage = messaging.getUserMessage();
        String messageId = userMessage.getMessageInfo().getMessageId();

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        attempt.setStatus(MessageAttemptStatus.SUCCESS);

        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;
        ResponseResult responseResult = null;
        SOAPMessage responseSoapMessage = null;

        LegConfiguration legConfiguration = null;
        final String pModeKey;

        try {
            try {
                validateBeforeSending(userMessage);
            } catch (DomibusCoreException e) {
                getLog().error("Validation exception: message [{}] will not be send", messageId, e);
                attempt.setError(e.getMessage());
                attempt.setStatus(MessageAttemptStatus.ABORT);
                // this flag is used in the finally clause
                reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.ABORT;
                return;
            }


            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            getLog().debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            getLog().info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            Policy policy;
            try {
                policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            } catch (final ConfigurationException e) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            Party sendingParty = pModeProvider.getSenderParty(pModeKey);
            Validate.notNull(sendingParty, "Initiator party was not found");
            Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            Validate.notNull(receiverParty, "Responder party was not found");

            try {
                messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
                messageExchangeService.verifySenderCertificate(legConfiguration, sendingParty.getName());
            } catch (ChainCertificateInvalidException cciEx) {
                getLog().securityError(DomibusMessageCode.SEC_INVALID_X509CERTIFICATE, cciEx);
                attempt.setError(cciEx.getMessage());
                attempt.setStatus(MessageAttemptStatus.ERROR);
                // this flag is used in the finally clause
                reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;
                getLog().error("Cannot handle request for message:[{}], Certificate is not valid or it has been revoked ", messageId, cciEx);
                errorLogDao.create(new ErrorLogEntry(MSHRole.SENDING, messageId,  ErrorCode.EBMS_0004, cciEx.getMessage()));
                return;
            }

            getLog().debug("PMode found : " + pModeKey);
            final SOAPMessage requestSoapMessage = createSOAPMessage(userMessage, legConfiguration);
            responseSoapMessage = mshDispatcher.dispatch(requestSoapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            responseResult = responseHandler.verifyResponse(responseSoapMessage, messageId);

            reliabilityCheckSuccessful = reliabilityChecker.check(requestSoapMessage, responseSoapMessage, responseResult, legConfiguration);
        } catch (final SOAPFaultException soapFEx) {
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                getLog().warn("Error for message with ID [{}]", messageId, soapFEx);
            }
            attempt.setError(soapFEx.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
        } catch (final EbMS3Exception e) {
            reliabilityChecker.handleEbms3Exception(e, messageId);
            attempt.setError(e.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            getLog().error("Error sending message [{}]", messageId, t);
            attempt.setError(t.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
            throw t;
        } finally {
            try {
                getLog().debug("Finally handle reliability");
                reliabilityService.handleReliability(messageId, messaging, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
            } catch (Exception ex) {
                getLog().warn("Finally exception when handlingReliability", ex);
                reliabilityService.handleReliabilityInNewTransaction(messageId, messaging, userMessageLog, reliabilityCheckSuccessful, responseSoapMessage, responseResult, legConfiguration, attempt);
            }
        }
    }

    protected void validateBeforeSending(final UserMessage userMessage) {
        //can be overridden by child implementations
    }

    protected abstract SOAPMessage createSOAPMessage(final UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception;

    protected abstract DomibusLogger getLog();
}
