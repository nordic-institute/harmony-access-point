package eu.domibus.plugin.environment;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition for the WebLogic server
 * @author Cosmin Baciu
 * @since 4.2
 */
public class WebLogicCondition implements Condition {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WebLogicCondition.class);

    /**
     * Returns true if the current server is WebLogic
     *
     * @param context  Context information {@link ConditionContext}
     * @param metadata Defines access to the annotations of a specific type {@link AnnotatedTypeMetadata}
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DomibusEnvironmentUtil.isWebLogic(context.getEnvironment());
    }
}