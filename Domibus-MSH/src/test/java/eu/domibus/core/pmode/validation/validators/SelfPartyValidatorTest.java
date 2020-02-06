package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.validation.PModeValidationHelper;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelfPartyValidatorTest extends AbstractValidatorTest {

    private SelfPartyValidator validator = new SelfPartyValidator();

    @Test
    public void validate() throws Exception {
        validator.pModeValidationHelper = new PModeValidationHelper();

        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<PModeIssue> results = validator.validate(configuration);
        assertTrue(results.size() == 1);
        assertEquals("Party not found in business process parties", results.get(0).getMessage());
    }
}