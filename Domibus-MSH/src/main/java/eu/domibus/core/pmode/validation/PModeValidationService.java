package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Interface for pMode validation: Calls al validators, aggregates the results and raises an exception if at least one exception
 */
public interface PModeValidationService {
    /**
     * Validates pMode as serialized xml byte array or/and Configuration object;
     *
     * @param configuration - Configuration class instance representing the deserialized pmode
     * @return list of validation issues in case there are no error among them
     * @throws PModeValidationException an exception that contains the list of issues found( errors or warnings)
     */
    List<ValidationIssue> validate(Configuration configuration) throws PModeValidationException;
}
