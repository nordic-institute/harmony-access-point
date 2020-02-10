package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.BusinessProcesses;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Service;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1.2
 */
@RunWith(JMockit.class)
public class LegConfigurationValidatorTest {

    @Tested
    LegConfigurationValidator legValidator;

    @Injectable
    PModeValidationHelper pModeValidationHelper;

    @Test
    public void test_validate(final @Mocked Configuration configuration, final @Mocked BusinessProcesses businessProcesses) {

        final String legConfigurationName = "testConfiguration";
        final LegConfiguration legConfiguration = new LegConfiguration();
        final Service service = new Service();
        service.setName("testService");
        service.setValue("testServiceValue");
        legConfiguration.setName(legConfigurationName);
        legConfiguration.setService(service);

        new Expectations() {{
            configuration.getBusinessProcesses();
            result = businessProcesses;

            businessProcesses.getLegConfigurations();
            result = Collections.singleton(legConfiguration);

            pModeValidationHelper.createValidationIssue(anyString, anyString, anyString);
            result = new ValidationIssue("of leg configuration [" + legConfigurationName + "] not found in business process", ValidationIssue.Level.ERROR);
        }};

        //tested method
        final List<ValidationIssue> results = legValidator.validate(configuration);

        new Verifications() {{
            pModeValidationHelper.createValidationIssue("Action [%s] of leg configuration [%s] not found in business process actions.", null,  legConfigurationName);
            pModeValidationHelper.createValidationIssue("Security [%s] of leg configuration [%s] not found in business process securities.", null,  legConfigurationName);
            pModeValidationHelper.createValidationIssue("DefaultMpc [%s] of leg configuration [%s] not found in business process mpc.", null,  legConfigurationName);
            pModeValidationHelper.createValidationIssue("ReceptionAwareness [%s] of leg configuration [%s] not found in business process as4 awarness.", null,  legConfigurationName);
            pModeValidationHelper.createValidationIssue("Reliability [%s] of leg configuration [%s] not found in business process as4 reliability.", null,  legConfigurationName);
            pModeValidationHelper.createValidationIssue("ErrorHandling [%s] of leg configuration [%s] not found in business process error handlings.", null,  legConfigurationName);
        }};

        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() == 6);
    }

}