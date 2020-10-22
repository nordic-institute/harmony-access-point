package eu.domibus.ext.services;

import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.ext.exceptions.UserMessageExtException;

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
    UserMessageDTO getMessage(String messageId) throws UserMessageExtException;

    String getUserMessageEnvelope(String messageId);

    String getSignalMessageEnvelope(String messageId);
}
