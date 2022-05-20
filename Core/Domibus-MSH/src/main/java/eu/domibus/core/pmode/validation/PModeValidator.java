package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Interface for the pMode validators
 */
public interface PModeValidator {
    /**
     * Validates pMode as deserialize as Configuration object
     *
     * @param {pMode} configuration - Configuration class instance representing the deserialized pmode
     * @returns {list|PModeIssue} - the list of issues found( errors or warnings, if any)
     */
    List<ValidationIssue> validate(Configuration pMode);
}
