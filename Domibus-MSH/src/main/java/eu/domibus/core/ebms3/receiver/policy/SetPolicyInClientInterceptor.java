package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.interceptor.CheckEBMSHeaderInterceptor;
import eu.domibus.core.ebms3.receiver.interceptor.SOAPMessageBuilderInterceptor;
import eu.domibus.core.ebms3.receiver.leg.ClientInMessageLegConfigurationFactory;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.stereotype.Service;

/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service("setPolicyInInterceptorClient")
public class SetPolicyInClientInterceptor extends SetPolicyInInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInClientInterceptor.class);

    protected PModeProvider pModeProvider;
    protected ClientInMessageLegConfigurationFactory clientInMessageLegConfigurationFactory;

    public SetPolicyInClientInterceptor(PModeProvider pModeProvider, ClientInMessageLegConfigurationFactory clientInMessageLegConfigurationFactory) {
        this.pModeProvider = pModeProvider;
        this.clientInMessageLegConfigurationFactory = clientInMessageLegConfigurationFactory;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Policy policy = (Policy) message.getExchange().get(PolicyConstants.POLICY_OVERRIDE);
        if (policy == null) {
            throwFault(message, ErrorCode.EbMS3ErrorCode.EBMS_0010, "No valid security policy found");
        }
        message.put(PolicyConstants.POLICY_OVERRIDE, policy);

        final String securityAlgorithm = (String) message.getExchange().get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM);
        if (StringUtils.isBlank(securityAlgorithm)) {
            throwFault(message, ErrorCode.EbMS3ErrorCode.EBMS_0004, "No security algorithm found");
        }

        message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);

        message.getInterceptorChain().add(new CheckEBMSHeaderInterceptor());
        message.getInterceptorChain().add(new SOAPMessageBuilderInterceptor());
    }

    protected void throwFault(SoapMessage message, ErrorCode.EbMS3ErrorCode ebMS3ErrorCode, String errorMessage) {
        setBindingOperation(message);
        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);

        EbMS3Exception ex = new EbMS3Exception(ebMS3ErrorCode, errorMessage, StringUtils.isNotBlank(messageId) ? messageId : "unknown", null);
        ex.setMshRole(MSHRole.SENDING);
        throw new Fault(ex);
    }
}
