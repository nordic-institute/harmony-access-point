package eu.domibus.core.message.pull;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptBuilder;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.MessagingDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityMatcher;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.sql.Timestamp;

import static eu.domibus.core.message.reliability.ReliabilityChecker.CheckResult.ABORT;
import static eu.domibus.core.message.reliability.ReliabilityChecker.CheckResult.WAITING_FOR_CALLBACK;

/**
 * @author Thomas Dussart
 * @since 3.3
 */


@Component
public class PullRequestHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullRequestHandler.class);

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ReliabilityMatcher pullRequestMatcher;

    @Autowired
    private MessageAttemptService messageAttemptService;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private PullMessageService pullMessageService;

    public SOAPMessage handlePullRequest(String messageId, PullContext pullContext, String refToMessageId) {
        if (messageId != null) {
            LOG.info("Message id [{}], refToMessageId [{}]", messageId, refToMessageId);
            return handleRequest(messageId, pullContext);
        } else {
            return notifyNoMessage(pullContext, refToMessageId);
        }
    }

    SOAPMessage notifyNoMessage(PullContext pullContext, String refToMessageId) {
        LOG.trace("No message for received pull request with mpc " + pullContext.getMpcQualifiedName());
        EbMS3Exception ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0006, "There is no message available for\n" +
                "pulling from this MPC at this moment.", refToMessageId, null);
        return messageBuilder.getSoapMessage(ebMS3Exception);
    }

    public SOAPMessage handleRequest(String messageId, PullContext pullContext) {
        LegConfiguration leg = null;
        ReliabilityChecker.CheckResult checkResult = ReliabilityChecker.CheckResult.PULL_FAILED;
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;
        final Timestamp startDate = new Timestamp(System.currentTimeMillis());
        SOAPMessage soapMessage = null;
        UserMessage userMessage = null;
        try {
            userMessage = messagingDao.findUserMessageByMessageId(messageId);
            leg = pullContext.filterLegOnMpc();
            try {
                String initiatorPartyName = null;
                final String mpc = userMessage.getMpc();
                if (pullContext.getInitiator() != null) {
                    LOG.debug("Get initiator from pull context");
                    initiatorPartyName = pullContext.getInitiator().getName();
                } else if (initiatorPartyName == null && messageExchangeService.forcePullOnMpc(mpc)) {
                    LOG.debug("Extract initiator from mpc");
                    initiatorPartyName = messageExchangeService.extractInitiator(mpc);
                }
                LOG.info("Initiator is [{}]", initiatorPartyName);

                messageExchangeService.verifyReceiverCertificate(leg, initiatorPartyName);
                messageExchangeService.verifySenderCertificate(leg, pullContext.getResponder().getName());
                leg = pullContext.filterLegOnMpc();
                soapMessage = messageBuilder.buildSOAPMessage(userMessage, leg);
                PhaseInterceptorChain.getCurrentMessage().getExchange().put(MSHDispatcher.MESSAGE_TYPE_OUT, MessageType.USER_MESSAGE);
                if (pullRequestMatcher.matchReliableCallBack(leg.getReliability()) &&
                        leg.getReliability().isNonRepudiation()) {
                    PhaseInterceptorChain.getCurrentMessage().getExchange().put(DispatchClientDefaultProvider.MESSAGE_ID, messageId);
                }
                checkResult = WAITING_FOR_CALLBACK;
                LOG.info("Sending message");
                return soapMessage;
            } catch (DomibusCertificateException dcEx) {
                LOG.error(dcEx.getMessage(), dcEx);
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, dcEx.getMessage(), messageId, dcEx);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            } catch (ConfigurationException e) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", messageId, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

        } catch (ChainCertificateInvalidException e) {
            checkResult = ABORT;
            LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
            LOG.error("Cannot handle pullrequest for message:[{}], Receivever:[{}] certificate is not valid or it has been revoked ", messageId, pullContext.getInitiator().getName(), e);
        } catch (EbMS3Exception e) {
            LOG.error("EbMS3 exception occurred when handling pull request for message with ID [{}]", messageId, e);
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            reliabilityChecker.handleEbms3Exception(e, messageId);
            try {
                soapMessage = messageBuilder.buildSOAPFaultMessage(e.getFaultInfoError());
            } catch (EbMS3Exception e1) {
                throw new WebServiceException(e1);
            }
        } catch (Throwable e) { // NOSONAR: This was done on purpose.
            LOG.error("Error occurred when handling pull request for message with ID [{}]", messageId, e);
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            LOG.debug("Before updatePullMessageAfterRequest message id[{}] checkResult[{}]", messageId, checkResult);
            pullMessageService.updatePullMessageAfterRequest(userMessage, messageId, leg, checkResult);
            if (checkResult != ABORT) {
                try {
                    final MessageAttempt attempt = MessageAttemptBuilder.create()
                            .setMessageId(messageId)
                            .setAttemptStatus(attemptStatus)
                            .setAttemptError(attemptError)
                            .setStartDate(startDate).build();
                    messageAttemptService.create(attempt);
                } catch (Exception e) {
                    LOG.error("Could not create the message attempt", e);
                }
            }
        }
        return soapMessage;
    }

}
