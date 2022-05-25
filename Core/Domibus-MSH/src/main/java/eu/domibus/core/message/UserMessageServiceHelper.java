package eu.domibus.core.message;

import eu.domibus.api.model.PartyId;
import eu.domibus.api.model.UserMessage;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface UserMessageServiceHelper {

    String getOriginalSender(UserMessage userMessage);

    String getFinalRecipient(UserMessage userMessage);

    PartyId getPartyTo(UserMessage userMessage);

    String getPartyToRole(UserMessage userMessage);

    String getPartyToValue(UserMessage userMessage);

    PartyId getPartyFrom(UserMessage userMessage);

    String getPartyFromValue(UserMessage userMessage);

    String getPartyFromRole(UserMessage userMessage);

    String getProperty(UserMessage userMessage, String type);

    String getService(UserMessage userMessage);

    String getAction(UserMessage userMessage);

    Map<String, String> getProperties(UserMessage userMessage);

}
