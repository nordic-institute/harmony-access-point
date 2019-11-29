package eu.domibus.common.metrics;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import java.io.IOException;

import static eu.domibus.common.metrics.MetricNames.SERVLET_INCOMING_USER_MESSAGE;

@Component
public class MetricsServletFilter implements Filter {

    @PostConstruct
    public void init(){
        System.out.println("MetricsServletFilter");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    @Timer(value = SERVLET_INCOMING_USER_MESSAGE)
    @Counter(SERVLET_INCOMING_USER_MESSAGE)
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
