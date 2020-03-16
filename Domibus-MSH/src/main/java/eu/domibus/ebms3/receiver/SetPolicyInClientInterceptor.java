package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.exception.EbMS3Exception;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 4.1
 */
public class SetPolicyInClientInterceptor extends SetPolicyInInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInClientInterceptor.class);

    @Autowired
    protected PModeProvider pModeProvider;

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
