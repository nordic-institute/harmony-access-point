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

    String getFinalRecipientValue(UserMessage userMessage);

    String getFinalRecipientType(UserMessage userMessage);

    PartyId getPartyTo(UserMessage userMessage);

    String getPartyToRole(UserMessage userMessage);

    String getPartyToValue(UserMessage userMessage);

    PartyId getPartyFrom(UserMessage userMessage);

    String getPartyFromValue(UserMessage userMessage);

    String getPartyFromRole(UserMessage userMessage);

    String getPropertyValue(UserMessage userMessage, String propertyName);
    String getPropertyType(UserMessage userMessage, String propertyName);

    String getService(UserMessage userMessage);

    String getAction(UserMessage userMessage);

    Map<String, String> getProperties(UserMessage userMessage);

    Map<String, String> getProperties(Long messageEntityId);
}
