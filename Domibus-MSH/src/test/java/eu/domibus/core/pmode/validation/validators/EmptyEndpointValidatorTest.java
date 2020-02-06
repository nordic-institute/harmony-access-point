package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmptyEndpointValidatorTest extends AbstractValidatorTest {

    private EmptyEndpointValidator validator = new EmptyEndpointValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<PModeIssue> results = validator.validate(configuration);
        assertTrue(results.size() == 1);
        assertEquals("Party [party2] should not have an empty endpoint.", results.get(0).getMessage());
    }
}