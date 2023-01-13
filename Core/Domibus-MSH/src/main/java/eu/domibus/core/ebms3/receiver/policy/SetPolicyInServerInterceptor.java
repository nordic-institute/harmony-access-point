package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.receiver.interceptor.CheckEBMSHeaderInterceptor;
import eu.domibus.core.ebms3.receiver.interceptor.SOAPMessageBuilderInterceptor;
import eu.domibus.core.ebms3.receiver.leg.LegConfigurationExtractor;
import eu.domibus.core.ebms3.receiver.leg.ServerInMessageLegConfigurationFactory;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.message.TestMessageValidator;
import eu.domibus.core.message.UserMessageErrorCreator;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.util.SecurityProfileService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.converters.ConverterException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;


/**
 * @author Thomas Dussart
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SetPolicyInServerInterceptor extends SetPolicyInInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInServerInterceptor.class);

    protected final ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    protected final BackendNotificationService backendNotificationService;

    protected final TestMessageValidator testMessageValidator;

    protected final Ebms3Converter ebms3Converter;

    protected final UserMessageErrorCreator userMessageErrorCreator;

    private final SecurityProfileService securityProfileService;

    public SetPolicyInServerInterceptor(ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory,
                                        BackendNotificationService backendNotificationService,
                                        TestMessageValidator testMessageValidator, Ebms3Converter ebms3Converter,
                                        UserMessageErrorCreator userMessageErrorCreator, SecurityProfileService securityProfileService) {
        this.serverInMessageLegConfigurationFactory = serverInMessageLegConfigurationFactory;
        this.backendNotificationService = backendNotificationService;
        this.testMessageValidator = testMessageValidator;
        this.ebms3Converter = ebms3Converter;
        this.userMessageErrorCreator = userMessageErrorCreator;
        this.securityProfileService = securityProfileService;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        final String httpMethod = (String) message.get("org.apache.cxf.request.method");
        //TODO add the below logic to a separate interceptor
        if (StringUtils.containsIgnoreCase(httpMethod, HttpMethod.GET)) {
            LOG.debug("Detected GET request on MSH: aborting the interceptor chain");
            message.getInterceptorChain().abort();

            final HttpServletResponse response = (HttpServletResponse) message.get(AbstractHTTPDestination.HTTP_RESPONSE);
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                response.getWriter().println(domibusVersionService.getBuildDetails());
            } catch (IOException ex) {
                throw new Fault(ex);
            }
            return;
        }

        String policyName = null;
        String messageId = null;
        LegConfiguration legConfiguration = null;
        Ebms3Messaging ebms3Messaging = null;

        try {
            ebms3Messaging = soapService.getMessage(message);
            message.getExchange().put(MessageConstants.EMBS3_MESSAGING_OBJECT, ebms3Messaging);
            message.put(DispatchClientDefaultProvider.MESSAGING_KEY_CONTEXT_PROPERTY, ebms3Messaging);

            LegConfigurationExtractor legConfigurationExtractor = serverInMessageLegConfigurationFactory.extractMessageConfiguration(message, ebms3Messaging);
            if (legConfigurationExtractor == null) return;

            legConfiguration = legConfigurationExtractor.extractMessageConfiguration();
            policyName = legConfiguration.getSecurity().getPolicy();
            Policy policy = policyService.parsePolicy("policies" + File.separator + policyName);

            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_USE, policyName);

            message.getExchange().put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.getInterceptorChain().add(new CheckEBMSHeaderInterceptor());
            message.getInterceptorChain().add(new SOAPMessageBuilderInterceptor());

            String securityAlgorithm = securityProfileService.getSecurityAlgorithm(legConfiguration);

            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            message.getExchange().put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_ALGORITHM_INCOMING_USE, securityAlgorithm);

        } catch (EbMS3Exception e) {
            setBindingOperation(message);
            LOG.debug("", e); // Those errors are expected (no PMode found, therefore DEBUG)
            processPluginNotification(e, legConfiguration, ebms3Messaging);
            logIncomingMessaging(message);
            throw new Fault(e);
        } catch (IOException | JAXBException e) {
            setBindingOperation(message);
            LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_NOT_FOUND, e, policyName); // Those errors are not expected
            throw new Fault(EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message("no valid security policy found")
                    .refToMessageId(ebms3Messaging != null ? messageId : "unknown")
                    .cause(e)
                    .mshRole(MSHRole.RECEIVING)
                    .build());
        }
    }

    protected void processPluginNotification(EbMS3Exception e, LegConfiguration legConfiguration, Ebms3Messaging ebms3Messaging) {
        if (ebms3Messaging == null || ebms3Messaging.getUserMessage() == null) {
            LOG.debug("Messaging header is empty");
            return;
        }
        final UserMessage userMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getUserMessage());

        final String messageId = userMessage.getMessageId();
        if (legConfiguration == null) {
            if (e == null) {
                // no pmode mismatch/misconfiguration issues
                LOG.debug("LegConfiguration is null for messageId=[{}] we will not notify backend plugins", messageId);
            } else {
                // if pmode misconfigured in c3, then notify the plugins
                backendNotificationService.notifyMessageReceivedFailure(userMessage, userMessageErrorCreator.createErrorResult(e));
            }
            return;
        }
        boolean testMessage = testMessageValidator.checkTestMessage(userMessage);
        try {
            if (!testMessage && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) {
                backendNotificationService.notifyMessageReceivedFailure(userMessage, userMessageErrorCreator.createErrorResult(e));
            }
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messageId);
        }
    }

    protected void logIncomingMessaging(SoapMessage message) {
        if (message == null) {
            LOG.debug("SoapMessage is null");
            return;
        }
        try {
            String xml = soapService.getMessagingAsRAWXml(message);
            LOG.error("EbMS3Exception caused by incoming message: {}", xml);

        } catch (ConverterException | IOException | EbMS3Exception | TransformerException e) {
            LOG.error("Error while getting Soap Envelope", e);
        }
    }

}
