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
        return getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
    }

    @Override
    public String getFinalRecipient(UserMessage userMessage) {
        return getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
    }

    @Override
    public String getPartyTo(UserMessage userMessage) {
        PartyId partyId = userMessage.getPartyInfo().getTo().getToPartyId();
        if (partyId == null) {
            return null;
        }
        return partyId.getValue();
    }

    @Override
    public String getPartyFrom(UserMessage userMessage) {
        return userMessage.getPartyInfo().getFrom().getFromPartyId().getValue();
    }


    @Override
    public String getProperty(UserMessage userMessage, String propertyName) {
        if (userMessage == null || userMessage.getMessageProperties() == null) {
            return null;
        }
        String originalUser = null;
        for (Property property : userMessage.getMessageProperties()) {
            if (StringUtils.equalsIgnoreCase(property.getName(), propertyName)) {
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
}
