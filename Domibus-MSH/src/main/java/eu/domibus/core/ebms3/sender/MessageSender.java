package eu.domibus.core.ebms3.sender;

import eu.domibus.core.message.UserMessageLog;
import eu.domibus.ebms3.common.model.Messaging;

/**
 * Defines the contract for sending AS4 messages depending on the message type: UserMessage, MessageFragment or SourceMessage
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface MessageSender {

    void sendMessage(final Messaging messaging, final UserMessageLog userMessageLog);
}
