package eu.domibus.core.ebms3.sender;

import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;

/**
 * Defines the contract for sending AS4 messages depending on the message type: UserMessage, MessageFragment or SourceMessage
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageSender {

    void sendMessage(final UserMessage userMessage, final UserMessageLog userMessageLog);
}
