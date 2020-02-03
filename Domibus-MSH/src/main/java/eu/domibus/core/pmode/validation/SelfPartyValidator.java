package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
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
@Order(1)
public class SelfPartyValidator implements PModeValidator {
    @Override
    public List<PModeIssue> validate(Configuration pMode) {
        List<PModeIssue> issues = new ArrayList<>();
        if (pMode == null) {
            return issues;
        }

        String partyName = pMode.getParty().getName();
        if (!pMode.getBusinessProcesses().getParties().stream().anyMatch(party -> party.getName().equals(partyName))) {
            String message = String.format("Party [%s] not found in business process parties", partyName);
            issues.add(new PModeIssue(message, PModeIssue.Level.ERROR));
        }

        return issues;
    }
}
