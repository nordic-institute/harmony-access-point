package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import mockit.Expectations;
import mockit.Injectable;
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
        assertTrue(issues.size() == 1);
        assertThat(issues.get(0).getMessage(), is("Party [party2] should not have an empty endpoint."));
    }

    @Test
    public void validateForbiddenCharacters(@Injectable Party party) {
        new Expectations() {{
            party.getEndpoint();
            result = "http://localhost:8180/domibus/services/msh3?domain=aqua&amp;&lt;img src=http://placekitten.com/271/300&gt;>";
        }};
        final List<ValidationIssue> issues = validator.validateForbiddenCharacters(party);
        assertTrue(issues.get(0).getMessage().contains("Forbidden characters '< >' found in the endpoint"));
    }
}