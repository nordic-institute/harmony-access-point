package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 */
public class EndpointValidatorTest extends AbstractValidatorTest {

    private EndpointValidator validator = new EndpointValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<ValidationIssue> issues = validator.validate(configuration);
        assertTrue(issues.size() == 2);
        assertThat(issues.get(0).getMessage(), is("Party [party2] should not have an empty endpoint."));
        assertTrue(issues.get(1).getMessage().contains("Forbidden characters '< >' found in the endpoint"));
    }

}