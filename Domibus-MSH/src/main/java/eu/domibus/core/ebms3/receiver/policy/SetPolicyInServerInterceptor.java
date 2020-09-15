package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.interceptor.CheckEBMSHeaderInterceptor;
import eu.domibus.core.ebms3.receiver.interceptor.SOAPMessageBuilderInterceptor;
import eu.domibus.core.ebms3.receiver.leg.LegConfigurationExtractor;
import eu.domibus.core.ebms3.receiver.leg.ServerInMessageLegConfigurationFactory;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.core.message.UserMessageHandlerService;
import eu.domibus.core.plugin.notification.BackendNotificationService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.xml.bind.JAXBException;
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

    protected ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory;

    protected BackendNotificationService backendNotificationService;

    protected UserMessageHandlerService userMessageHandlerService;

    public SetPolicyInServerInterceptor(ServerInMessageLegConfigurationFactory serverInMessageLegConfigurationFactory,
                                        BackendNotificationService backendNotificationService,
                                        UserMessageHandlerService userMessageHandlerService
                                        ) {
        this.serverInMessageLegConfigurationFactory = serverInMessageLegConfigurationFactory;
        this.backendNotificationService = backendNotificationService;
        this.userMessageHandlerService = userMessageHandlerService;
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

        Messaging messaging = null;
        String policyName = null;
        String messageId = null;
        LegConfiguration legConfiguration = null;

        try {
            messaging = soapService.getMessage(message);
            message.put(DispatchClientDefaultProvider.MESSAGING_KEY_CONTEXT_PROPERTY, messaging);

            LegConfigurationExtractor legConfigurationExtractor = serverInMessageLegConfigurationFactory.extractMessageConfiguration(message, messaging);
            if (legConfigurationExtractor == null) return;

            legConfiguration = legConfigurationExtractor.extractMessageConfiguration();
            policyName = legConfiguration.getSecurity().getPolicy();
            Policy policy = policyService.parsePolicy("policies" + File.separator + policyName);

            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_USE, policyName);

            message.getExchange().put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.put(PolicyConstants.POLICY_OVERRIDE, policy);
            message.getInterceptorChain().add(new CheckEBMSHeaderInterceptor());
            message.getInterceptorChain().add(new SOAPMessageBuilderInterceptor());
            final String securityAlgorithm = legConfiguration.getSecurity().getSignatureMethod().getAlgorithm();
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            message.getExchange().put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, securityAlgorithm);
            LOG.businessInfo(DomibusMessageCode.BUS_SECURITY_ALGORITHM_INCOMING_USE, securityAlgorithm);

        } catch (EbMS3Exception e) {
            setBindingOperation(message);
            LOG.debug("", e); // Those errors are expected (no PMode found, therefore DEBUG)
            processPluginNotification(e, legConfiguration, messaging);
            throw new Fault(e);
        } catch (IOException | JAXBException e) {
            setBindingOperation(message);
            LOG.businessError(DomibusMessageCode.BUS_SECURITY_POLICY_INCOMING_NOT_FOUND, e, policyName); // Those errors are not expected
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "no valid security policy found", messaging != null ? messageId : "unknown", e);
            ex.setMshRole(MSHRole.RECEIVING);
            throw new Fault(ex);
        }
    }

    protected void processPluginNotification(EbMS3Exception e, LegConfiguration legConfiguration, Messaging messaging) {
        if (messaging == null) {
            LOG.debug("Messaging header is empty");
            return;
        }
        final String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        boolean testMessage = userMessageHandlerService.checkTestMessage(messaging.getUserMessage());
        if (legConfiguration == null) {
            LOG.debug("LegConfiguration is null for messageId=[{}] we will not notify backend plugins", messageId);
            return;
        }
        try {
            if (!testMessage && legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer()) {
                backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), userMessageHandlerService.createErrorResult(e));
            }
        } catch (Exception ex) {
            LOG.businessError(DomibusMessageCode.BUS_BACKEND_NOTIFICATION_FAILED, ex, messageId);
        }
    }

}
