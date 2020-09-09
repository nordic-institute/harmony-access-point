package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Ion Perpegel, Soumya Chandran
 * @since 4.2
 * <p>
 */
public class EndpointValidatorTest extends AbstractValidatorTest {

    @Tested
    EndpointValidator validator;

    @Injectable
    BusinessProcessValidator businessProcessValidator;

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<ValidationIssue> issues = validator.validate(configuration);
        assertTrue(issues.size() == 1);
        assertThat(issues.get(0).getMessage(), is("Party [party2] should not have an empty endpoint."));
        new Verifications() {{
            businessProcessValidator.validateForbiddenCharacters(issues, anyString, anyString);
            times = 2;
        }};
    }

}