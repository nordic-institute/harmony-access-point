package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * In Interceptor for Apache CXF Http headers
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class HttpHeaderInInterceptor extends HttpHeaderOutInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpHeaderInInterceptor.class);

    public HttpHeaderInInterceptor() {
        super(Phase.PRE_INVOKE);
        addBefore(LoggingInInterceptor.class.getName());
    }

    @Override
    protected DomibusLogger getLogger(){
        return LOG;
    }
}
