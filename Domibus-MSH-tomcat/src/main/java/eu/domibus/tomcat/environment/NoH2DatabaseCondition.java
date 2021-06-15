package eu.domibus.tomcat.environment;

import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NoH2DatabaseCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Environment environment = conditionContext.getEnvironment();
        String currentDatabase = environment.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME);
        return !StringUtils.contains(currentDatabase, "org.h2.Driver");

    }
}
