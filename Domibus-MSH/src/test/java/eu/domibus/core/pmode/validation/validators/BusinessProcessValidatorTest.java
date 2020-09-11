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

import static org.junit.Assert.assertTrue;

/**
 * @author Ion Perpegel
 * @author Catalin Enache
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
    public void validate_test(final @Injectable Configuration configuration, final @Injectable BusinessProcesses businessProcesses) throws NoSuchFieldException, IllegalAccessException {
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
        List<Identifier> identifiers = new ArrayList<>();
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
        List<Identifier> identifiers = new ArrayList<>();
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

    @Test
    public void test_validateResponderParties(final @Injectable ValidationIssue validationIssue,
                                              final @Injectable Process process,
                                              final @Injectable PartyIdType partyIdType,
                                              final @Injectable Party validResponderParty,
                                              final @Injectable ResponderParties responderPartiesXml,
                                              final @Injectable ResponderParty responderParty
    ) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        Set<PartyIdType> partyIdTypes = new HashSet<>();
        partyIdTypes.add(partyIdType);
        List<ResponderParty> allResponderParties = new ArrayList<>();
        allResponderParties.add(responderParty);
        Set<Party> validResponderParties = new HashSet<>();
        validResponderParties.add(validResponderParty);

        new Expectations(businessProcessValidator) {{
            process.getResponderParties();
            result = validResponderParties;

            process.getResponderPartiesXml();
            result = responderPartiesXml;;

            responderPartiesXml.getResponderParty();
            result = allResponderParties;


            responderParty.getName();
            result = "RED_GW";

            validResponderParty.getName();
            result = "red_gw";

        }};

        //tested method
        businessProcessValidator.validateResponderParties(issues, process, partyIdTypes);

        new FullVerifications(businessProcessValidator) {{
            businessProcessValidator.validateResponderPartyIdType(issues, process, partyIdTypes, validResponderParties);
        }};
    }

    @Test
    public void test_checkPartyIdentifiers(final @Injectable ValidationIssue validationIssue,
                                           final @Injectable Process process,
                                           final @Injectable PartyIdType partyIdType,
                                           final @Injectable Party party,
                                           final @Injectable Identifier identifier) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        Set<PartyIdType> partyIdTypes = new HashSet<>();
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);
        final String message = "test message";
        final String partyName = "test party";

        new Expectations(businessProcessValidator) {{
            party.getIdentifiers();
            result = identifiers;

            party.getName();
            result = partyName;

            identifier.getPartyIdType();
            result = partyIdType;
        }};

        //tested method
        businessProcessValidator.checkPartyIdentifiers(issues, process, partyIdTypes, party, message);

        new FullVerifications(businessProcessValidator) {{
            businessProcessValidator.createIssue(issues, process, anyString , anyString);
        }};
    }

    @Test
    public void test_validateAgreement(final @Injectable List<ValidationIssue> validationIssues,
                                       final @Injectable Process process) {
        new Expectations(businessProcessValidator) {{
            process.getAgreement();
            result = null;

            pModeValidationHelper.getAttributeValue(process, "agreementXml", String.class);
            result = "agreement test";
        }};

        //tested method
        businessProcessValidator.validateAgreement(validationIssues, process);

        new FullVerifications(businessProcessValidator) {{
           businessProcessValidator.createIssue(validationIssues, process, anyString, "Agreement [%s] of process [%s] not found in business process agreements.");
        }};
    }

    @Test
    public void test_validateLegConfiguration(final @Injectable ValidationIssue validationIssue,
                                              final @Injectable Process process,
                                              final @Injectable Legs legs,
                                              final @Injectable Leg leg) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);

        Set<LegConfiguration> legConfigurations=new HashSet<>();

        new Expectations(businessProcessValidator) {{
            process.getLegs();
            result = legConfigurations;

            pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
            result = legs;

            legs.getLeg();
            result = Collections.singletonList(leg);
        }};

        //tested method
        businessProcessValidator.validateLegConfiguration(issues, process);

        new FullVerifications(businessProcessValidator) {{
        }};
    }

    @Test
    public void test_validateEmptyLegs(final @Injectable ValidationIssue validationIssue,
                                       final @Injectable Process process,
                                       final @Injectable Set<LegConfiguration> legConfigurations,
                                       final @Injectable Legs legs) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);

        List<Leg> legList = new ArrayList<>();

        new Expectations(businessProcessValidator) {{
            process.getLegs();
            result = legConfigurations;

            pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
            result = legs;

            legs.getLeg();
            result = legList;
        }};

        //tested method
        businessProcessValidator.validateLegConfiguration(issues, process);

        new FullVerifications(businessProcessValidator) {{
        }};
    }

    @Test
    public void test_validateLegConfigurationWithError(final @Injectable ValidationIssue validationIssue,
                                                       final @Injectable Process process,
                                                       final @Injectable LegConfiguration legConfiguration,
                                                       final @Injectable Legs legs,
                                                       final @Injectable Leg leg,
                                                       final @Injectable Leg leg1) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        List<Leg> allLegs = new ArrayList<>();
        allLegs.add(leg);
        allLegs.add(leg1);
        Set<LegConfiguration> validLegs = new HashSet<>();
        validLegs.add(legConfiguration);

        new Expectations(businessProcessValidator) {{
            process.getLegs();
            result = validLegs;

            pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
            result = legs;

            legs.getLeg();
            result = allLegs;

            leg.getName();
            result = "test1";

            legConfiguration.getName();
            result = "test2";
        }};

        //tested method
        businessProcessValidator.validateLegConfiguration(issues, process);

        new FullVerifications(businessProcessValidator) {{
            businessProcessValidator.createIssue(issues, process, anyString, "Leg [%s] of process [%s] not found in business process leg configurations");
        }};
    }

    @Test
    public void test_validateLegConfigurationCaseInsensitive(final @Injectable ValidationIssue validationIssue,
                                                       final @Injectable Process process,
                                                       final @Injectable LegConfiguration legConfiguration,
                                                       final @Injectable Legs legs,
                                                       final @Injectable Leg leg) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        List<Leg> allLegs = new ArrayList<>();
        allLegs.add(leg);
        Set<LegConfiguration> validLegs = new HashSet<>();
        validLegs.add(legConfiguration);

        new Expectations(businessProcessValidator) {{
            process.getLegs();
            result = validLegs;

            pModeValidationHelper.getAttributeValue(process, "legsXml", Legs.class);
            result = legs;

            legs.getLeg();
            result = allLegs;

            leg.getName();
            result = "testServiceCase";

            legConfiguration.getName();
            result = "TESTSERVICECASE";
        }};

        //tested method
        businessProcessValidator.validateLegConfiguration(issues, process);

        new FullVerifications(businessProcessValidator) {{
        }};
    }

    @Test
    public void test_validateResponderPartiesCaseInsensitive(final @Injectable ValidationIssue validationIssue,
                                                             final @Injectable Process process,
                                                             final @Injectable Set<PartyIdType> partyIdTypes,
                                                             final @Injectable Party validResponderParty,
                                                             final @Injectable ResponderParties responderPartiesXml,
                                                             final @Injectable ResponderParty responderParty
                                                             ) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        List<ResponderParty> allResponderParties = new ArrayList<>();
        allResponderParties.add(responderParty);
        Set<Party> responderParties = new HashSet<>();
        responderParties.add(validResponderParty);

        new Expectations(businessProcessValidator) {{
            process.getResponderParties();
            result = responderParties;

            process.getResponderPartiesXml();
            result = responderPartiesXml;

            responderPartiesXml.getResponderParty();
            result = allResponderParties;

            responderParty.getName();
            result = "RED_GW";

            validResponderParty.getName();
            result = "red_gw";
        }};

        //tested method
        businessProcessValidator.validateResponderParties(issues, process, partyIdTypes);

        new FullVerifications(businessProcessValidator) {{
        }};
    }

    @Test
    public void test_validateInitiatorPartiesCaseInsensitive(final @Injectable ValidationIssue validationIssue,
                                                             final @Injectable Process process,
                                                             final @Injectable Set<PartyIdType> partyIdTypes,
                                                             final @Injectable Party validInitiatorParty,
                                                             final @Injectable InitiatorParties initiatorPartiesXml,
                                                             final @Injectable InitiatorParty InitiatorParty) {
        List<ValidationIssue> issues = new ArrayList<>();
        issues.add(validationIssue);
        List<InitiatorParty> allInitiatorParties= new ArrayList<>();
        allInitiatorParties.add(InitiatorParty);
        Set<Party> initiatorParties = new HashSet<>();
        initiatorParties.add(validInitiatorParty);

        new Expectations(businessProcessValidator) {{
            process.getInitiatorParties();
            result = initiatorParties;

            process.getInitiatorPartiesXml();
            result = initiatorPartiesXml;

            initiatorPartiesXml.getInitiatorParty();
            result = allInitiatorParties;

            InitiatorParty.getName();
            result = "BLUE_GW";

            validInitiatorParty.getName();
            result = "blue_gw";
        }};

        //tested method
        businessProcessValidator.validateInitiatorParties(issues, process, partyIdTypes);

        new FullVerifications(businessProcessValidator) {{
        }};
    }

    @Test
    public void validateForbiddenCharacters(@Injectable ValidationIssue issue) {
        List<ValidationIssue> issues = new ArrayList<>();
        final String processName = "tcxProcess<img src=http://placekitten.com/155/155>";
        businessProcessValidator.validateForbiddenCharacters(issues, processName, "process name[" + processName + "]");
        assertTrue(issues.size() == 1);
        assertTrue(issues.get(0).getMessage().contains("Forbidden characters '< >' found in the process name"));
    }
}