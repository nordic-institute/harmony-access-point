package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 */
@RunWith(JMockit.class)
public class BusinessProcessValidatorTest {

    @Tested
    BusinessProcessValidator businessProcessValidator;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Test
    public void validate_test(final @Mocked Configuration configuration, final @Mocked BusinessProcesses businessProcesses) throws NoSuchFieldException, IllegalAccessException {
        final String processName = "testProcess";
        final Process process = new Process();
        final Agreement agreement = new Agreement();
        agreement.setName("testAgreement");
        agreement.setValue("testAgreementValue");
        process.setName(processName);

        Field field = process.getClass().getDeclaredField("agreement");
        field.setAccessible(true);
        field.set(process, agreement);

        InitiatorParties initiatorParties = new InitiatorParties();
        InitiatorParty initiatorParty = new InitiatorParty();
        initiatorParty.setName("testInitiatorParty");
        Field field2 = initiatorParties.getClass().getDeclaredField("initiatorParty");
        field2.setAccessible(true);
        field2.set(initiatorParties, Arrays.asList(initiatorParty));
        process.setInitiatorPartiesXml(initiatorParties);

        Field field3 = process.getClass().getDeclaredField("initiatorParties");
        field3.setAccessible(true);
        field3.set(process, new HashSet<>());
        process.setInitiatorPartiesXml(initiatorParties);

        new Expectations() {{
            configuration.getBusinessProcesses();
            result = businessProcesses;

            businessProcesses.getProcesses();
            result = Arrays.asList(process);

            pModeValidationHelper.createValidationIssue(anyString, anyString, anyString);
            result = new ValidationIssue("Agreement of process [%s] not found in business process", ValidationIssue.Level.ERROR);

        }};

        //tested method
        final List<ValidationIssue> results = businessProcessValidator.validate(configuration);

        new Verifications() {{
            pModeValidationHelper.createValidationIssue("Mep [%s] of process [%s] not found in business process meps.", null, processName);
            pModeValidationHelper.createValidationIssue("Mep binding [%s] of process [%s] not found in business process bindings.", null, processName);
            pModeValidationHelper.createValidationIssue("Initiator role [%s] of process [%s] not found in business process roles.", null, processName);
            pModeValidationHelper.createValidationIssue("Responder role [%s] of process [%s] not found in business process roles.", null, processName);
            pModeValidationHelper.createValidationIssue("Initiator party [%s] of process [%s] not found in business process parties", "testInitiatorParty", processName);
        }};

        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() == 5);
    }

    @Test
    public void testValidateInitiatorPartyIdType(@Injectable ValidationIssue issue, @Injectable PartyIdType partyIdType,
                                                 @Injectable Process process, @Injectable Party party,
                                                 @Injectable Identifier identifier) {

        Set<PartyIdType> partyIdTypes = new HashSet<>();
        Set<Identifier> identifiers = new HashSet<>();
        Identifier identifier1 = new Identifier();
        PartyIdType partyIdType1 = new PartyIdType();
        partyIdType1.setName("partyIdTypeUrn1");
        PartyIdType partyIdType2 = new PartyIdType();
        partyIdType1.setName("partyIdTypeUrn");
        partyIdTypes.add(partyIdType2);
        identifier1.setPartyIdType(partyIdType1);
        identifiers.add(identifier1);
        Set<Party> validInitiatorParties = new HashSet<>();
        Party party1 = new Party();
        party1.setIdentifiers(identifiers);
        validInitiatorParties.add(party1);

        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(issue);

        //tested method
        businessProcessValidator.validateInitiatorPartyIdType(issues, process, partyIdTypes, validInitiatorParties);


        new Verifications() {{
            businessProcessValidator.createIssue(issues, process, party1.getName(), "Initiator Party's [%s] partyIdType of process [%s] not found in business process partyId types");
            times = 1;
        }};

    }

    @Test
    public void testValidateResponderPartyIdType(@Injectable ValidationIssue issue, @Injectable PartyIdType partyIdType,
                                                 @Injectable Process process, @Injectable Party party,
                                                 @Injectable Identifier identifier) {

        Set<PartyIdType> partyIdTypes = new HashSet<>();
        Set<Identifier> identifiers = new HashSet<>();
        Identifier identifier1 = new Identifier();
        PartyIdType partyIdType1 = new PartyIdType();
        partyIdType1.setName("partyIdTypeUrn1");
        PartyIdType partyIdType2 = new PartyIdType();
        partyIdType1.setName("partyIdTypeUrn");
        partyIdTypes.add(partyIdType2);
        identifier1.setPartyIdType(partyIdType1);
        identifiers.add(identifier1);
        Set<Party> validResponderParties = new HashSet<>();
        Party party1 = new Party();
        party1.setIdentifiers(identifiers);
        validResponderParties.add(party1);

        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(issue);

        //tested method
        businessProcessValidator.validateResponderPartyIdType(issues, process, partyIdTypes, validResponderParties);


        new Verifications() {{
            businessProcessValidator.createIssue(issues, process, party1.getName(), "Responder Party's [%s] partyIdType of process [%s] not found in business process partyId types");
            times = 1;
        }};

    }

}