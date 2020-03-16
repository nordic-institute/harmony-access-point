package eu.domibus.core.ebms3.receiver;

import eu.domibus.common.DomibusStatusService;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 *
 * First in interceptor, verifies that the system is ready for incoming request.
 * @see DomibusStatusService for more details.
 */
public class DomibusReadyInterceptor extends AbstractPhaseInterceptor {

    @Autowired
    private DomibusStatusService domibusStatusService;

    public DomibusReadyInterceptor() {
        super("receive");
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (domibusStatusService.isNotReady()) {
            throw new Fault(new IllegalStateException("Server starting"));
        }
    }

}
