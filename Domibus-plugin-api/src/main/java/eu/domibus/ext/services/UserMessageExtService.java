package eu.domibus.ext.services;

import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageExtException;
import eu.domibus.messaging.MessageNotFoundException;

/**
 * Responsible for operations related with user messages.
 *
 * @author Tiago Miguel
 * @since 4.0
 */
public interface UserMessageExtService {

    /**
     * Gets a user message
     *
     * @param messageId The message id of the message to be retrieved
     * @return The user message {@link UserMessageDTO}
     * @throws UserMessageExtException Raised in case an exception occurs while trying to get the user message {@link UserMessageExtException}
     */
    UserMessageDTO getMessage(String messageId) throws MessageNotFoundException;

    String getUserMessageEnvelope(String messageId);

    String getSignalMessageEnvelope(String messageId);

    /**
     * Gets the final recipient from the properties of the domibus message
     * @param messageId if the domibus message
     * @return {@code finalRecipient} or {@code null} if message not found
     */
    String getFinalRecipient(String messageId);

    /**
     * Gets the original sender from the properties of the domibus message
     * @param messageId if the domibus message
     * @return {@code originalSender} or {@code null} if message not found
     */
    String getOriginalSender(String messageId);

    /**
     * Validates the UserMessage before Domibus saves the message into the database.
     *
     * @param userMessage The UserMessage to be validated
     * @throws UserMessageExtException in case the validation does not pass
     */
    void validateUserMessage(UserMessageDTO userMessage) throws UserMessageExtException;
}
