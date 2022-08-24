package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class UserMessageDefaultServiceHelper implements UserMessageServiceHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageDefaultServiceHelper.class);

    @Autowired
    private UserMessageDao userMessageDao;

    @Override
    public String getOriginalSender(UserMessage userMessage) {
        return getProperty(userMessage, MessageConstants.ORIGINAL_SENDER);
    }

    @Override
    public String getFinalRecipient(UserMessage userMessage) {
        return getProperty(userMessage, MessageConstants.FINAL_RECIPIENT);
    }

    protected To getTo(UserMessage userMessage) {
        if (userMessage == null
                || userMessage.getPartyInfo() == null) {
            return null;
        }
        return userMessage.getPartyInfo().getTo();
    }

    @Override
    public PartyId getPartyTo(UserMessage userMessage) {
        final To to = getTo(userMessage);
        if (to == null) {
            return null;
        }
        return to.getToPartyId();
    }

    @Override
    public String getPartyToValue(UserMessage userMessage) {
        final PartyId partyTo = getPartyTo(userMessage);
        if (partyTo == null) {
            return null;
        }
        return partyTo.getValue();
    }

    @Override
    public String getPartyToRole(UserMessage userMessage) {
        final To to = getTo(userMessage);
        if (to == null) {
            return null;
        }
        return to.getRoleValue();
    }

    protected From getFrom(UserMessage userMessage) {
        if (userMessage == null
                || userMessage.getPartyInfo() == null) {
            return null;
        }
        return userMessage.getPartyInfo().getFrom();
    }

    @Override
    public String getPartyFromRole(UserMessage userMessage) {
        final From from = getFrom(userMessage);
        if (from == null) {
            return null;
        }
        return from.getRoleValue();
    }

    @Override
    public PartyId getPartyFrom(UserMessage userMessage) {
        final From from = getFrom(userMessage);
        if (from == null) {
            return null;
        }
        return from.getFromPartyId();
    }

    @Override
    public String getPartyFromValue(UserMessage userMessage) {
        final PartyId partyFrom = getPartyFrom(userMessage);
        if (partyFrom == null) {
            return null;
        }
        return partyFrom.getValue();
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

    @Override
    public Map<String, String> getProperties(Long messageEntityId) {
        HashMap<String, String> properties = new HashMap<>();
        final UserMessage userMessage = userMessageDao.read(messageEntityId);
        final Set<MessageProperty> propertiesForMessageId = userMessage.getMessageProperties();
        propertiesForMessageId.forEach(property -> properties.put(property.getName(), property.getValue()));
        return properties;
    }

}
