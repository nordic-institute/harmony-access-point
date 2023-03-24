package eu.domibus.web.security;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static eu.domibus.logging.DomibusLogger.MDC_USER;

/**
 * A Spring MVC interceptor that ensures all authenticated REST calls are making their principal available on the MDC.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
public class AuthenticatedPrincipalInterceptor extends HandlerInterceptorAdapter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthenticatedPrincipalInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LOG.trace("Trying to add any authenticated principal information on request [{}]", request.getRequestURI());

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        if(principal != null) {
            LOG.trace("Adding the authenticated principal name [{}]", principal.getName());
            LOG.putMDC(MDC_USER, principal.getName());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        LOG.trace("Removing any authenticated principal information present from request [{}]", request.getRequestURI());
        LOG.removeMDC(MDC_USER);
    }
}
