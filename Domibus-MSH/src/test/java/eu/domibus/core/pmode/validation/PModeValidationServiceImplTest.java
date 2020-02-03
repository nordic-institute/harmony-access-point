package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.Configuration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_LEVEL;
import static eu.domibus.api.property.DomibusPropertyMetadataManager.DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS;

@RunWith(JMockit.class)
public class PModeValidationServiceImplTest {

    @Tested
    PModeValidationServiceImpl pModeValidationService;

    @Injectable
    List<PModeValidator> pModeValidatorList = new ArrayList<PModeValidator>();

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    CompositePModeValidator compositePModeValidator;

    @Before
    public void init() {
        pModeValidatorList.add(compositePModeValidator);
    }

    @Test
    public void validate_Disabled(@Mocked byte[] rawConfiguration, @Mocked Configuration configuration) {

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS);
            result = true;

            configuration.preparePersist();

            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_LEVEL + "." + "CompositePModeValidator");
            result = "NONE";
        }};

        List<PModeIssue> issues = pModeValidationService.validate(rawConfiguration, configuration);

        new Verifications() {{
            compositePModeValidator.validateAsConfiguration(configuration);
            times = 0;
//            compositePModeValidator.validateAsXml(rawConfiguration);
//            times = 0;
        }};

        Assert.assertTrue(issues.size() == 0);
    }

    @Test
    public void validate_SetAsWarning(@Mocked byte[] rawConfiguration, @Mocked Configuration configuration) {

        PModeIssue issue = new PModeIssue();
        issue.setLevel(PModeIssue.IssueLevel.ERROR);
        issue.setMessage("Leg configuration is wrong");

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS);
            result = false;

            configuration.preparePersist();

            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_LEVEL + "." + "CompositePModeValidator");
            result = "WARNING";

//            compositePModeValidator.validateAsXml(rawConfiguration);
//            result = Arrays.asList(issue);
        }};

        List<PModeIssue> issues = pModeValidationService.validate(rawConfiguration, configuration);

        new Verifications() {{
            compositePModeValidator.validateAsConfiguration(configuration);
            times = 1;
//            compositePModeValidator.validateAsXml(rawConfiguration);
//            times = 1;
        }};

        Assert.assertTrue(issues.size() == 1);
        Assert.assertTrue(issues.get(0).getLevel() == PModeIssue.IssueLevel.WARNING);
    }

    @Test
    public void validate_SetAsError(@Mocked byte[] rawConfiguration, @Mocked Configuration configuration) {

        PModeIssue issue = new PModeIssue();
        issue.setLevel(PModeIssue.IssueLevel.ERROR);
        issue.setMessage("Leg configuration is wrong");

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_P_MODE_VALIDATION_WARNINGS_AS_ERRORS);
            result = true;

            configuration.preparePersist();

            domibusPropertyProvider.getProperty(DOMIBUS_P_MODE_VALIDATION_LEVEL + "." + "CompositePModeValidator");
            result = "WARNING";

//            compositePModeValidator.validateAsXml(rawConfiguration);
//            result = Arrays.asList(issue);
        }};

        List<PModeIssue> issues = pModeValidationService.validate(rawConfiguration, configuration);

        new Verifications() {{
            compositePModeValidator.validateAsConfiguration(configuration);
            times = 1;
//            compositePModeValidator.validateAsXml(rawConfiguration);
            times = 1;
        }};

        Assert.assertTrue(issues.size() == 1);
        Assert.assertTrue(issues.get(0).getLevel() == PModeIssue.IssueLevel.ERROR);
    }

}