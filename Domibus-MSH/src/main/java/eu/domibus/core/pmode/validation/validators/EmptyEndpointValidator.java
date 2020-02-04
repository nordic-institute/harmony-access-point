package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
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
 * Validates self party existence  aka checks if configuration/@party is found in businessProcesses/parties collection
 */
@Component
@Order(4)
public class EmptyEndpointValidator implements PModeValidator {

    @Override
    public List<PModeIssue> validate(Configuration pMode) {
        List<PModeIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getParties().forEach(party -> {
            if (StringUtils.isEmpty(party.getEndpoint())) {
                String message = String.format("Party [%s] should not have an empty endpoint.", party.getName());
                issues.add(new PModeIssue(message, PModeIssue.Level.ERROR));
            }
        });

        return issues;
    }
}
