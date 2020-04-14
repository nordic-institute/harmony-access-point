package eu.domibus.core.message.payload;

import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @since 4.2
 * @author Catalin Enache
 */
public interface ClearPayloadMessageService {

    /**
     * clears the payload for a given user message
     *
     * @param messageId
     */
    void clearPayloadData(final String messageId);

    /**
     * send the UserMessage to JMS clearPayloadQueue
     *
     * @param userMessage
     */
    void enqueueMessageForClearPayload(UserMessage userMessage);
}
