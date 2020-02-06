package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidator;
import eu.domibus.ebms3.common.model.MessageExchangePattern;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Two-Way mep with binding pushAndPush, pullAndPush and pushAndPull are not accepted.
 * These bindings are simulated in Domibus with two One-Way exchange.
 * <p>
 */
@Component
@Order(6)
public class TwoWayMepValidator implements PModeValidator {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TwoWayMepValidator.class);

    private static final String TWOWAY_MEP_VALUE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/twoWay";
    private static final List<String> notAccepted = Arrays.asList(
            MessageExchangePattern.TWO_WAY_PUSH_PUSH.getUri(),
            MessageExchangePattern.TWO_WAY_PUSH_PULL.getUri(),
            MessageExchangePattern.TWO_WAY_PULL_PUSH.getUri()
    );

    @Override
    public List<PModeIssue> validate(Configuration configuration) {
        List<PModeIssue> issues = new ArrayList<>();

        configuration.getBusinessProcesses().getProcesses().forEach(process -> {
            if (process.getMep() != null && TWOWAY_MEP_VALUE.equalsIgnoreCase(process.getMep().getValue())) {
                String binding = process.getMepBinding() == null ? null : process.getMepBinding().getValue();
                if (notAccepted.stream().anyMatch(binding::equalsIgnoreCase)) {
                    issues.add(new PModeIssue("Two-Way mep with binding " + binding + " not accepted for process " + process.getName(), PModeIssue.Level.WARNING));
                }
            }
        });
        return Collections.unmodifiableList(issues);
    }


}
