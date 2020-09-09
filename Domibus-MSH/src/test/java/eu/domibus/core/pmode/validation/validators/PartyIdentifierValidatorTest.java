package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * @author Soumya Chandran
 * @since 4.2
 */
public class PartyIdentifierValidatorTest {

    @Tested
    PartyIdentifierValidator partyIdentifierValidator;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Injectable
    BusinessProcessValidator businessProcessValidator;

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
            partyIdentifierValidator.validateForbiddenCharactersInParty((Party) any);
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
    public void testValidateDuplicateIdentifiersInAllParties(@Injectable Party party1,
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
    public void testGetDuplicateIdentifiers(@Injectable Party party1,
                                            @Injectable Party party2,
                                            @Injectable Identifier identifier) {


        List<Party> allParties = new ArrayList<>();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);
        Set<Identifier> identifierSet = new HashSet<>(identifiers);
        allParties.add(party1);
        allParties.add(party2);

        List<Identifier> identifierList = partyIdentifierValidator.getDuplicateIdentifiers(identifierSet, party1);

        new FullVerifications(partyIdentifierValidator) {{
            Assert.assertNotNull(identifierList);
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

    @Test
    public void validateForbiddenCharactersInParty(@Injectable Party party, @Injectable Identifier identifier) {
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        new Expectations(partyIdentifierValidator) {{
            party.getName();
            result = "party3<img src=http://placekitten.com/222/333>";
            identifier.getPartyId();
            result = "domibus-blue<img src=http://placekitten.com/333/333>";
            party.getIdentifiers();
            result = identifiers;
        }};
        List<ValidationIssue> issues = partyIdentifierValidator.validateForbiddenCharactersInParty(party);
        assertTrue(issues.size() == 1);
        assertTrue(issues.get(0).getMessage().contains("Forbidden characters '< >' found in the party identifier's partyId"));
        new Verifications() {{
            businessProcessValidator.validateForbiddenCharacters(anyString, anyString);
            times = 1;
        }};
    }
}
