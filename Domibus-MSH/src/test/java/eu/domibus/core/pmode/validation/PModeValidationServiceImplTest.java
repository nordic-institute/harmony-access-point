package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.validators.LegConfigurationValidator;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JMockit.class)
public class PModeValidationServiceImplTest {

    @Tested
    PModeValidationServiceImpl pModeValidationService;

    @Injectable
    List<PModeValidator> pModeValidatorList = new ArrayList<PModeValidator>();

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    LegConfigurationValidator legConfigurationValidator;

    @Before
    public void init() {
        pModeValidatorList.add(legConfigurationValidator);
    }

    @Test
    public void validate_Disabled(@Mocked Configuration configuration) {

        List<ValidationIssue> issues = pModeValidationService.validate(configuration);

        new Verifications() {{
            legConfigurationValidator.validate(configuration);
            times = 1;
        }};

        Assert.assertTrue(issues.size() == 0);
    }

    @Test(expected = PModeValidationException.class)
    public void validate_Error(@Mocked Configuration configuration) {

        ValidationIssue issue = new ValidationIssue();
        issue.setLevel(ValidationIssue.Level.ERROR);
        issue.setMessage("Leg configuration is wrong");

        new Expectations() {{
            configuration.preparePersist();

            legConfigurationValidator.validate(configuration);
            result = Arrays.asList(issue);
        }};

        List<ValidationIssue> issues = pModeValidationService.validate(configuration);

        new Verifications() {{
            legConfigurationValidator.validate(configuration);
            times = 1;
        }};

        Assert.assertTrue(issues.size() == 1);
        Assert.assertTrue(issues.get(0).getLevel() == ValidationIssue.Level.ERROR);
    }


}