package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class UserMessageDefaultServiceHelper implements UserMessageServiceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultServiceHelper.class);

    @Override
    public String getOriginalSender(UserMessage userMessage) {
        return getOriginalUser(userMessage, MessageConstants.ORIGINAL_SENDER);
    }

    @Override
    public String getFinalRecipient(UserMessage userMessage) {
        return getOriginalUser(userMessage, MessageConstants.FINAL_RECIPIENT);
    }

    @Override
    public String getPartyTo(UserMessage userMessage) {
        PartyId partyId = userMessage.getPartyInfo().getTo().getPartyId();
        if (partyId == null) {
            return null;
        }
        return partyId.getValue();
    }

    @Override
    public String getPartyFrom(UserMessage userMessage) {
        return userMessage.getPartyInfo().getFrom().getPartyId().getValue();
    }


    @Override
    public boolean isSameOriginalSender(UserMessage userMessage, String providedOriginalSender) {
        final String messageId = userMessage.getMessageId();
        LOG.debug("Checking for message [{}] if the provided original sender [{}] is the same as the message original sender", messageId, providedOriginalSender);

        if (StringUtils.isEmpty(providedOriginalSender)) {
            LOG.debug("Provided original user is empty");
            return false;
        }

        String messageOriginalSender = getOriginalSender(userMessage);
        if (StringUtils.equalsIgnoreCase(messageOriginalSender, providedOriginalSender)) {
            LOG.debug("For message [{}] the provided original sender [{}] is the same as the message original sender", messageId, providedOriginalSender);
            return true;
        }
        return false;
    }

    @Override
    public boolean isSameFinalRecipient(UserMessage userMessage, String providedFinalRecipient) {
        final String messageId = userMessage.getMessageId();
        LOG.debug("Checking for message [{}] if the provided final recipient [{}] is the same as the message final recipient", messageId, providedFinalRecipient);

        if (StringUtils.isEmpty(providedFinalRecipient)) {
            LOG.debug("Provided final recipient is empty");
            return false;
        }

        String messageOriginalSender = getFinalRecipient(userMessage);
        if (StringUtils.equalsIgnoreCase(messageOriginalSender, providedFinalRecipient)) {
            LOG.debug("For message [{}] the provided final recipient [{}] is the same as the message final recipient", messageId, providedFinalRecipient);
            return true;
        }
        return false;
    }

    @Override
    public String getOriginalUser(UserMessage userMessage, String type) {
        if (userMessage == null || userMessage.getMessageProperties() == null) {
            return null;
        }
        String originalUser = null;
        for (Property property : userMessage.getMessageProperties()) {
            if (property.getName() != null && property.getName().equalsIgnoreCase(type)) {
                originalUser = property.getValue();
                break;
            }
        }
        return originalUser;
    }

    @Override
    public String getService(UserMessage userMessage) {
        ServiceEntity service = userMessage.getService();
        if (service == null) {
            LOG.trace("Service is null");
            return null;
        }
        return service.getValue();
    }

    @Override
    public String getAction(UserMessage userMessage) {
        return userMessage.getActionValue();
    }

    @Override
    public Map<String, String> getProperties(UserMessage userMessage) {
        Map<String, String> result = new HashMap<>();
        if (userMessage == null) {
            LOG.trace("UserMessage not present");
            return result;
        }
        if (userMessage.getMessageProperties() == null) {
            LOG.debug("No properties found for UserMessage [{}]", userMessage.getEntityId());
            return result;
        }
        for (Property property : userMessage.getMessageProperties()) {
            result.put(property.getName(), property.getValue());
        }
        return result;
    }

    @Override
    public String getConversationId(UserMessage userMessage){
        return userMessage.getConversationId();
    }

    @Override
    public String getRefToMessageId (UserMessage userMessage) {
        return userMessage.getRefToMessageId();
    }
}
