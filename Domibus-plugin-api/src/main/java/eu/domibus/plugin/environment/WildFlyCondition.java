package eu.domibus.plugin.environment;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition for the WildFly server
 * @author Cosmin Baciu
 * @since 4.2
 */
public class WildFlyCondition implements Condition {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WildFlyCondition.class);

    /**
     * Returns true if the current server is WildFly
     *
     * @param context  Context information {@link ConditionContext}
     * @param metadata Defines access to the annotations of a specific type {@link AnnotatedTypeMetadata}
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DomibusEnvironmentUtil.INSTANCE.isWildFly(context.getEnvironment());
    }
}