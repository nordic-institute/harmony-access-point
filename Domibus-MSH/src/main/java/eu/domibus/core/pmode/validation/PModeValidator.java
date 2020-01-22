package eu.domibus.core.pmode.validation;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.common.model.configuration.Configuration;

import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Interface for the pMode validators; We provide 2 methods: with serialized or deserialized pMode
 */
public interface PModeValidator {

    /**
     * Validates pMode as serialized xml byte array
     *
     * @param   {array|byte} xml - array or bytes representing the raw pmode
     * @returns {list|PModeIssue} - the list of issues found( errors or warnings, if any)
     */
    List<PModeIssue> validateAsXml(byte[] xml);

    /**
     * Validates pMode as deserialized as Configuration object
     *
     * @param   {Configuration} configuration - Configuration class instance representing the deserialized pmode
     * @returns {list|PModeIssue} - the list of issues found( errors or warnings, if any)
     */
    List<PModeIssue> validateAsConfiguration(Configuration configuration);
}
