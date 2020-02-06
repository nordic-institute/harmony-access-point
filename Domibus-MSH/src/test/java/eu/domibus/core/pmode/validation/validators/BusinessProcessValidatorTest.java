package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
            result = new PModeIssue("Agreement of process [%s] not found in business process", PModeIssue.Level.ERROR);

        }};

        //tested method
        final List<PModeIssue> results = businessProcessValidator.validate(configuration);

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
}