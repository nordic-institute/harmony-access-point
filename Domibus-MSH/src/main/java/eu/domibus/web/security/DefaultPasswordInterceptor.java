package eu.domibus.web.security;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean checkDefaultPassword = Boolean.parseBoolean(domibusPropertyProvider.getProperty(DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD));
        if (!checkDefaultPassword) {
            return true;
        }

        LOG.debug("Intercepted request for [{}]", request.getRequestURI());

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && (authentication.getPrincipal() instanceof UserDetail)) {
            UserDetail securityUser = (UserDetail) authentication.getPrincipal();
            if (securityUser.isDefaultPasswordUsed()) {
                response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
                return false;
            }
        }
        return true;
    }
}