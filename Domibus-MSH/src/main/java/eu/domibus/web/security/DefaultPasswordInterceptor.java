package eu.domibus.web.security;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.IDomibusUserDetails;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class DefaultPasswordInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(DefaultPasswordInterceptor.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private AuthUtils authUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean checkDefaultPassword = Boolean.parseBoolean(domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD));
        if (!checkDefaultPassword) {
            return true;
        }

        LOG.debug("Intercepted request for [{}]", request.getRequestURI());

        final IDomibusUserDetails userDetails = authUtils.getUserDetails();
        if (userDetails != null && userDetails.isDefaultPasswordUsed()) {
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
            return false;
        }
        return true;
    }
}
