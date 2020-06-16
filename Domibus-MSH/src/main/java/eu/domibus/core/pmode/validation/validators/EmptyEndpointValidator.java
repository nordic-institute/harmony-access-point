package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Validates that each party has a non-empty endpoint.
 */
@Component
@Order(2)
public class EmptyEndpointValidator implements PModeValidator {

    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getParties().forEach(party -> {
            if (StringUtils.isEmpty(party.getEndpoint())) {
                String message = String.format("Party [%s] should not have an empty endpoint.", party.getName());
                issues.add(new ValidationIssue(message, ValidationIssue.Level.WARNING));
            }
        });

        return issues;
    }
}
