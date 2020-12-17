
package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;
import org.springframework.stereotype.Service;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service("setSignatureAlgorithmInInterceptor")
public class SetSignatureAlgorithmInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetSignatureAlgorithmInInterceptor.class);

    public SetSignatureAlgorithmInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(PolicyInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {

        final Object signatureAlgorithm = message.getContextualProperty(DispatchClientDefaultProvider.ASYMMETRIC_SIG_ALGO_PROPERTY);
        if (signatureAlgorithm != null) {
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, signatureAlgorithm);
        }
    }
}
