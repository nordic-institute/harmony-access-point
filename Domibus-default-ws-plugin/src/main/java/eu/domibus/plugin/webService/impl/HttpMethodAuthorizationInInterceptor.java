package eu.domibus.plugin.webService.impl;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

@Component
public class HttpMethodAuthorizationInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(eu.domibus.plugin.webService.impl.HttpMethodAuthorizationInInterceptor.class);

    public HttpMethodAuthorizationInInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final String httpMethod = (String) message.get("org.apache.cxf.request.method");

        if (StringUtils.containsIgnoreCase(httpMethod, HttpMethod.GET)) {
            LOG.debug("Detected GET request: aborting the interceptor chain");
            message.getInterceptorChain().abort();

            final HttpServletResponse httpResponse = (HttpServletResponse) message.get("HTTP.RESPONSE");
            httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            httpResponse.addHeader("Allow", HttpMethod.POST);
            return;
        }
    }
}
