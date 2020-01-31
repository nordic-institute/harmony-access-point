package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Interface for pMode validation: Calls al validators and aggregates  the results
 */
public interface PModeValidationService {
    /**
     * Validates pMode as serialized xml byte array or/and Configuration object;
     *
     * @param   rawConfiguration - array or bytes representing the raw pmode
     * @param   configuration - Configuration class instance representing the deserialized pmode
     * @returns the list of issues found( errors or warnings, if any)
     */
    List<PModeIssue> validate(byte[] rawConfiguration, Configuration configuration);
}
