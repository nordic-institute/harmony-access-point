package eu.domibus.core.dss;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class UseDssCondition implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(UseDssCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final Environment environment = context.getEnvironment();
        if (environment == null) {
            LOG.debug("Condition not matching: environment is null");
            return false;
        }
        final String property = environment.getProperty("domibus.dss.load");
        return StringUtils.isNotEmpty(property) && Boolean.valueOf(property);
    }
}
