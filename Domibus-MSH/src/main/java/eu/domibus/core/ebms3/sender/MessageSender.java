package eu.domibus.core.ebms3.sender;

import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.model.Messaging;

/**
 * Defines the contract for sending AS4 messages depending on the message type: UserMessage, MessageFragment or SourceMessage
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageSender {

    void sendMessage(final Messaging messaging, final UserMessageLog userMessageLog);
}
