package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.crypto.SecurityProfileService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyOutInterceptor;
import org.apache.cxf.ws.policy.PolicyVerificationOutInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * This interceptor is responsible for discovery and setup of WS-Security Policies for outgoing messages
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service("setPolicyOutInterceptor")
public class SetPolicyOutInterceptor extends AbstractSoapInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyOutInterceptor.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected SecurityProfileService securityProfileService;

    public SetPolicyOutInterceptor() {
        super(Phase.SETUP);
        this.addBefore(PolicyOutInterceptor.class.getName());
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will
     * take care of this.
     *
     * @param message the message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        LOG.debug("SetPolicyOutInterceptor");
        final String pModeKey = (String) message.getContextualProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
        LOG.debug("Using pModeKey [{}]", pModeKey);
        message.getExchange().put(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pModeKey);
        message.getInterceptorChain().add(new PrepareAttachmentInterceptor());

        final LegConfiguration legConfiguration = this.pModeProvider.getLegConfiguration(pModeKey);

        Policy policy;
        try {
            policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy(), legConfiguration.getSecurity().getProfile());
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_POLICY_OUTGOING_USE, legConfiguration.getSecurity().getPolicy());
        } catch (final ConfigurationException e) {
            LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_OUTGOING_NOT_FOUND, e, legConfiguration.getSecurity().getPolicy());
            throw new Fault(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                    .message("Could not find policy file " + domibusConfigurationService.getConfigLocation() + "/" + this.pModeProvider.getLegConfiguration(pModeKey).getSecurity())
                    .build());
        }

        if (!policyService.isNoSecurityPolicy(policy)) {
            message.put(SecurityConstants.USE_ATTACHMENT_ENCRYPTION_CONTENT_ONLY_TRANSFORM, true);

            final String securityAlgorithm = securityProfileService.getSecurityAlgorithm(legConfiguration);
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            message.getExchange().put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);

            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_ALGORITHM_OUTGOING_USE, securityAlgorithm);

            String receiverPartyName = extractReceiverPartyName(pModeKey);
            String encryptionAlias = securityProfileService.getAliasForEncrypting(legConfiguration, receiverPartyName);

            message.put(SecurityConstants.ENCRYPT_USERNAME, encryptionAlias);
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_USER_OUTGOING_USE, encryptionAlias);
        }

        message.put(PolicyConstants.POLICY_OVERRIDE, policy);
        message.getExchange().put(PolicyConstants.POLICY_OVERRIDE, policy);
    }

    protected String extractReceiverPartyName(String pModeKey) {
        String receiverPartyName = null;
        try {
            Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            if (receiverParty != null) {
                receiverPartyName = receiverParty.getName();
            }
        } catch (ConfigurationException exc) {
            LOG.info("Responder party was not found, will be extracted from pModeKey.");
        }
        if (receiverPartyName == null) {
            receiverPartyName = pModeProvider.getReceiverPartyNameFromPModeKey(pModeKey);
        }

        return receiverPartyName;
    }


    public static class LogAfterPolicyCheckInterceptor extends AbstractSoapInterceptor {


        public LogAfterPolicyCheckInterceptor() {
            super(Phase.POST_STREAM);
            this.addAfter(PolicyVerificationOutInterceptor.class.getName());
        }

        @Override
        public void handleMessage(final SoapMessage message) throws Fault {

            final SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
            soapMessage.removeAllAttachments();
        }
    }
}
