package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Out Interceptor for Apache CXF Http headers
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class HttpHeaderOutInterceptor extends HttpHeaderAbstractInterceptor {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpHeaderOutInterceptor.class);


    public HttpHeaderOutInterceptor() {
        super(Phase.PRE_STREAM);
        addBefore(LoggingOutInterceptor.class.getName());
    }

    public HttpHeaderOutInterceptor(String phase) {
        super(phase);
    }


    @Override
    protected DomibusLogger getLogger() {
        return LOG;
    }

}
