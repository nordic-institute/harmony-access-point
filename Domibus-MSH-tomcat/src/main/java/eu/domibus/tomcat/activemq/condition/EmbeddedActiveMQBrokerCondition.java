package eu.domibus.tomcat.activemq.condition;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class EmbeddedActiveMQBrokerCondition implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(EmbeddedActiveMQBrokerCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOGGER.debug("Condition not matching: environment is null");
            return false;
        }
        final boolean embeddedActiveMQ = StringUtils.isNotEmpty(environment.getProperty(ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE));
        return embeddedActiveMQ;
    }
}
