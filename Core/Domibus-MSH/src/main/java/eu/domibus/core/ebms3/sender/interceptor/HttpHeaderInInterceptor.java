package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
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
    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpHeaderInInterceptor.class);

    public HttpHeaderInInterceptor() {
        super(Phase.PRE_INVOKE);
        addBefore(LoggingInInterceptor.class.getName());
    }

    @Override
    protected IDomibusLogger getLogger() {
        return LOG;
    }
}
