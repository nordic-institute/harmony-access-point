package eu.domibus.tomcat.activemq.condition;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Configuration
public class DummyEmbeddedActiveMQCondition implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DummyEmbeddedActiveMQCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final boolean dummyActiveMQBroker = !new EmbeddedActiveMQBrokerCondition().matches(context, metadata);
        LOGGER.debug("Condition result is [{}]", dummyActiveMQBroker);
        return dummyActiveMQBroker;
    }

}
