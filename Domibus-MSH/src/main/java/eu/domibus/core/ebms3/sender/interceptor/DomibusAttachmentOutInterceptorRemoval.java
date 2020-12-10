package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.core.metrics.MetricsHelper;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;

@Service("domibusAttachmentOutInterceptorRemoval")
public class DomibusAttachmentOutInterceptorRemoval extends AbstractSoapInterceptor {
    public DomibusAttachmentOutInterceptorRemoval() {
        super(Phase.PRE_STREAM);
        super.addBefore(AttachmentOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) {
        com.codahale.metrics.Timer.Context methodTimer = MetricsHelper.getMetricRegistry().timer(name(DomibusAttachmentOutInterceptorRemoval.class, "handleMessage", "timer")).time();

        if (message == null ||
                message.getInterceptorChain() == null ||
                message.getInterceptorChain().iterator() == null)
            return;

        Iterator<Interceptor<? extends Message>> it = message.getInterceptorChain().iterator();
        while (it.hasNext()) {
            Interceptor interceptor = it.next();
            if (interceptor.getClass().getName().equals(AttachmentOutInterceptor.class.getName())) {
                message.getInterceptorChain().remove(interceptor);
                return;
            }
        }

        methodTimer.stop();
    }
}
