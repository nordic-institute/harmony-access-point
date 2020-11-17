package eu.domibus.core.spring;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * WebApplicationInitializer needed by Spring Session to insert the session filter as the first one;
 * hence the highest precedence, even higher than DomibusApplicationInitializer
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionInitializer extends AbstractHttpSessionApplicationInitializer {
}
