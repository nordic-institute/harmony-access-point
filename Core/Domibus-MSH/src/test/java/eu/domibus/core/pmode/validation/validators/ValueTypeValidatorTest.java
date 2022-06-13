package eu.domibus.core.pmode.validation.validators;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author idragusa
 * @since 3.3
 */
public class ValueTypeValidatorTest extends AbstractValidatorTest {

    private ValueTypeValidator validator = new ValueTypeValidator();

    @Test
    public void validate() throws Exception {
        Configuration configuration = newConfiguration("ValueTypeConfiguration.json");
        final List<ValidationIssue> results = validator.validate(configuration);
        assertTrue(results.size() == 4);
        assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("PartyIdType is empty and the partyId is not an URI for red_gw")));
        assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Service type is empty and the service value is not an URI for testService1")));
        assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Agreement type is empty and the agreement value is not an URI for agreement2")));
        assertTrue(results.stream().anyMatch(el -> el.getMessage().equals("Agreement type is empty and the agreement value is not an URI for agreement1")));
    }
}