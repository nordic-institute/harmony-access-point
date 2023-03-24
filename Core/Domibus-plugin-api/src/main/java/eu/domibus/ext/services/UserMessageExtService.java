package eu.domibus.ext.services;

import eu.domibus.common.MSHRole;
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
     * @deprecated since 5.0 Use instead {@link #getMessage(String messageId, MSHRole role)}
     */
    @Deprecated
    UserMessageDTO getMessage(String messageId) throws MessageNotFoundException;

    /**
     * Gets a user message
     *
     * @param messageId The message id of the message to be retrieved
     * @return The user message {@link UserMessageDTO}
     * @throws UserMessageExtException Raised in case an exception occurs while trying to get the user message {@link UserMessageExtException}
     */
    UserMessageDTO getMessage(String messageId, MSHRole mshRole) throws MessageNotFoundException;

    /**
     * @param messageId
     * @return
     * @deprecated since 5.0 Use instead {@link #getUserMessageEnvelope(String messageId, MSHRole role)}
     */
    @Deprecated
    String getUserMessageEnvelope(String messageId);

    String getUserMessageEnvelope(String messageId, MSHRole mshRole);

    /**
     * @param messageId
     * @return
     * @deprecated since 5.0 Use instead {@link #getSignalMessageEnvelope(String messageId, MSHRole role)}
     */
    @Deprecated
    String getSignalMessageEnvelope(String messageId);

    String getSignalMessageEnvelope(String messageId, MSHRole mshRole);

    /**
     * Gets the final recipient from the properties of the domibus message
     *
     * @param messageId if the domibus message
     * @return {@code finalRecipient} or {@code null} if message not found
     * @deprecated since 5.0 Use instead {@link #getFinalRecipient(String messageId, MSHRole role)}
     */
    @Deprecated
    String getFinalRecipient(String messageId);

    String getFinalRecipient(String messageId, MSHRole mshRole);

    /**
     * Gets the original sender from the properties of the domibus message
     *
     * @param messageId if the domibus message
     * @return {@code originalSender} or {@code null} if message not found
     * @deprecated since 5.0 Use instead {@link #getOriginalSender(String messageId, MSHRole role)}
     */
    @Deprecated
    String getOriginalSender(String messageId);

    String getOriginalSender(String messageId, MSHRole role);

    /**
     * Validates the UserMessage before Domibus saves the message into the database.
     *
     * @param userMessage The UserMessage to be validated
     * @throws UserMessageExtException in case the validation does not pass
     */
    void validateUserMessage(UserMessageDTO userMessage) throws UserMessageExtException;
}
