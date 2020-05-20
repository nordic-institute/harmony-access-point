package eu.domibus.core.message;

import com.google.common.collect.Lists;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JMSMessageBuilder;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.reliability.ReliabilityException;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLogDao;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeDto;
import eu.domibus.core.message.nonrepudiation.RawEnvelopeLog;
import eu.domibus.core.message.pull.ProcessValidator;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.message.pull.MpcService;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.message.pull.PullContext;
import eu.domibus.core.message.pull.PullMessageService;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.core.message.pull.PullFrequencyHelper;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Queue;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING;
import static eu.domibus.common.MessageStatus.READY_TO_PULL;
import static eu.domibus.common.MessageStatus.SEND_ENQUEUED;
import static eu.domibus.core.message.pull.PullContext.MPC;
import static eu.domibus.core.message.pull.PullContext.PMODE_KEY;

/**
 * @author Thomas Dussart
 * @since 3.3
 * {@inheritDoc}
 */

@Service
public class MessageExchangeServiceImpl implements MessageExchangeService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageExchangeServiceImpl.class);

    private static final String PULL = "pull";

    @Autowired
    private MessagingDao messagingDao;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("pullMessageQueue")
    private Queue pullMessageQueue;

    @Autowired
    protected JMSManager jmsManager;

    @Autowired
    private RawEnvelopeLogDao rawEnvelopeLogDao;

    @Autowired
    private ProcessValidator processValidator;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyService policyService;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected CertificateService certificateService;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private PullMessageService pullMessageService;

    @Autowired
    private MpcService mpcService;

    @Autowired
    private PullFrequencyHelper pullFrequencyHelper;


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageStatus getMessageStatus(final MessageExchangeConfiguration messageExchangeConfiguration) {
        MessageStatus messageStatus = SEND_ENQUEUED;
        List<Process> processes = pModeProvider.findPullProcessesByMessageContext(messageExchangeConfiguration);
        if (!processes.isEmpty()) {
            processValidator.validatePullProcess(Lists.newArrayList(processes));
            messageStatus = READY_TO_PULL;
        } else {
            LOG.debug("No pull process found for message configuration");
        }
        return messageStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageStatus retrieveMessageRestoreStatus(final String messageId) {
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            if (forcePullOnMpc(userMessage)) {
                return READY_TO_PULL;
            }
            MessageExchangeConfiguration userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            return getMessageStatus(userMessageExchangeConfiguration);
        } catch (EbMS3Exception e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_001, "Could not get the PMode key for message [" + messageId + "]", e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void initiatePullRequest() {
        initiatePullRequest(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void initiatePullRequest(final String mpc) {
        if (!pModeProvider.isConfigurationLoaded()) {
            LOG.debug("A configuration problem occurred while initiating the pull request. Probably no configuration is loaded.");
            return;
        }
        Party initiator = pModeProvider.getGatewayParty();
        List<Process> pullProcesses = pModeProvider.findPullProcessesByInitiator(initiator);
        LOG.trace("Initiating pull requests:");
        if (pullProcesses.isEmpty()) {
            LOG.trace("No pull process configured !");
            return;
        }

        final List<Process> validPullProcesses = getValidProcesses(pullProcesses);
        final Set<String> mpcNames = validPullProcesses.stream()
                .flatMap(pullProcess -> pullProcess.getLegs().stream().map(leg -> leg.getDefaultMpc().getName()))
                .collect(Collectors.toSet());
        pullFrequencyHelper.setMpcNames(mpcNames);

        final Integer maxPullRequestNumber = pullFrequencyHelper.getTotalPullRequestNumberPerJobCycle();
        if (pause(maxPullRequestNumber)) {
            return;
        }
        validPullProcesses.forEach(pullProcess ->
                pullProcess.getLegs().stream().forEach(legConfiguration ->
                        preparePullRequestForMpc(mpc, initiator, pullProcess, legConfiguration)));
    }

    private List<Process> getValidProcesses(List<Process> pullProcesses) {
        final List<Process> validPullProcesses = new ArrayList<>();
        for (Process pullProcess : pullProcesses) {
            try {
                processValidator.validatePullProcess(Lists.newArrayList(pullProcess));
                validPullProcesses.add(pullProcess);
            } catch (PModeException e) {
                LOG.warn("Invalid pull process configuration found during pull try", e);
            }
        }
        return validPullProcesses;
    }

    private void preparePullRequestForMpc(String mpc, Party initiator, Process pullProcess, LegConfiguration legConfiguration) {
        for (Party responder : pullProcess.getResponderParties()) {
            String mpcQualifiedName = legConfiguration.getDefaultMpc().getQualifiedName();
            if (mpc != null && !mpc.equals(mpcQualifiedName)) {
                continue;
            }
            //@thom remove the pullcontext from here.
            PullContext pullContext = new PullContext(pullProcess,
                    responder,
                    mpcQualifiedName);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(pullContext.getAgreement(),
                    responder.getName(),
                    initiator.getName(),
                    legConfiguration.getService().getName(),
                    legConfiguration.getAction().getName(),
                    legConfiguration.getName());
            LOG.debug("messageExchangeConfiguration:[{}]", messageExchangeConfiguration);
            String mpcName = legConfiguration.getDefaultMpc().getName();
            Integer pullRequestNumberForResponder = pullFrequencyHelper.getPullRequestNumberForMpc(mpcName);
            LOG.debug("Sending:[{}] pull request for mpcFQN:[{}] to mpc:[{}]", pullRequestNumberForResponder, mpcQualifiedName, mpcName);
            for (int i = 0; i < pullRequestNumberForResponder; i++) {
                jmsManager.sendMapMessageToQueue(JMSMessageBuilder.create()
                        .property(MPC, mpcQualifiedName)
                        .property(PMODE_KEY, messageExchangeConfiguration.getReversePmodeKey())
                        .property(PullContext.NOTIFY_BUSINNES_ON_ERROR, String.valueOf(legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()))
                        .build(), pullMessageQueue);
            }

        }
    }

    private boolean pause(Integer maxPullRequestNumber) {
        LOG.trace("Checking if the system should pause the pulling mechanism.");
        final long queueMessageNumber = jmsManager.getDestinationSize(PULL);

        final boolean shouldPause = queueMessageNumber > maxPullRequestNumber;

        if (shouldPause) {
            LOG.debug("[PULL]:Size of the pulling queue:[{}] is higher then the number of pull requests to send:[{}]. Pause adding to the queue so the system can consume the requests.", queueMessageNumber, maxPullRequestNumber);
        } else {
            LOG.trace("[PULL]:Size of the pulling queue:[{}], the number of pull requests to send:[{}].", queueMessageNumber, maxPullRequestNumber);
        }
        return shouldPause;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String retrieveReadyToPullUserMessageId(final String mpc, final Party initiator) {
        String partyId = getPartyId(mpc, initiator);

        if (partyId == null) {
            LOG.warn("No identifier found for party:[{}]", initiator.getName());
            return null;
        }
        return pullMessageService.getPullMessageId(partyId, mpc);
    }

    protected String getPartyId(String mpc, Party initiator) {
        String partyId = null;
        if (initiator != null && initiator.getIdentifiers() != null) {
            Optional<Identifier> optionalParty = initiator.getIdentifiers().stream().findFirst();
            partyId = optionalParty.isPresent() ? optionalParty.get().getPartyId() : null;
        }
        if (partyId == null && pullMessageService.allowDynamicInitiatorInPullProcess()) {
            LOG.debug("Extract partyId from mpc [{}]", mpc);
            partyId = mpcService.extractInitiator(mpc);
        }
        return partyId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PullContext extractProcessOnMpc(final String mpcQualifiedName) {
        try {
            String mpc = mpcQualifiedName;
            final Party gatewayParty = pModeProvider.getGatewayParty();
            List<Process> processes = pModeProvider.findPullProcessByMpc(mpc);
            if (CollectionUtils.isEmpty(processes) && mpcService.forcePullOnMpc(mpc)) {
                LOG.debug("No process corresponds to mpc:[{}]", mpc);
                mpc = mpcService.extractBaseMpc(mpc);
                processes = pModeProvider.findPullProcessByMpc(mpc);
            }
            if (LOG.isDebugEnabled()) {
                for (Process process : processes) {
                    LOG.debug("Process:[{}] correspond to mpc:[{}]", process.getName(), mpc);
                }
            }
            processValidator.validatePullProcess(processes);
            return new PullContext(processes.get(0), gatewayParty, mpc);
        } catch (IllegalArgumentException e) {
            throw new PModeException(DomibusCoreErrorCode.DOM_003, "No pmode configuration found");
        }
    }


    @Override
    @Transactional(noRollbackFor = ReliabilityException.class)
    public RawEnvelopeDto findPulledMessageRawXmlByMessageId(final String messageId) {
        final RawEnvelopeDto rawXmlByMessageId = rawEnvelopeLogDao.findRawXmlByMessageId(messageId);
        if (rawXmlByMessageId == null) {
            throw new ReliabilityException(DomibusCoreErrorCode.DOM_004, "There should always have a raw message for message " + messageId);
        }
        return rawXmlByMessageId;
    }

    /**
     * This method is a bit weird as we delete and save a xml message for the same message id.
     * Saving the raw xml message in the case of the pull is occuring on the last outgoing interceptor in order
     * to have all the cxf message modification saved (reliability check.) Unfortunately this saving is not done in the
     * same transaction.
     *
     * @param rawXml    the soap envelope
     * @param messageId the user message
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveRawXml(String rawXml, String messageId) {
        LOG.debug("Saving rawXML for message [{}]", messageId);

        RawEnvelopeLog newRawEnvelopeLog = new RawEnvelopeLog();
        newRawEnvelopeLog.setRawXML(rawXml);
        newRawEnvelopeLog.setMessageId(messageId);
        rawEnvelopeLogDao.create(newRawEnvelopeLog);
    }

    @Override
    public void verifyReceiverCertificate(final LegConfiguration legConfiguration, String receiverName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy) || policyService.isNoEncryptionPolicy(policy)) {
            LOG.debug("Validation of the receiver certificate is skipped.");
            return;
        }

        if (domibusPropertyProvider.getBooleanProperty(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING)) {
            String chainExceptionMessage = "Cannot send message: receiver certificate is not valid or it has been revoked [" + receiverName + "]";
            try {
                boolean certificateChainValid = multiDomainCertificateProvider.isCertificateChainValid(domainProvider.getCurrentDomain(), receiverName);
                if (!certificateChainValid) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
                }
                LOG.info("Receiver certificate exists and is valid [{}}]", receiverName);
            } catch (DomibusCertificateException | CryptoException e) {
                throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage, e);
            }
        }
    }

    @Override
    public boolean forcePullOnMpc(String mpc) {
        return mpcService.forcePullOnMpc(mpc);
    }

    @Override
    public boolean forcePullOnMpc(UserMessage userMessage) {
        return mpcService.forcePullOnMpc(userMessage);
    }

    @Override
    public String extractInitiator(String mpc) {
        return mpcService.extractInitiator(mpc);
    }

    @Override
    public String extractBaseMpc(String mpc) {
        return mpcService.extractBaseMpc(mpc);
    }

    @Override
    public void verifySenderCertificate(final LegConfiguration legConfiguration, String senderName) {
        Policy policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
        if (policyService.isNoSecurityPolicy(policy)) {
            LOG.debug("Validation of the sender certificate is skipped.");
            return;
        }
        if (domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING)) {
            String chainExceptionMessage = "Cannot send message: sender certificate is not valid or it has been revoked [" + senderName + "]";
            try {
                X509Certificate certificate = multiDomainCertificateProvider.getCertificateFromKeystore(domainProvider.getCurrentDomain(), senderName);
                if (certificate == null) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, "Cannot send message: sender[" + senderName + "] certificate not found in Keystore");
                }
                if (!certificateService.isCertificateValid(certificate)) {
                    throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage);
                }
                LOG.info("Sender certificate exists and is valid [{}]", senderName);
            } catch (DomibusCertificateException | KeyStoreException | CryptoException ex) {
                // Is this an error and we stop the sending or we just log a warning that we were not able to validate the cert?
                // my opinion is that since the option is enabled, we should validate no matter what => this is an error
                throw new ChainCertificateInvalidException(DomibusCoreErrorCode.DOM_001, chainExceptionMessage, ex);
            }
        }
    }
}

