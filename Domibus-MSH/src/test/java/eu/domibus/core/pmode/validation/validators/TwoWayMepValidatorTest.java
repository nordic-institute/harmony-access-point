package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
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
        assertTrue(results.size() == 1);
        assertEquals(
                "Two-Way mep with binding http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pushAndPush not accepted for process TestProcess",
                results.get(0).getMessage());
    }
}