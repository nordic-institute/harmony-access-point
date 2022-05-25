package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Binding;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class OneWayMepValidatorTest extends AbstractValidatorTest {

    private OneWayMepValidator validator = new OneWayMepValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<ValidationIssue> results = validator.validate(configuration);
        assertEquals(1, results.size());
    }

    @Test
    public void validateBinding_valid() {
        Binding binding = new Binding();
        binding.setName("push");
        binding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push");
        ValidationIssue issue = validator.validateBinding(binding, "any");
        assertNull(issue);
    }

    @Test
    public void validateBinding_notValid() {
        Binding binding = new Binding();
        binding.setName("pushAndPull");
        binding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull");
        ValidationIssue issue = validator.validateBinding(binding, "any");
        assertTrue(issue != null);
        assertTrue(issue.getMessage().contains("not valid"));
    }

}
