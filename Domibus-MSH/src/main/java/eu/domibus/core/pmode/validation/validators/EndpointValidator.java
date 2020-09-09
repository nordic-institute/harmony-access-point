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
 * Validates the endpoint of each party.
 */
@Component
@Order(2)
public class EndpointValidator implements PModeValidator {

    final BusinessProcessValidator businessProcessValidator;

    public EndpointValidator(BusinessProcessValidator businessProcessValidator) {
        this.businessProcessValidator = businessProcessValidator;
    }


    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getParties().forEach(party -> {
            if (StringUtils.isEmpty(party.getEndpoint())) {
                String message = String.format("Party [%s] should not have an empty endpoint.", party.getName());
                issues.add(new ValidationIssue(message, ValidationIssue.Level.WARNING));
            }else {
                issues.addAll(businessProcessValidator.validateForbiddenCharacters(party.getEndpoint(), "endpoint [" + party.getEndpoint() + "] for the party [" + party.getName() + "]."));
            }
        });

        return issues;
    }
}
