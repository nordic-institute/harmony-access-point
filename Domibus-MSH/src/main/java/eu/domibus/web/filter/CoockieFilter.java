package eu.domibus.web.filter;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public class CoockieFilter extends GenericFilterBean {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(CoockieFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;

        Collection<String> headers = resp.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers) { // there can be multiple Set-Cookie attributes
            if (firstHeader) {
                resp.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, " SameSite=None; Secure"));
                firstHeader = false;
                continue;
            }
            resp.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, " SameSite=None; Secure"));
        }
        
        chain.doFilter(request, response);
    }

}
