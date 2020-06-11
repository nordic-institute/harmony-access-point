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

import static eu.domibus.core.ebms3.Ebms3Constants.TWOWAY_MEP_VALUE;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Two-Way mep with binding pushAndPush, pullAndPush and pushAndPull are not supported.
 * (These bindings are simulated in Domibus with two One-Way exchanges).
 * <p>
 * Two-Way mep with any other binding is not valid.
 * <p>
 * Both these situations generate warnings, not errors, for backward compatibility.
 * <p>
 */
@Component
@Order(6)
public class TwoWayMepValidator implements PModeValidator {

    private static final List<String> notSupportedBindings = Arrays.asList(
            MessageExchangePattern.TWO_WAY_PUSH_PUSH.getUri(),
            MessageExchangePattern.TWO_WAY_PUSH_PULL.getUri(),
            MessageExchangePattern.TWO_WAY_PULL_PUSH.getUri()
    );

    @Override
    public List<ValidationIssue> validate(Configuration configuration) {
        List<ValidationIssue> issues = new ArrayList<>();

        configuration.getBusinessProcesses().getProcesses().forEach(process -> {
            if (process.getMep() != null && TWOWAY_MEP_VALUE.equalsIgnoreCase(process.getMep().getValue())) {
                String binding = process.getMepBinding() == null ? null : process.getMepBinding().getValue();
                if (binding != null) {
                    if (notSupportedBindings.stream().anyMatch(binding::equalsIgnoreCase)) {
                        String message = String.format("Two-way mep with binding [%s] is not supported for process [%s]. In the pMode XML it is required to use 2 one-way processes to simulate two-way communication.",
                                process.getMepBinding().getName(), process.getName());
                        issues.add(new ValidationIssue(message, ValidationIssue.Level.WARNING));
                    } else {
                        String message = String.format("Two-way mep with binding [%s] is invalid for process [%s].",
                                process.getMepBinding().getName(), process.getName());
                        issues.add(new ValidationIssue(message, ValidationIssue.Level.WARNING));
                    }
                }
            }
        });
        return Collections.unmodifiableList(issues);
    }

}
