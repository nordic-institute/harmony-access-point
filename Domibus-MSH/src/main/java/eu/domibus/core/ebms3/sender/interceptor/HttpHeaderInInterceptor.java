package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * In Interceptor - it will remove user-agent entry from Http headers if contains Apache CXF version
 *
 * @since 4.2
 * @author Catalin Enache
 */
public class HttpHeaderInInterceptor extends HttpHeaderOutInterceptor {

    public HttpHeaderInInterceptor() {
        super(Phase.PRE_INVOKE);
        addBefore(LoggingInInterceptor.class.getName());
    }

    @Override
    protected DomibusLogger getLogger(){
        return DomibusLoggerFactory.getLogger(HttpHeaderInInterceptor.class);
    }
}
