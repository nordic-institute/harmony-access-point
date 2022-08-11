package eu.domibus.core.spring;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletContext;

import static eu.domibus.core.spring.DomibusSessionInitializer.SESSION_INITIALIZER_ORDER;

/**
 * WebApplicationInitializer needed by Spring Session to insert the session filter as the first one;
 * hence the highest precedence, even higher than DomibusApplicationInitializer
 *
 * @author Ion Perpegel
 * @since 5.0
 * Makes sure this is executed first so that the Spring session filter is added before anybody else
 */
@Order(SESSION_INITIALIZER_ORDER)
public class DomibusSessionInitializer extends AbstractHttpSessionApplicationInitializer {
    // HIGHEST_PRECEDENCE is negative so adding one actually decreases the precedence
    public static final int SESSION_INITIALIZER_ORDER = Ordered.HIGHEST_PRECEDENCE;

    private final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusSessionInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        ClassLoader classLoader = servletContext.getClassLoader();
        boolean externalAuthenticationProviderPresent = ClassUtils
                .isPresent("eu.domibus.weblogic.security.ECASSecurityConfiguration", classLoader);
        if(externalAuthenticationProviderPresent) {
            LOG.info("Skipping initializing the Spring Session in EU Login");
            return;
        }

        super.onStartup(servletContext);
    }
}
