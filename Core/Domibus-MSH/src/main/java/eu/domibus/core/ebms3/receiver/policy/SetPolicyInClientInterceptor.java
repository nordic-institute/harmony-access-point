package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.receiver.interceptor.CheckEBMSHeaderInterceptor;
import eu.domibus.core.ebms3.receiver.interceptor.SOAPMessageBuilderInterceptor;
import eu.domibus.core.ebms3.receiver.leg.ClientInMessageLegConfigurationFactory;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service("setPolicyInInterceptorClient")
public class SetPolicyInClientInterceptor extends SetPolicyInInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInClientInterceptor.class);
    public static final ThreadLocal<String> RAW_MESSAGE_XML = new ThreadLocal<>();

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

        if (policyService.isNoSecurityPolicy(policy) == false) {
            final String securityAlgorithm = (String) message.getExchange().get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM);
            if (StringUtils.isBlank(securityAlgorithm)) {
                throwFault(message, ErrorCode.EbMS3ErrorCode.EBMS_0004, "No security algorithm found");
            }
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
        }

        String pModeKeyContextProperty = (String) message.getExchange().get(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);
        if (StringUtils.isBlank(pModeKeyContextProperty)) {
            throwFault(message, ErrorCode.EbMS3ErrorCode.EBMS_0010, "PMode key context property is empty");
        }
        message.put(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, pModeKeyContextProperty);

        message.getInterceptorChain().add(new CheckEBMSHeaderInterceptor());
        message.getInterceptorChain().add(new SOAPMessageBuilderInterceptor());

        try {
            saveRawMessageMessageContext(message);
        } catch (IOException e) {
            LOG.warn("Could not save the raw message envelope content (to have the encrypted data section)", e);
        }
    }

    protected void saveRawMessageMessageContext(SoapMessage message) throws IOException {
        final InputStream inputStream = message.getContent(InputStream.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileCopyUtils.copy(inputStream, bos);
        String rawXMLMessage = IOUtils.toString(bos.toByteArray(), StandardCharsets.UTF_8.name());
        SetPolicyInClientInterceptor.RAW_MESSAGE_XML.set(rawXMLMessage);

        ByteArrayInputStream reusableStream = new ByteArrayInputStream(rawXMLMessage.getBytes(StandardCharsets.UTF_8));
        // Replace the message content with the reusable stream
        message.setContent(InputStream.class, reusableStream);
    }

    protected void throwFault(SoapMessage message, ErrorCode.EbMS3ErrorCode ebMS3ErrorCode, String errorMessage) {
        setBindingOperation(message);
        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);

        throw new Fault(EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(ebMS3ErrorCode)
                .message(errorMessage)
                .refToMessageId( StringUtils.isNotBlank(messageId) ? messageId : "unknown")
                .mshRole(MSHRole.SENDING)
                .build());
    }
}
