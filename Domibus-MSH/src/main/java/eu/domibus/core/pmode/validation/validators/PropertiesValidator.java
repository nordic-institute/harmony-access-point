package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertyRef;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author François Gautier
 * @since 4.2
 *
 * Validates the properties and properties ref between themselves to avoid undefined or unused properties.
 */
@Component
@Order(10)
public class PropertiesValidator implements PModeValidator {

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses()
                .getPropertySets()
                .stream()
                .flatMap(propertySet -> propertySet.getPropertyRef().stream())
                .forEach(propertyRef -> {
                    if (notFoundInProperties(pMode, propertyRef)) {
                        String message = String.format("PropertyRef [%s] is not defined in properties", propertyRef.getProperty());
                        issues.add(new ValidationIssue(message, ValidationIssue.Level.ERROR));
                    }
                });

        return issues;
    }

    private boolean notFoundInProperties(Configuration pMode, PropertyRef propertyRef) {
        for (final Property property : pMode.getBusinessProcesses().getProperties()) {
            if (StringUtils.equals(property.getName(), propertyRef.getProperty())) {
                return false;
            }
        }
        return true;
    }
}
