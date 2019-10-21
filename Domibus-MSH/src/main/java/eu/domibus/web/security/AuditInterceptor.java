package eu.domibus.web.security;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
public class AuditInterceptor extends HandlerInterceptorAdapter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditInterceptor.class);

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        LOG.debug("Adding audit information on request [{}]", request.getRequestURI());

        if(SecurityContextHolder.getContext().getAuthentication() != null) {
            LOG.putMDC(MDC_USER, SecurityContextHolder.getContext().getAuthentication().getName());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        LOG.debug("Removing audit information on request [{}]", request.getRequestURI());

        LOG.removeMDC(MDC_USER);
    }
}
