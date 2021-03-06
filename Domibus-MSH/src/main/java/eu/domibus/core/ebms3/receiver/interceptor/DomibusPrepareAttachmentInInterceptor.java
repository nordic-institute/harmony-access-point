package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

/**
 * Interceptor used to configure other Domibus custom interceptors while the AS4 messages are handled
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service("domibusPrepareAttachmentInInterceptor")
public class DomibusPrepareAttachmentInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusPrepareAttachmentInInterceptor.class);


    public DomibusPrepareAttachmentInInterceptor() {
        this(Phase.RECEIVE);
    }

    protected DomibusPrepareAttachmentInInterceptor(String phase) {
        super(phase);
        this.addAfter(AttachmentInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        message.getExchange().put(ClientImpl.FINISHED, Boolean.TRUE);
        message.getInterceptorChain().add(new CheckEBMSHeaderInterceptor());
        message.getInterceptorChain().add(new SOAPMessageBuilderInterceptor());
    }
}