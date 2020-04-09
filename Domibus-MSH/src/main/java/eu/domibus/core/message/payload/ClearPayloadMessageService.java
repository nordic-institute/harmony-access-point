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

    void enqueueMessageForClearPayload(UserMessage userMessage);
}
