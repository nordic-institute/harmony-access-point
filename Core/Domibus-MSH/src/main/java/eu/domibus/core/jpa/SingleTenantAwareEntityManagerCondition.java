package eu.domibus.core.jpa;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition evaluating to true when the application is run in single tenancy mode; otherwise, false.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Configuration
public class SingleTenantAwareEntityManagerCondition implements ConfigurationCondition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(SingleTenantAwareEntityManagerCondition.class);

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOGGER.debug("Condition not matching: environment is null");
            return false;
        }

        final MultiTenantAwareEntityManagerCondition multiTenantAwareEntityManagerCondition = new MultiTenantAwareEntityManagerCondition();
        return !multiTenantAwareEntityManagerCondition.matches(context, metadata);
    }
}
