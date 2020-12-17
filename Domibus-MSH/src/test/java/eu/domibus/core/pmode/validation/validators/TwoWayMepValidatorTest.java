package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Binding;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class TwoWayMepValidatorTest extends AbstractValidatorTest {

    private TwoWayMepValidator validator = new TwoWayMepValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("TestConfiguration.json");
        final List<ValidationIssue> results = validator.validate(configuration);
        assertEquals(1, results.size());
        assertEquals(
                "Two-way mep with binding [pushAndPush] is not supported for process [TestProcess_notSupported]. In the pMode XML it is required to use 2 one-way processes to simulate two-way communication.",
                results.get(0).getMessage());
    }

    @Test
    public void validateBinding_notSupported() {
        Binding binding = new Binding();
        binding.setName("pushAndPull");
        binding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPull");
        ValidationIssue issue = validator.validateBinding(binding, "any");
        assertTrue(issue != null);
        assertTrue(issue.getMessage().contains("not supported"));
    }

    @Test
    public void validateBinding_notValid() {
        Binding binding = new Binding();
        binding.setName("push");
        binding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push");
        ValidationIssue issue = validator.validateBinding(binding, "any");
        assertTrue(issue != null);
        assertTrue(issue.getMessage().contains("invalid"));
    }
}
