package eu.domibus.web.filter;

import eu.domibus.api.property.DomibusPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_SESSION_SAME_SITE;

/**
 * Setting the correct value for SameSite attribute for cookies other than the session cookie(XSRF-TOKEN)
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public class CookieFilter extends GenericFilterBean {

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;

        String propertyValue = domibusPropertyProvider.getProperty(DOMIBUS_UI_SESSION_SAME_SITE);
        Collection<String> cookieHeaders = resp.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String cookieHeader : cookieHeaders) {
            String cookieValue = String.format("%s; %s", cookieHeader, " SameSite=" + propertyValue);
            if (firstHeader) {
                resp.setHeader(HttpHeaders.SET_COOKIE, cookieValue);
                firstHeader = false;
                continue;
            }
            resp.addHeader(HttpHeaders.SET_COOKIE, cookieValue);
        }

        chain.doFilter(request, response);
    }

}
