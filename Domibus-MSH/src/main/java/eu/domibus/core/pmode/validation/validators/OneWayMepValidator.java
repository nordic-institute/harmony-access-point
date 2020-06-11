package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidator;
import eu.domibus.ebms3.common.model.MessageExchangePattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.domibus.core.ebms3.Ebms3Constants.ONEWAY_MEP_VALUE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * One-Way mep with binding pushAndPush, pullAndPush and pushAndPull is not valid.
 * This combination generates a warning, not an error, for backward compatibility.
 * <p>
 */
@Component
@Order(6)
public class OneWayMepValidator implements PModeValidator {

    private static final List<String> notAcceptedBindings = Arrays.asList(
            MessageExchangePattern.TWO_WAY_PUSH_PUSH.getUri(),
            MessageExchangePattern.TWO_WAY_PUSH_PULL.getUri(),
            MessageExchangePattern.TWO_WAY_PULL_PUSH.getUri()
    );

    @Override
    public List<ValidationIssue> validate(Configuration configuration) {
        List<ValidationIssue> issues = new ArrayList<>();

        configuration.getBusinessProcesses().getProcesses().forEach(process -> {
            if (process.getMep() != null && ONEWAY_MEP_VALUE.equalsIgnoreCase(process.getMep().getValue())) {
                String binding = process.getMepBinding() == null ? null : process.getMepBinding().getValue();
                if (binding != null) {
                    if (notAcceptedBindings.stream().anyMatch(binding::equalsIgnoreCase)) {
                        String message = String.format("One-way mep with binding [%s] is not valid for process [%s].",
                                process.getMepBinding().getName(), process.getName());
                        issues.add(new ValidationIssue(message, ValidationIssue.Level.WARNING));
                    }
                }
            }
        });
        return Collections.unmodifiableList(issues);
    }

}
