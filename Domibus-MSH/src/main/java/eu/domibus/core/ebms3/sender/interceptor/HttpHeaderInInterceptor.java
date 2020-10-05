package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.core.logging.cxf.DomibusLoggingInInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

/**
 * In Interceptor for Apache CXF Http headers
 *
 * @author Catalin Enache
 * @since 4.2
 */
@Service("httpHeaderInInterceptor")
public class HttpHeaderInInterceptor extends HttpHeaderAbstractInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpHeaderInInterceptor.class);

    public HttpHeaderInInterceptor() {
        super(Phase.RECEIVE);
        addBefore(DomibusLoggingInInterceptor.class.getName());
    }

    @Override
    protected DomibusLogger getLogger() {
        return LOG;
    }
}
