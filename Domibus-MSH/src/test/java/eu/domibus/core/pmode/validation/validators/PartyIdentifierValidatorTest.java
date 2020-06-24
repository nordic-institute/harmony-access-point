package eu.domibus.core.pmode.validation.validators;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.PartyIdType;
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
    public void testValidateDuplicatePartyIdentifiers(@Injectable Party party,
                                                      @Injectable Identifier identifier
    ) {


        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier1 = new Identifier();
        Identifier identifier2 = new Identifier();
        PartyIdType partyIdType1 = new PartyIdType();
        String message = "Duplicate party identifier [%s] found for the party [%s]";
        partyIdType1.setName("partyIdTypeUrn");

        identifier1.setPartyId("domibus-blue");
        identifier1.setPartyIdType(partyIdType1);
        identifiers.add(identifier1);

        identifier2.setPartyId("domibus-blue");
        identifier2.setPartyIdType(partyIdType1);
        identifiers.add(identifier2);

        Party party1 = new Party();
        party1.setName("blue_gw");
        party1.setIdentifiers(identifiers);

        //tested method
        partyIdentifierValidator.validateDuplicatePartyIdentifiers(party1);

        new Verifications() {{
            partyIdentifierValidator.createIssue(identifier1.getPartyId(), party1.getName(), message);
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
    public void testvalidateDuplicateIdentifiersInAllParties(@Injectable Party party, @Injectable Identifier identifier) {

        List<Identifier> identifiers = new ArrayList<>();
        List<Identifier> identifiers1 = new ArrayList<>();
        List<Party> allParties = new ArrayList<>();
        Identifier identifier1 = new Identifier();
        Identifier identifier2 = new Identifier();
        PartyIdType partyIdType1 = new PartyIdType();
        String message = "Duplicate party identifier [%s] found in party [%s] and in party [red_gw]";
        partyIdType1.setName("partyIdTypeUrn");

        identifier1.setPartyId("domibus-blue");
        identifier1.setPartyIdType(partyIdType1);
        identifiers.add(identifier1);

        identifier2.setPartyId("domibus-blue");
        identifier2.setPartyIdType(partyIdType1);
        identifiers1.add(identifier2);

        Party party1 = new Party();
        Party party2 = new Party();
        party1.setName("blue_gw");
        party1.setIdentifiers(identifiers);
        party2.setName("red_gw");
        party2.setIdentifiers(identifiers1);
        allParties.add(party1);
        allParties.add(party2);

        //tested method
        partyIdentifierValidator.validateDuplicateIdentifiersInAllParties(party1, allParties);

        new FullVerifications(partyIdentifierValidator) {{
            partyIdentifierValidator.createIssue(identifier1.getPartyId(), party1.getName(), message);
        }};
    }

}
