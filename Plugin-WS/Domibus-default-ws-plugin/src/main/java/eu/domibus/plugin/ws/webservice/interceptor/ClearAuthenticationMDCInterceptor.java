package eu.domibus.plugin.ws.webservice.interceptor;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;

/**
 * CXF interceptor that clears the authenticated user from the MDC context after the execution of the web service
 */
@Component(value = "clearAuthenticationMDCInterceptor")
public class ClearAuthenticationMDCInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearAuthenticationMDCInterceptor.class);

    public ClearAuthenticationMDCInterceptor() {
        super(Phase.SETUP_ENDING);
    }

    @Override
    public void handleMessage(Message message) {
        LOG.debug("handleMessage");
        clearAuthenticationMDC();
    }

    @Override
    public void handleFault(Message message) {
        LOG.debug("handleFault");
        clearAuthenticationMDC();
    }

    private void clearAuthenticationMDC() {
        LOG.removeMDC(IDomibusLogger.MDC_USER);
        LOG.debug("Cleared MDC property [{}]", LOG.getMDCKey(IDomibusLogger.MDC_USER));
    }
}
