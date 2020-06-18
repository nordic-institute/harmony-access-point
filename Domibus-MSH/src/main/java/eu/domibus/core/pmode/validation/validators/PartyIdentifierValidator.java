package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import eu.domibus.core.pmode.validation.PModeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Soumya Chandran
 * @since 4.2
 * Validates duplicate Identifiers in the party.
 */
@Component
@Order(9)
public class PartyIdentifierValidator implements PModeValidator {

    @Autowired
    PModeValidationHelper pModeValidationHelper;


    /**
     * Validate the Identifiers in the pmode
     * @param pMode
     * @return list of ValidationIssue
     */
    @Override
    public List<ValidationIssue> validate(Configuration pMode) {
        List<ValidationIssue> issues = new ArrayList<>();

        pMode.getBusinessProcesses().getParties().forEach(party -> {
            validateDuplicatePartyIdentifiers(issues, party);
        });

        return issues;
    }

    /**
     * check the duplicate identifiers of the parties
     * @param issues
     * @param party
     * @return list of ValidationIssue
     */
    protected List<ValidationIssue> validateDuplicatePartyIdentifiers(List<ValidationIssue> issues, Party party) {
        party.getIdentifiers().forEach(identifier -> {
            long duplicateIdentifiersCount = party.getIdentifiers().stream().filter(identifier1 -> identifier1.equals(identifier)).count();
            if (duplicateIdentifiersCount > 1) {
                createIssue(issues, identifier.getPartyId(), party.getName(), "Duplicate party identifier [%s] found for the party [%s]");
            }
        });
        return issues;
    }

    /**
     * Creates pmode validation issue
     * @param issues
     * @param partyId
     * @param name
     * @param message
     */
    protected void createIssue(List<ValidationIssue> issues, String partyId, String name, String message) {
        issues.add(pModeValidationHelper.createValidationIssue(message, partyId, name));
    }

}
