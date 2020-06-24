package eu.domibus.core.pmode.validation.validators;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
public class PartyIdentifierValidatorTest {

    @Tested
    PartyIdentifierValidator partyIdentifierValidator;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Test
    public void testValidate(@Injectable Configuration pMode, @Injectable Party party) {
        List<Party> parties = new ArrayList<>();
        parties.add(party);

        new Expectations(partyIdentifierValidator) {{
            pMode.getBusinessProcesses().getParties();
            result = parties;

        }};
        partyIdentifierValidator.validate(pMode);

        new Verifications() {{
            partyIdentifierValidator.validateDuplicatePartyIdentifiers((Party) any);
        }};
    }


    @Test
    public void testValidateDuplicatePartyIdentifiers(@Injectable Party party1,
                                                      @Injectable Identifier identifier1
    ) {


        List<Identifier> identifiers = new ArrayList<>();

        identifiers.add(identifier1);
        identifiers.add(identifier1);

        new Expectations(partyIdentifierValidator) {{
            party1.getName();
            result = "blue_gw";
            identifier1.getPartyId();
            result = "domibus-blue";
            party1.getIdentifiers();
            result = identifiers;
        }};

        //tested method
        partyIdentifierValidator.validateDuplicatePartyIdentifiers(party1);

        new FullVerifications(partyIdentifierValidator) {{
            partyIdentifierValidator.createIssue(identifier1.getPartyId(), party1.getName(), anyString);
        }};
    }

    @Test
    public void testvalidateDuplicateIdentifiersInAllParties(@Injectable Party party1,
                                                             @Injectable Party party2,
                                                             @Injectable Identifier identifier) {


        List<Party> allParties = new ArrayList<>();
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        allParties.add(party1);
        allParties.add(party2);
        new Expectations(partyIdentifierValidator) {{
            party1.getName();
            result = "blue_gw";
            identifier.getPartyId();
            result = "domibus-blue";
            party1.getIdentifiers();
            result = identifiers;
            party2.getName();
            result = "red_gw";
            identifier.getPartyId();
            result = "domibus-blue";
            party2.getIdentifiers();
            result = identifiers;
        }};
        //tested method
        partyIdentifierValidator.validateDuplicateIdentifiersInAllParties(party1, allParties);

        new FullVerifications(partyIdentifierValidator) {{
            partyIdentifierValidator.createIssue(identifier.getPartyId(), party1.getName(), anyString);
        }};
    }

    @Test
    public void testCreateIssue(@Injectable Party party, @Injectable Identifier identifier) {
        String partyId = "domibus-blue";
        String name = "blue-gw";
        String message = "duplicate identifier";

        //tested method
        partyIdentifierValidator.createIssue(partyId, name, message);

        new FullVerifications(partyIdentifierValidator) {{
            pModeValidationHelper.createValidationIssue(message, partyId, name);
        }};
    }

}
