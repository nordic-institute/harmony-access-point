package eu.domibus.core.spi.validation;

import eu.domibus.ext.domain.UserMessageDTO;

import java.io.InputStream;

/**
 * @author Cosmin Baciu
 * @since 5.0
 *
 * SPI interface should be extended to implement custom validation of the incoming/outgoing UserMessages. The validation is applied to all UserMessages, incoming and outgoing, regardless of any plugin implementation.
 * This SPI interface can be used to implement antivirus validation.
 */
public interface UserMessageValidatorSpi {

    /**
     * Validates the UserMessage before Domibus saves the message into the database.
     *
     * @param userMessage The UserMessage to be validated
     * @throws UserMessageValidatorSpiException in case the validation does not pass
     */
    void validateUserMessage(UserMessageDTO userMessage) throws UserMessageValidatorSpiException;

    /**
     * Validates the UserMessage payload on demand eg from a custom plugin. This validation can be implemented to scan the payload using an antivirus solution.
     *
     * @param payload The payload to be validated
     * @param mimeType The payload mime type
     * @throws UserMessageValidatorSpiException in case the validation does not pass
     */
    void validatePayload(InputStream payload, String mimeType) throws UserMessageValidatorSpiException;
}
