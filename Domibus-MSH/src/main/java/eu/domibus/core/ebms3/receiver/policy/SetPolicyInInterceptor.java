package eu.domibus.core.ebms3.receiver.policy;

import eu.domibus.core.ebms3.receiver.leg.MessageLegConfigurationFactory;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.message.SoapService;
import eu.domibus.core.property.DomibusVersionService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * This interceptor is responsible for discovery and setup of WS-Security Policies for incoming messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */
public abstract class SetPolicyInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetPolicyInInterceptor.class);

    @Autowired
    protected SoapService soapService;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected DomibusVersionService domibusVersionService;

    protected MessageLegConfigurationFactory messageLegConfigurationFactory;

    public SetPolicyInInterceptor() {
        this(Phase.RECEIVE);
    }

    protected SetPolicyInInterceptor(String phase) {
        super(phase);
        this.addBefore(PolicyInInterceptor.class.getName());
        this.addAfter(AttachmentInInterceptor.class.getName());
    }

    public void setMessageLegConfigurationFactory(MessageLegConfigurationFactory messageLegConfigurationFactory) {
        this.messageLegConfigurationFactory = messageLegConfigurationFactory;
    }

    //this is a hack to avoid a nullpointer in @see WebFaultOutInterceptor.
    //I suppose the bindingOperation is set after the execution of this interceptor and is empty in case of error.

    protected void setBindingOperation(SoapMessage message) {
        final Exchange exchange = message.getExchange();
        if (exchange == null) {
            return;
        }
        final Endpoint endpoint = exchange.getEndpoint();
        if (endpoint == null) {
            return;
        }
        final EndpointInfo endpointInfo = endpoint.getEndpointInfo();
        if (endpointInfo == null) {
            return;
        }
        final BindingInfo binding = endpointInfo.getBinding();
        if (binding == null) {
            return;
        }
        final Collection<BindingOperationInfo> operations = binding.getOperations();
        if (operations == null) {
            return;
        }
        for (BindingOperationInfo operation : operations) {
            exchange.put(BindingOperationInfo.class, operation);
        }
    }

}


