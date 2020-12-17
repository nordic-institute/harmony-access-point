package eu.domibus.core.message;

import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageServiceHelper {

    String getOriginalSender(UserMessage userMessage);

    String getFinalRecipient(UserMessage userMessage);

    String getPartyTo(UserMessage userMessage);

    boolean isSameOriginalSender(UserMessage userMessage, String originalSender);

    boolean isSameFinalRecipient(UserMessage userMessage, String originalSender);

    String getOriginalUser(UserMessage userMessage, String type);

    String getService(UserMessage userMessage);

    String getAction(UserMessage userMessage);
}
