package eu.domibus.core.message;

import eu.domibus.api.model.UserMessage;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageServiceHelper {

    String getOriginalSender(UserMessage userMessage);

    String getFinalRecipient(UserMessage userMessage);

    String getPartyTo(UserMessage userMessage);

    String getPartyFrom(UserMessage userMessage);

    boolean isSameOriginalSender(UserMessage userMessage, String originalSender);

    boolean isSameFinalRecipient(UserMessage userMessage, String originalSender);

    String getOriginalUser(UserMessage userMessage, String type);

    String getService(UserMessage userMessage);

    String getAction(UserMessage userMessage);

    Map<String, String> getProperties(UserMessage userMessage);

    String getConversationId(UserMessage userMessage);

    String getRefToMessageId(UserMessage userMessage);
}
